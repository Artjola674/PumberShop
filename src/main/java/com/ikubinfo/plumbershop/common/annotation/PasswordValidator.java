package com.ikubinfo.plumbershop.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static com.ikubinfo.plumbershop.common.constants.Constants.INPUT_NOT_NULL;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_MESSAGE;
import static com.ikubinfo.plumbershop.user.constants.UserConstants.PASS_VALIDATE_REGEX;

public class PasswordValidator implements ConstraintValidator<Password,String> {
    private boolean allowNull;
    @Override
    public void initialize(Password constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null && !allowNull){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(INPUT_NOT_NULL)
                    .addConstraintViolation();
            return false;
        }
        Pattern pattern = Pattern.compile(PASS_VALIDATE_REGEX);

        if (password != null && !pattern.matcher(password).matches()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(PASS_VALIDATE_MESSAGE)
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
