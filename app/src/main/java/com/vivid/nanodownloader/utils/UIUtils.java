package com.vivid.nanodownloader.utils;

import java.util.Locale;

/**
 * Created by webeyejoe on 16-7-14.
 */
public class UIUtils {

    private static String[] BYTE_UNITS = new String[]{"b", "KB", "Mb", "Gb", "Tb"};

    public static String getBytesInHuman(float size) {
        int i = 0;
        for (i = 0; size > 1024; i++) {
            size /= 1024f;
        }
        return String.format(Locale.US, "%.2f %s", size, BYTE_UNITS[i]);
    }

}
