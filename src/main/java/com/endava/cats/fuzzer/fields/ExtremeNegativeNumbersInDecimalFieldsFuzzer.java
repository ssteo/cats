package com.endava.cats.fuzzer.fields;

import com.endava.cats.annotations.FieldFuzzer;
import com.endava.cats.args.FilesArguments;
import com.endava.cats.fuzzer.fields.base.BaseBoundaryFieldFuzzer;
import com.endava.cats.generator.simple.NumberGenerator;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.model.FuzzingData;
import com.endava.cats.report.TestCaseListener;
import io.swagger.v3.oas.models.media.Schema;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Fuzzer that sends extreme negative numbers in decimal fields.
 */
@Singleton
@FieldFuzzer
public class ExtremeNegativeNumbersInDecimalFieldsFuzzer extends BaseBoundaryFieldFuzzer {

    /**
     * Creates a new ExtremeNegativeNumbersInDecimalFieldsFuzzer instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cp files arguments
     */
    public ExtremeNegativeNumbersInDecimalFieldsFuzzer(ServiceCaller sc, TestCaseListener lr, FilesArguments cp) {
        super(sc, lr, cp);
    }

    @Override
    public String typeOfDataSentToTheService() {
        return "extreme negative values";
    }

    @Override
    public List<String> getSchemaTypesTheFuzzerWillApplyTo() {
        return List.of("number");
    }

    @Override
    public Object getBoundaryValue(Schema schema) {
        return NumberGenerator.getExtremeNegativeDecimalValue(schema);
    }

    @Override
    public boolean hasBoundaryDefined(String fuzzedField, FuzzingData data) {
        return true;
    }

    @Override
    public String description() {
        return String.format("iterate through each Number field and send %s for no format, %s for float and %s for double", NumberGenerator.MOST_NEGATIVE_DECIMAL, -Float.MAX_VALUE, -Double.MAX_VALUE);
    }
}