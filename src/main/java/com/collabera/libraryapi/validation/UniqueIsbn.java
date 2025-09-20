package com.collabera.libraryapi.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UniqueIsbnValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueIsbn {
    String message() default "ISBN must be unique";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
