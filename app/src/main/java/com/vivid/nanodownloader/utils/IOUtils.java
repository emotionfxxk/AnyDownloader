package com.vivid.nanodownloader.utils;





import com.vivid.nanodownloader.BuildConfig;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.zip.ZipFile;

public class IOUtils {

    private static final String TAG = "IOUtils";
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

    public static void close(Closeable... cs) {
        for (Closeable c : cs) {
            if (c != null) {
                if (BuildConfig.DEBUG) { // See comment of close variants
                    // section.
                    Class<?>[] forbiddenList = new Class<?>[] {
                            ZipFile.class, Socket.class,
                            ServerSocket.class, java.net.DatagramSocket.class,
                            java.nio.channels.Selector.class, java.util.Scanner.class,
                    };
                    for (Class<?> clz : forbiddenList) {
                        if (clz.isInstance(c)) {
                            throw new IllegalArgumentException("The " + clz.toString()
                                    + " is not Closeable until API 19. It will crash on earilier devices.");
                        }
                    }
                }
                try {
                    c.close();
                } catch (IOException t) {
                    LogUtils.e(TAG, "Exception " + t + "occur when close " + c);
                }
            }
        }
    }

    public static void close(ZipFile close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException t) {
                LogUtils.e(TAG, "Exception " + t + "occur when close " + close);
            }
        }
    }

    public static void close(Socket close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException t) {
                LogUtils.e(TAG, "Exception " + t + "occur when close " + close);
            }
        }
    }

    public static void close(ServerSocket close) {
        if (close != null) {
            try {
                close.close();
            } catch (IOException t) {
                LogUtils.e(TAG, "Exception " + t + "occur when close " + close);
            }
        }
    }

    public static void closeOrThrow(Closeable... cs) throws IOException {
        for (Closeable c : cs) {
            if (c != null) {
                c.close();
            }
        }
    }

    public static boolean closeForResult(Closeable o) {
        if (o != null) {
            try {
                o.close();
            } catch (IOException t) {
                return false;
            }
        }
        return true;
    }

    public static void closeAfterFlush(OutputStream os) {
        if (os != null) {
            try {
                os.flush();
            } catch (IOException t) {
                LogUtils.e(TAG, "Exception " + t + "occurred when flusing " + os);
            }
            try {
                os.close();
            } catch (IOException t) {
                LogUtils.e(TAG, "Exception " + t + "occurred when flusing " + os);
            }
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(4 * DEFAULT_BUFFER_SIZE);
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int n = 0;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
    }

    public static String toString(InputStream input) {
        return toString(input, Charset.defaultCharset());
    }

    public static String toString(InputStream input, Charset charset) {
        String string = null;
        try {
            string = new String(toByteArray(input), charset.name());
        } catch (IOException e) {
            LogUtils.e(TAG, "Exception " + e + "occur when IOUtils.toString");
        }
        return string;
    }
}
