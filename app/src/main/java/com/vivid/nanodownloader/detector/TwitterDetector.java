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


public class TwitterDetector extends MediaDetector {
    private final static String TAG = "TwitterDetector";
    private final static String IMG_CLASS = "_1ninV_xt _1YeWCqJF";
    private final static String VIDEO_CLASS = "_1tHcaxlQ";

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new TwitterDetector(url, title);
        }
    };

    protected TwitterDetector(String url, String title) {
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
            Elements images = doc.select("img[class*=" + IMG_CLASS + "]");
            Elements videos = doc.select("video[class*=" + VIDEO_CLASS + "]");
            LogUtils.d(TAG, "extractMediaResource image:" + images.size() +
                    ", video size:" + videos.size());
            if (videos != null && videos.size() > 0) {
                for (int index = 0; index < videos.size(); ++index) {
                    try {
                        Element videoElement = videos.get(index);
                        String thumbUrl = videoElement.attr("poster");
                        Element mp4Element = videoElement.select("source[type=video/mp4]").get(0);
                        String url = mp4Element.attr("src");
                        if (url.contains("googlevideo.com")) {
                            continue;
                        }
                        String fileName = HttpUtils.getFilenameFromURL(url);
                        VideoEntry videoEntry = new VideoEntry();
                        videoEntry.setTitle(fileName);
                        videoEntry.setMimeType("video/mp4");
                        videoEntry.setUrl(url);
                        videoEntry.setThumbUrl(thumbUrl);
                        videoEntry.setSource("twitter");
                        mediaEntries.add(videoEntry);
                    } catch (Exception e) {}
                }
            }
            if (images != null && images.size() > 0) {
                for (int index = 0; index < images.size(); ++index) {
                    try {
                        Element imgElement = images.get(index);
                        String url = imgElement.attr("src");
                        String fileName = HttpUtils.getFilenameFromURL(url);
                        String mime = MimeTypes.instance().getMime(null, url);
                        ImageEntry imageEntry = new ImageEntry();
                        imageEntry.setTitle(fileName);
                        imageEntry.setMimeType(mime);
                        imageEntry.setUrl(url);
                        imageEntry.setSource("twitter");
                        mediaEntries.add(imageEntry);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
        return mediaEntries;
    }
}
