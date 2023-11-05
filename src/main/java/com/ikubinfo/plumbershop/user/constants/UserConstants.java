package com.ikubinfo.plumbershop.user.constants;

public class UserConstants {

    public static final String USER = "user";
    public static final String USERNAME = "username";
    public static final String USER_DOCUMENT = "userDocument";
    public static final String PASS_CHANGED_SUCCESSFULLY = "Your password has been changed successfully";
    public static final String FORGET_PASS = "Check your email for instructions to reset your password";
    public static final String RESET_TOKEN = "Reset token";
    public static final String RESET_TOKEN_EXPIRED= "Reset token has expired. Please make a new forget password request";
    public static final String PASS_VALIDATE_MESSAGE = "Password should have at least "
            + "8 characters and at most 32 characters. it should contain a digit, a lower case alphabet, an upper case alphabet and a special character.";

    public static final String PASS_VALIDATE_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+.!])(?=\\S+$).{8,32}$";
    private UserConstants() {
    }
}
