package com.github.stepwise.utils.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoDuplicateIdsValidator.class)
public @interface NoDublicatesInCollection {
  String message() default "List contains duplicate IDs";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}


class NoDuplicateIdsValidator implements ConstraintValidator<NoDublicatesInCollection, List<Long>> {
  @Override
  public boolean isValid(List<Long> ids, ConstraintValidatorContext context) {
    if (ids == null) {
      return true;
    }
    return ids.size() == ids.stream().distinct().count();
  }
}

