package com.vivid.nanodownloader.detector;


import android.text.TextUtils;

import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.LogUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class VimeoVideoDetector extends MediaDetector {
    private final static String TAG = "VimeoVideoDetector";
    private String videoKey;

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new VimeoVideoDetector(url, title);
        }
    };

    protected VimeoVideoDetector(String url, String title) {
        super(url, title);
        try {
            URL currentUrl = new URL(requestUrl);
            boolean isVimeo = currentUrl.getHost().toLowerCase().contains("vimeo.com");
            if (isVimeo && url.contains("vimeo.com/")) {
                int index = url.lastIndexOf("/") + 1;
                if (url.length() > index) {
                    videoKey = url.substring(index, url.length());
                    LogUtils.d(TAG, "getVideoEntry videoKey:" + videoKey);
                }
            }
        } catch (Exception e) {}
    }
    @Override
    public boolean isAvailable() {
        return (videoKey != null);
    }

    @Override
    protected List<MediaEntry> extractMediaResource(String rawHtml) {
        List<MediaEntry> mediaEntries;
        try {
            if (rawHtml != null) {
                String strTitle = "", embedUrl = "";
                String regex = "['\"]embedUrl['\"]\\s*:\\s*['\"]([^'\"]+)['\"]";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(rawHtml);

                while (matcher.find()) {
                    embedUrl = matcher.group(1);
                    LogUtils.d(TAG, "embedUrl:" + embedUrl);
                }
                if (rawHtml.contains("<title>") && rawHtml.contains("</title>")) {
                    strTitle = rawHtml.substring(rawHtml.indexOf("<title>") + 7, rawHtml.indexOf("</title>"));
                    LogUtils.d(TAG, "strTitle:" + strTitle);
                }
                if (!TextUtils.isEmpty(embedUrl)) {
                    // make second request
                    BufferedReader in = null;
                    try {
                        URL reqUrl = new URL(embedUrl);
                        HttpURLConnection conn = (HttpURLConnection) reqUrl.openConnection();
                        conn.setRequestProperty("accept", "*/*");
                        conn.setRequestProperty("connection", "Keep-Alive");
                        conn.setRequestProperty("user-agent", getUserAgent());
                        HashMap<String, String> properties = getRequestProperties();
                        if (properties != null) {
                            for (Map.Entry<String, String> property : properties.entrySet()) {
                                conn.setRequestProperty(property.getKey(), property.getValue());
                            }
                        }
                        conn.connect();
                        if (conn.getResponseCode() == 200) {
                            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            StringBuilder sb = new StringBuilder();
                            String line;
                            while ((line = in.readLine()) != null) {
                                sb.append(line);
                            }
                            String rawContent = sb.toString();
                            int startOfVideoJson = rawContent.indexOf("\"progressive\":") + 14;
                            int endOfVideoJson = rawContent.indexOf("]", startOfVideoJson) + 1;
                            String videoJson =  rawContent.substring(startOfVideoJson, endOfVideoJson);
                            LogUtils.d(TAG, "videoJson: " + videoJson);
                            JSONArray videos = new JSONArray(videoJson);
                            mediaEntries = new ArrayList<>();
                            for (int index = 0; index < videos.length(); ++index) {
                                JSONObject videoInfo = videos.getJSONObject(index);
                                String url = videoInfo.optString("url", null);
                                if (url != null && !TextUtils.isEmpty(url)) {
                                    VideoEntry video = new VideoEntry();
                                    video.setKey(videoKey);
                                    video.setQuality(videoInfo.optString("quality", null));
                                    video.setMimeType(videoInfo.optString("mime", null));
                                    video.setUrl(url);
                                    video.setTitle(strTitle);
                                    video.setSource("Vimeo");
                                    if (!mediaEntries.contains(video)) {
                                        mediaEntries.add(video);
                                    }
                                }
                            }
                            return mediaEntries;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.w(TAG, "exception: " + e.getMessage());
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {}
        return null;
    }
}
