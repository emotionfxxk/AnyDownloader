package com.vivid.nanodownloader.utils;

import java.text.DecimalFormat;

/**
 * Created by sean on 7/5/16.
 */
public class SizeFormatter {
    private static final DecimalFormat DF = new DecimalFormat("0.00");

    public static String getDownloadPerSize(long finished, long total) {
        return DF.format((float) finished / (1024 * 1024)) + "M/" + DF.format((float) total / (1024 * 1024)) + "M";
    }
}
