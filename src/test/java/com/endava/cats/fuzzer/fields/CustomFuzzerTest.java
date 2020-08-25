package com.endava.cats.fuzzer.fields;

import com.endava.cats.fuzzer.http.ResponseCodeFamily;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.io.TestCaseExporter;
import com.endava.cats.model.CatsResponse;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.ExecutionStatisticsListener;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.util.CatsUtil;
import com.google.gson.JsonObject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
class CustomFuzzerTest {
    @MockBean
    private ServiceCaller serviceCaller;

    @SpyBean
    private TestCaseListener testCaseListener;

    @MockBean
    private ExecutionStatisticsListener executionStatisticsListener;

    @MockBean
    private TestCaseExporter testCaseExporter;

    @SpyBean
    private BuildProperties buildProperties;

    @MockBean
    private CatsUtil catsUtil;

    private CustomFuzzer customFuzzer;

    @BeforeAll
    static void init() {
        System.setProperty("name", "cats");
        System.setProperty("version", "4.3.2");
        System.setProperty("time", "100011111");
    }

    @BeforeEach
    void setup() {
        customFuzzer = new CustomFuzzer(serviceCaller, testCaseListener, catsUtil);
        Mockito.when(buildProperties.getName()).thenReturn("CATS");
        Mockito.when(buildProperties.getVersion()).thenReturn("1.1");
        Mockito.when(buildProperties.getTime()).thenReturn(Instant.now());
        ReflectionTestUtils.setField(testCaseListener, "buildProperties", buildProperties);
    }


    @Test
    void givenAnEmptyCustomFuzzerFile_whenTheFuzzerRuns_thenNothingHappens() {
        FuzzingData data = FuzzingData.builder().build();
        ReflectionTestUtils.setField(customFuzzer, "customFuzzerFile", "empty");
        CustomFuzzer spyCustomFuzzer = Mockito.spy(customFuzzer);
        spyCustomFuzzer.fuzz(data);

        Mockito.verify(spyCustomFuzzer, Mockito.never()).processCustomFuzzerFile(data);
        Assertions.assertThat(customFuzzer.description()).isNotNull();
        Assertions.assertThat(customFuzzer).hasToString(customFuzzer.getClass().getSimpleName());
    }

    @Test
    void givenACustomFuzzerFileWithSimpleTestCases_whenTheFuzzerRuns_thenCustomTestCasesAreExecuted() throws Exception {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        FuzzingData data = FuzzingData.builder().path("path1").payload("{'field':'oldValue'}").
                responses(responses).responseCodes(Collections.singleton("200")).build();
        CatsResponse catsResponse = CatsResponse.builder().body("{}").responseCode(200).build();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("field", "oldValue");

        ReflectionTestUtils.setField(customFuzzer, "customFuzzerFile", "custom");
        Mockito.when(catsUtil.parseYaml(any())).thenReturn(createCustomFuzzerFile());
        Mockito.when(catsUtil.parseAsJsonElement(data.getPayload())).thenReturn(jsonObject);
        Mockito.when(catsUtil.getJsonElementBasedOnFullyQualifiedName(Mockito.eq(jsonObject), Mockito.eq("field"))).thenReturn(jsonObject);
        Mockito.when(serviceCaller.call(Mockito.any(), Mockito.any())).thenReturn(catsResponse);
        CustomFuzzer spyCustomFuzzer = Mockito.spy(customFuzzer);
        spyCustomFuzzer.loadCustomFuzzerFile();
        spyCustomFuzzer.fuzz(data);

        Mockito.verify(spyCustomFuzzer, Mockito.times(1)).processCustomFuzzerFile(data);
        Mockito.verify(testCaseListener, Mockito.times(3)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(catsResponse), Mockito.eq(ResponseCodeFamily.TWOXX));
        Assertions.assertThat(jsonObject.toString()).contains("newValue");
    }

    @Test
    void givenACompleteCustomFuzzerFileWithDescriptionAndOutputVariables_whenTheFuzzerRuns_thenTheTestCasesAreCorrectlyExecuted() throws Exception {
        ReflectionTestUtils.setField(customFuzzer, "customFuzzerFile", "src/test/resources/customFuzzer.yml");
        Mockito.doCallRealMethod().when(catsUtil).parseYaml("src/test/resources/customFuzzer.yml");
        Mockito.doCallRealMethod().when(catsUtil).parseAsJsonElement(Mockito.anyString());
        Mockito.doCallRealMethod().when(catsUtil).getJsonElementBasedOnFullyQualifiedName(Mockito.any(), Mockito.anyString());
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        CatsResponse catsResponse = CatsResponse.from(200, "{'code': '200'}", "POST");

        FuzzingData data = FuzzingData.builder().path("/pets/{id}/move").payload("{'pet':'oldValue'}").
                responses(responses).responseCodes(Collections.singleton("200")).build();
        Mockito.when(serviceCaller.call(Mockito.any(), Mockito.any())).thenReturn(catsResponse);
        CustomFuzzer spyCustomFuzzer = Mockito.spy(customFuzzer);
        spyCustomFuzzer.loadCustomFuzzerFile();
        spyCustomFuzzer.fuzz(data);
        Mockito.verify(testCaseListener, Mockito.times(4)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(catsResponse), Mockito.eq(ResponseCodeFamily.TWOXX));
    }

    private Map<String, Map<String, Object>> createCustomFuzzerFile() {
        Map<String, Map<String, Object>> result = new HashMap<>();
        Map<String, Object> path = new HashMap<>();
        Map<String, Object> tests = new HashMap<>();
        tests.put("k1", "v1");
        tests.put("field", Arrays.asList("newValue", "newValue2"));
        tests.put("expectedResponseCode", "200");

        path.put("test1", tests);
        path.put("test2", tests);

        result.put("path1", path);
        return result;
    }
}
