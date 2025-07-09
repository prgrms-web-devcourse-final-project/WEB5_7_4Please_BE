package com.deal4u.fourplease.global.util;


import lombok.experimental.UtilityClass;
import org.slf4j.helpers.MessageFormatter;

@UtilityClass
public class StringUtils {

    public static String formatMessage(String message, Object... args) {
        return MessageFormatter.arrayFormat(message, args).getMessage();
    }
}
