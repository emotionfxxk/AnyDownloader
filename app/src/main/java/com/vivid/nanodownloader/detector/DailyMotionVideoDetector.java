package com.vivid.nanodownloader.detector;



import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.LogUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DailyMotionVideoDetector extends MediaDetector {
    private final static String TAG = "DailyMotionVideoDetector";
    private static final String REQUEST_URL = "http://www.dailymotion.com/video/{0}";

    private String videoKey;

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new DailyMotionVideoDetector(url, title);
        }
    };

    protected DailyMotionVideoDetector(String url, String title) {
        super(url, title);
        try {
            URL currentUrl = new URL(url);
            boolean isDailyMotion = currentUrl.getHost().toLowerCase().contains("dailymotion.com");
            LogUtils.d(TAG, "host:" + currentUrl.getHost());
            if (isDailyMotion && url.contains("dailymotion.com/video/")) {
                videoKey = url.substring(url.lastIndexOf("video/") + 6, url.length());
            }

        } catch (Exception e) {}
    }
    @Override
    public boolean isAvailable() {
        return videoKey != null;
    }

    @Override
    protected List<MediaEntry> extractMediaResource(String rawHtml) {
        List<MediaEntry> mediaEntries;
        try {
            if (rawHtml != null) {
                int startMatch = rawHtml.indexOf("\"qualities\":");
                int endMath = rawHtml.indexOf("}]}", startMatch);
                String data = rawHtml.substring(startMatch + 12, endMath + 3);
                LogUtils.d(TAG, "data:" + data);
                JSONObject videoJson = new JSONObject(data);
                Iterator<String> iterator = videoJson.keys();
                mediaEntries = new ArrayList<>();
                while (iterator.hasNext()) {
                    String resKey = iterator.next();
                    if (resKey.equals("auto")) {
                        continue;
                    }
                    JSONObject resInfo = videoJson.optJSONArray(resKey).getJSONObject(0);
                    String mime = resInfo.optString("type");
                    String videoUrl = resInfo.optString("url");
                    LogUtils.d(TAG, "resKey:" + resKey + ", mime:" + mime
                            + ", url:" + videoUrl);
                    VideoEntry video = new VideoEntry();
                    video.setTitle(title);
                    video.setKey(videoKey);
                    video.setMimeType(mime);
                    video.setQuality(resKey + "p");
                    video.setUrl(videoUrl);
                    video.setSource("dailymotion");
                    if (!mediaEntries.contains(video)) {
                        mediaEntries.add(video);
                    }
                }
                return mediaEntries;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
