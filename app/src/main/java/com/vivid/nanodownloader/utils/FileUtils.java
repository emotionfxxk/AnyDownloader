package com.vivid.nanodownloader.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Locale;

public class FileUtils {
    private static final int BUFFER_SIZE = 512;

    public static boolean isFileExist(String path) {
        return new File(path).exists();
    }

    public static String loadStringFromStream(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder builder = new StringBuilder();
        String str;
        while ((str = reader.readLine()) != null) {
            builder.append(str);
        }
        return builder.toString();
    }

    public static boolean safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
                return true;
            } catch (Exception e) {

            }
        }
        return false;
    }

    public static boolean write(CharSequence from, File to, Charset charset) {
        if (from == null || to == null || charset == null) {
            throw new NullPointerException();
        }

        FileOutputStream out = null;
        Writer writer = null;
        boolean result = true;

        try {
            out = new FileOutputStream(to);
            writer = new OutputStreamWriter(out, charset).append(from);
        } catch (IOException e) {
            return false;
        } finally {
            if (writer == null) {
                safeClose(out);
            }
            result &= safeClose(writer);
        }

        return result;
    }

    public static boolean deleteDir(File dir, boolean deleteSelf) {
        boolean success = false;
        if (dir.isDirectory()) {
            File files[] = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        success = deleteDir(f, true);
                    } else {
                        success = f.delete();
                    }
                    if (!success) {
                        break;
                    }
                }
            }
        }
        if (deleteSelf) {
            success = dir.delete();
        }
        return success;
    }

    public static final String getShowSize(long size) {
        float showSize = size;
        String formatString = "%.2fB";

        if (showSize < 1024) {
            return String.format(Locale.CHINA, formatString, showSize);
        }
        showSize = showSize / 1024;
        formatString = "%.2fK";
        if (showSize < 1024) {
            return String.format(Locale.CHINA, formatString, showSize);
        }
        showSize = showSize / 1024;
        formatString = "%.2fM";
        if (showSize < 1024) {
            return String.format(Locale.CHINA, formatString, showSize);
        }
        showSize = showSize / 1024;
        formatString = "%.2fG";
        return String.format(Locale.CHINA, formatString, showSize);
    }

    public static String removeIllegalChar(String originalName, String replacement) {
        return originalName.replaceAll("[?*<\":>+\\[\\]/']", replacement);
    }
}
