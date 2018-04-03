package com.lupinemoon.favicoin.presentation.utils;

import java.util.UUID;

public class StringUtils {

    public static String getUniqueUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }
}
