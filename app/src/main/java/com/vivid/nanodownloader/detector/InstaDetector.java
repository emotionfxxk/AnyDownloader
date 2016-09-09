package com.vivid.nanodownloader.detector;


import com.vivid.nanodownloader.detector.entry.ImageEntry;
import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.HttpUtils;
import com.vivid.nanodownloader.utils.MimeTypes;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;


public class InstaDetector extends MediaDetector {
    private final static String TAG = "InstaDetector";
    private final static String IMG_CLASS = "_icyx7";
    private final static String VIDEO_CLASS = "_c8hkj";

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new InstaDetector(url, title);
        }
    };

    protected InstaDetector(String url, String title) {
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
            Elements videos = doc.select("video." + VIDEO_CLASS);
            Elements images = doc.select("img." + IMG_CLASS);
            if (videos != null && videos.size() > 0) {
                for (int index = 0; index < videos.size(); ++index) {
                    Element videoElement = videos.get(index);
                    String thumbUrl = videoElement.attr("poster");
                    String url = videoElement.attr("src");
                    if (url.contains("googlevideo.com")) {
                        continue;
                    }
                    String mime = videoElement.attr("type");
                    String fileName = HttpUtils.getFilenameFromURL(url);
                    VideoEntry videoEntry = new VideoEntry();
                    videoEntry.setTitle(fileName);
                    videoEntry.setMimeType(mime);
                    videoEntry.setUrl(url);
                    videoEntry.setThumbUrl(thumbUrl);
                    videoEntry.setSource("instagram");
                    mediaEntries.add(videoEntry);
                }
            }
            if (images != null && images.size() > 0) {
                for (int index = 0; index < images.size(); ++index) {
                    Element imgElement = images.get(index);
                    String url = imgElement.attr("src");
                    String fileName = HttpUtils.getFilenameFromURL(url);
                    String mime = MimeTypes.instance().getMime(null, url);
                    ImageEntry imageEntry = new ImageEntry();
                    imageEntry.setTitle(fileName);
                    imageEntry.setMimeType(mime);
                    imageEntry.setUrl(url);
                    imageEntry.setSource("instagram");
                    mediaEntries.add(imageEntry);
                }
            }
        } catch (Exception e) {}
        return mediaEntries;
    }
}
