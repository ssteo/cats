package com.endava.cats.fuzzer.fields;

import com.endava.cats.annotations.FieldFuzzer;
import com.endava.cats.annotations.ValidateAndSanitize;
import com.endava.cats.args.FilesArguments;
import com.endava.cats.http.ResponseCodeFamily;
import com.endava.cats.io.ServiceCaller;
import com.endava.cats.report.TestCaseListener;
import com.endava.cats.util.CatsUtil;
import jakarta.inject.Singleton;

/**
 * Fuzzer that sends zalgo text in fields for the validate then sanitize strategy.
 */
@Singleton
@FieldFuzzer
@ValidateAndSanitize
public class ZalgoTextInFieldsValidateSanitizeFuzzer extends ZalgoTextInFieldsSanitizeValidateFuzzer {
    /**
     * Creates a new ZalgoTextInFieldsValidateSanitizeFuzzer instance.
     *
     * @param sc the service caller
     * @param lr the test case listener
     * @param cu the utility class
     * @param cp files arguments
     */
    protected ZalgoTextInFieldsValidateSanitizeFuzzer(ServiceCaller sc, TestCaseListener lr, CatsUtil cu, FilesArguments cp) {
        super(sc, lr, cu, cp);
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenFuzzedValueNotMatchesPattern() {
        return ResponseCodeFamily.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenRequiredFieldsAreFuzzed() {
        return ResponseCodeFamily.FOURXX;
    }

    @Override
    public ResponseCodeFamily getExpectedHttpCodeWhenOptionalFieldsAreFuzzed() {
        return ResponseCodeFamily.FOURXX;
    }

}