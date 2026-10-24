package com.skillswap.dto;

public final class Rules {

    private Rules() {
    }

    public static final String GMAIL = "^[A-Za-z0-9._%+-]+@gmail\\.com$";
    public static final String GMAIL_MESSAGE = "Use your Gmail address, for example name@gmail.com";

    public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{6,72}$";
    public static final String PASSWORD_MESSAGE =
            "Password needs 6 or more characters with an uppercase letter, a lowercase letter, a digit and a special character";
}
