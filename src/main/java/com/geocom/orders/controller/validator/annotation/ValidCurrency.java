package com.geocom.orders.controller.validator.annotation;

import com.geocom.orders.controller.validator.CurrencyValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
public @interface ValidCurrency {
    String message() default "{api.validation.unknown-currency}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}