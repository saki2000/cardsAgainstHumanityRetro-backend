package com.retro.retro_against_humanity_backend.Constants;

public class Const {
    private Const() {}

    public static final int SESSION_ID_MIN_LENGTH = 6;
    public static final int SESSION_ID_MAX_LENGTH = 6;
    public static final String SESSION_CODE_SIZE_MESSAGE = "Session code must be exactly 6 characters";
    public static final String SESSION_ID_PATTERN = "^[a-zA-Z0-9]+$";
    public static final String SESSION_CODE_PATTERN_MESSAGE = "Session ID must be alphanumeric";
    public static final String SESSION_NOT_FOUND_MESSAGE = "Session not found";

}
