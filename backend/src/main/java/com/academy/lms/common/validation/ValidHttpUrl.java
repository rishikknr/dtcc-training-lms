package com.academy.lms.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidHttpUrlValidator.class)
public @interface ValidHttpUrl {
  String message() default "must be a valid http(s) URL";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
