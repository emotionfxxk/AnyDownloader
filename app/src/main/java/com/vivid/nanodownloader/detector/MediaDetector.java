package com.vivid.nanodownloader.detector;


import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class MediaDetector {
    private final static String TAG = "MediaDetector";
    private final static String DEFAULT_UA = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) " +
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.114 Mobile Safari/537.36";
    protected String requestUrl, title;
    private List<MediaEntry> mediaEntries;

    public static class MediaDetectorException extends Exception {
        public MediaDetectorException(String message) { super(message);}
    }

    public interface Creator {
        MediaDetector create(String url, String title);
    }

    protected MediaDetector(String url, String title) {
        this.requestUrl = url;
        this.title = title;
    }

    public List<MediaEntry> getMediaEntries() {
        return mediaEntries;
    }

    abstract public boolean isAvailable();
    abstract protected List<MediaEntry> extractMediaResource(String rawHtml);
    protected HashMap<String, String> getRequestProperties() { return  null; }
    protected String getUserAgent() {
        return DEFAULT_UA;
    }

    public void detect(String rawHtml) throws MediaDetectorException {
        try {
            mediaEntries = extractMediaResource(rawHtml);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtils.w(TAG, "exception: " + e.getMessage());
            throw new MediaDetectorException(e.getMessage());
        }
    }

}
