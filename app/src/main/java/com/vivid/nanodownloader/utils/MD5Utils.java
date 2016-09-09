package com.vivid.nanodownloader.utils;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

public class MD5Utils {

    public static String getMD5(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }
        try {
            return getDigest(input.getBytes("UTF-8"), "MD5");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String getDigest(byte[] bytes, String algorithm) {
        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance(algorithm);
            messageDigest.reset();
            messageDigest.update(bytes);
        } catch (Exception e) {
            return null;
        }

        byte[] byteArray = messageDigest.digest();
        StringBuilder md5StrBuff = new StringBuilder(byteArray.length * 2);
        for (byte b : byteArray) {
            md5StrBuff.append(Integer.toHexString((0xFF & b) >> 4));
            md5StrBuff.append(Integer.toHexString(0x0F & b));
        }
        return md5StrBuff.toString();
    }
}