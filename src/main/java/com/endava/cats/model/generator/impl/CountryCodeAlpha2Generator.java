package com.endava.cats.model.generator.impl;

import com.endava.cats.model.generator.api.PropertySanitizer;
import com.endava.cats.model.generator.api.ValidDataFormatGenerator;
import io.swagger.v3.oas.models.media.Schema;

import javax.inject.Singleton;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

@Singleton
public class CountryCodeAlpha2Generator implements ValidDataFormatGenerator {
    private static final Random RANDOM = new Random();

    @Override
    public Object generate(Schema<?> schema) {
        Set<String> isoCountries = Locale.getISOCountries(Locale.IsoCountryCode.PART1_ALPHA2);
        return isoCountries.stream().skip(RANDOM.nextInt(isoCountries.size())).findFirst().orElse(Locale.ROOT.getCountry());
    }

    @Override
    public boolean appliesTo(String format, String propertyName) {
        return "iso3166alpha2".equalsIgnoreCase(PropertySanitizer.sanitize(format));
    }
}