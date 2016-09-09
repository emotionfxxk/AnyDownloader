package com.vivid.nanodownloader.utils;

public class Base64 {

    public static String encode(byte[] data, int offset, int length) {
        int size = (length * 4 + 2) / 3;
        int mod = (4 - size % 4) % 4;
        byte[] buf = new byte[size + mod];
        for (int i = 0; i < size; i++) {
            int index = 0;
            for (int j = 0; j < 6; j++) {
                index <<= 1;
                int bit = i * 6 + j;
                int b = 0;
                if (bit / 8 < length) {
                    b = data[offset + bit / 8];
                }
                index |= (b >> (7 - bit % 8)) & 1;
            }
            char ch;
            if (index < 26) {
                ch = (char) ('A' + index);
            } else if (index < 52) {
                ch = (char) ('a' + (index - 26));
            } else if (index < 62) {
                ch = (char) ('0' + (index - 52));
            } else if (index == 62) {
                ch = '+';
            } else {
                ch = '/';
            }
            buf[i] = (byte) ch;
        }
        for (int i = 0; i < mod; i++) {
            buf[size + i] = (byte) '=';
        }
        return new String(buf);
    }

    public static byte[] decode(String text) {
        byte[] buf = new byte[text.length() * 3 / 4];
        int data = 0;
        int bits = 0;
        int pos = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int index;
            if (ch >= 'A' && ch <= 'Z') {
                index = ch - 'A';
            } else if (ch >= 'a' && ch <= 'z') {
                index = ch - 'a' + 26;
            } else if (ch >= '0' && ch <= '9') {
                index = ch - '0' + 52;
            } else if (ch == '+') {
                index = 62;
            } else if (ch == '/') {
                index = 63;
            } else if (ch == '=') {
                break;
            } else {
                return null;
            }
            data = (data << 6) | index;
            bits += 6;
            if (bits >= 8) {
                buf[pos] = (byte) (data >> (bits - 8));
                data ^= (buf[pos++] << (bits - 8));
                bits -= 8;
            }
        }
        if (pos == buf.length) {
            return buf;
        }
        byte[] buf2 = new byte[pos];
        System.arraycopy(buf, 0, buf2, 0, pos);
        return buf2;
    }
}
