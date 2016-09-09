package com.vivid.nanodownloader.detector;


import com.vivid.nanodownloader.detector.entry.ImageEntry;
import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.HttpUtils;
import com.vivid.nanodownloader.utils.LogUtils;
import com.vivid.nanodownloader.utils.MimeTypes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class FacebookDetector extends MediaDetector {
    private final static String TAG = "FacebookDetector";
    private final static String IMG_CLASS = "img _5sgi img _4s0y";
    private final static String DIV_VIDEO_CLASS = "_53mw _4gbu _53mu";
    private final static String VIDEO_CLASS = "_53mv";
    private final static String VIDEO_THUMB_CLASS = "img _lt3";

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new FacebookDetector(url, title);
        }
    };

    protected FacebookDetector(String url, String title) {
        super(url, title);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected List<MediaEntry> extractMediaResource(String rawHtml) {
        List<MediaEntry> mediaEntries = new ArrayList<>();
        try {
            Document doc = Jsoup.parse(rawHtml);
            Elements videos = doc.select("div[class*=" + DIV_VIDEO_CLASS + "]");
            Elements images = doc.select("i[class*=" + IMG_CLASS + "]");
            LogUtils.d(TAG, "extractMediaResource:" + images.size());
            if (videos != null && videos.size() > 0) {
                for (int index = 0; index < videos.size(); ++index) {
                    try {
                        Element videoDiv = videos.get(index);
                        Element videoElement = videoDiv.select("video[class*=" + VIDEO_CLASS + "]").get(0);

                        Element videoThumbElements = videoDiv.select("i[class*=" + VIDEO_THUMB_CLASS + "]").get(0);
                        String style = videoThumbElements.attr("style");
                        int startIndexOfUrl = style.indexOf("url(") + 5;
                        int endIndex = style.indexOf(")", startIndexOfUrl) - 1;
                        String thumbUrl = style.substring(startIndexOfUrl, endIndex);

                        String url = videoElement.attr("src");
                        if (url.contains("googlevideo.com")) {
                            continue;
                        }
                        String fileName = HttpUtils.getFilenameFromURL(url);
                        VideoEntry videoEntry = new VideoEntry();
                        videoEntry.setTitle(fileName);
                        videoEntry.setMimeType("video/mp4");
                        videoEntry.setUrl(url);
                        videoEntry.setThumbUrl(thumbUrl);
                        videoEntry.setSource("facebook");
                        mediaEntries.add(videoEntry);
                    } catch (Exception e) {}
                }
            }
            if (images != null && images.size() > 0) {
                for (int index = 0; index < images.size(); ++index) {
                    try {
                        Element imgElement = images.get(index);
                        String style = imgElement.attr("style");
                        int startIndexOfUrl = style.indexOf("url(") + 5;
                        int endIndex = style.indexOf(")", startIndexOfUrl) - 1;
                        String url = style.substring(startIndexOfUrl, endIndex);
                        String fileName = HttpUtils.getFilenameFromURL(url);
                        String mime = MimeTypes.instance().getMime(null, url);
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.setTitle(fileName);
                        imageEntry.setMimeType("image/jpeg");
                        imageEntry.setUrl(url);
                        imageEntry.setSource("facebook");
                        mediaEntries.add(imageEntry);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
        return mediaEntries;
    }
}
