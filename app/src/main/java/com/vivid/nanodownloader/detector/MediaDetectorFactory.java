package com.vivid.nanodownloader.detector;


import com.vivid.nanodownloader.utils.LogUtils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by sean on 7/25/16.
 */
public class MediaDetectorFactory {
    private final static String TAG = "MediaDetectorFactory";
    private MediaDetectorFactory() {}
    private static Map<String, MediaDetector.Creator> productSummary = new HashMap<>();
    static {
        /**
         * register product here
         * */
        productSummary.put("mobile.twitter.com", TwitterDetector.CREATOR);
        productSummary.put("www.tumblr.com", TumblrDetector.CREATOR);
        productSummary.put("m.facebook.com", FacebookDetector.CREATOR);
        productSummary.put("www.dailymotion.com", DailyMotionVideoDetector.CREATOR);
        productSummary.put("vine.co", VineVideoDetector.CREATOR);
        productSummary.put("vimeo.com", VimeoVideoDetector.CREATOR);
        productSummary.put("www.instagram.com", InstaDetector.CREATOR);
    }
    public static MediaDetector createMatchingDetector(String url, String title) {
        String host = null;
        try {
            URL currentUrl = new URL(url);
            LogUtils.d(TAG, "host:" + currentUrl.getHost());
            host =  currentUrl.getHost();
        } catch (Exception e) {}
        if (host != null) {
            MediaDetector.Creator creator = productSummary.get(host);
            if (creator != null) {
                return creator.create(url, title);
            }
        }
        return null;
    }
}
