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


public class TumblrDetector extends MediaDetector {
    private final static String TAG = "TumblrDetector";
    private final static String IMG_SRC_PATTERN = "media.tumblr.com";

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new TumblrDetector(url, title);
        }
    };

    protected TumblrDetector(String url, String title) {
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
            Elements images = doc.select("img[src*=" + IMG_SRC_PATTERN + "]");
            LogUtils.d(TAG, "extractMediaResource:" + images.size());
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
                        imageEntry.setSource("tumblr");
                        mediaEntries.add(imageEntry);
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}
        return mediaEntries;
    }
}
