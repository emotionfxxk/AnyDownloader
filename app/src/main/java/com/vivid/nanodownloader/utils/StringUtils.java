package com.vivid.nanodownloader.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringUtils {

    public static String convertObjectToString(Object object)
            throws IOException {
        if (object == null) {
            return "";
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.close();
        byte[] byteArray = baos.toByteArray();
        return new String(Base64.encode(byteArray, 0, byteArray.length));
    }

    public static Object convertStringToObject(String string)
            throws IOException, ClassNotFoundException {
        if (string.length() == 0) {
            return null;
        }
        byte[] data = Base64.decode(string);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(
                data));
        Object object = ois.readObject();
        ois.close();
        return object;
    }

    public static boolean isChinese(char ch) {
        return (0xAC00 <= ch && ch <= 0xD7A3) ||
                (0x4E00 <= ch && ch <= 0x9FCC);
    }

    public static boolean containChinese(String string) {
        for (char c: string.toCharArray()) {
            if (isChinese(c)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Like URLEncoder.encode, except translates spaces into %20 instead of +
     *
     * @param s
     * @return
     */
    public static String encodeUrl(String s) {
        String enc = "";

        if (s != null) {
            try {
                enc = URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Impossible to run in an environment with lack of UTF-8 support", e);
            }
        }

        return enc;
    }
    /**
     * Check if a String is null or empty (the length is null).
     *
     * @param s the string to check
     * @return true if it is null or empty
     */
    public static boolean isNullOrEmpty(String s, boolean trim) {
        return s == null || (trim ? s.trim().length() == 0 : s.length() == 0);
    }

    public static String removeDoubleSpaces(String s) {
        return s != null ? s.replaceAll("\\s+", " ") : null;
    }

    public static boolean isNullOrEmpty(String s) {
        return isNullOrEmpty(s, false);
    }
}
