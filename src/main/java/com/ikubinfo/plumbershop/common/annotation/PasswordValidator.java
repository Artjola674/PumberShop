package com.ikubinfo.plumbershop.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_MESSAGE;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_REGEX;

public class PasswordValidator implements ConstraintValidator<Password,String> {
    @Override
    public void initialize(Password constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null){
            return true;
        }
        Pattern pattern = Pattern.compile(PASS_VALIDATE_REGEX);

        if (!pattern.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PASS_VALIDATE_MESSAGE)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
