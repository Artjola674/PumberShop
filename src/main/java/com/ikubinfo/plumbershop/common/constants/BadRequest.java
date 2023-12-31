package com.ikubinfo.plumbershop.common.constants;

public class BadRequest {
    public static final String INVALID_TOKEN = "Invalid JWT token";
    public static final String EXPIRED_TOKEN = "Expired JWT token";
    public static final String UNSUPPORTED_TOKEN = "Unsupported JWT token";
    public static final String EMPTY_CLAIMS = "JWT claims string is empty.";
    public static final String ACTION_NOT_ALLOWED = "Action not allowed for this user";
    public static final String EMAIL_EXISTS = "There is already one account with that email address: ";
    public static final String PASS_NOT_CORRECT = "Existing password is not correct";
    public static final String PASSWORD_NOT_MATCH = "Passwords do not match";


    private BadRequest() {
    }
}
