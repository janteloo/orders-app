package com.geocom.orders.controller.validator;

import com.geocom.orders.controller.validator.annotation.ValidCurrency;
import org.springframework.beans.factory.annotation.Value;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.List;

public class CurrencyValidator implements ConstraintValidator<ValidCurrency, String> {

    @Value("${app.currency.supported.collection}")
    private String availableCurrencies;

    @Override
    public boolean isValid(String currency, ConstraintValidatorContext constraintValidatorContext) {
        List<String> supportedCurrencies = Arrays.asList(availableCurrencies.split(","));
        return supportedCurrencies.contains(currency);
    }
}
