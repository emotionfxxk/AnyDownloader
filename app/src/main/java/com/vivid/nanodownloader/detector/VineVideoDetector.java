package com.vivid.nanodownloader.detector;



import android.webkit.URLUtil;

import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.HttpUtils;
import com.vivid.nanodownloader.utils.LogUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class VineVideoDetector extends MediaDetector {
    private final static String TAG = "VineVideoDetector";
    private String videoKey;

    public static Creator CREATOR = new Creator() {
        @Override
        public MediaDetector create(String url, String title) {
            return new VineVideoDetector(url, title);
        }
    };

    protected VineVideoDetector(String url, String title) {
        super(url, title);
        try {
            URL currentUrl = new URL(requestUrl);
            boolean isVine = currentUrl.getHost().toLowerCase().contains("vine");
            if (isVine && url.contains("vine.co/v/")) {
                videoKey = url.substring(url.lastIndexOf("v/") + 2, url.length());
                LogUtils.d(TAG, "getVideoEntry videoKey:" + videoKey);
            }
        } catch (Exception e) {}
    }
    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    protected List<MediaEntry> extractMediaResource(String rawHtml) {
        LogUtils.d(TAG, "extractMediaResource:" + rawHtml);
        List<MediaEntry> mediaEntries = new ArrayList<>();
        try {
            if (rawHtml != null) {
                Document doc = Jsoup.parse(rawHtml);
                LogUtils.d(TAG, "extractMediaResource after parse:");
                Elements videos = doc.select("video[src]");
                if (videos != null && videos.size() > 0) {
                    for (int index = 0; index < videos.size(); ++index) {
                        Element videoElement = videos.get(index);
                        String url = videoElement.attr("src");
                        if (url.contains("googlevideo.com")) {
                            continue;
                        }
                        String fileName = HttpUtils.getFilenameFromURL(url);
                        LogUtils.d(TAG, "url:" + url);
                        if (!URLUtil.isValidUrl(url)) continue;
                        VideoEntry videoEntry = new VideoEntry();
                        videoEntry.setTitle(fileName);
                        videoEntry.setMimeType("video/mp4");
                        videoEntry.setUrl(url);
                        videoEntry.setSource("Vine");
                        mediaEntries.add(videoEntry);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaEntries;
        /*
        List<MediaEntry> mediaEntries;
        try {
            if (rawHtml != null) {
                //if (videoKey != null) {
                    int startMatch = rawHtml.indexOf("<script type=\"application/ld+json\">");
                    int endMath = rawHtml.indexOf("</script>", startMatch);
                    String data = rawHtml.substring(startMatch + 35, endMath);
                    LogUtils.d(TAG, "videoKey not null, data:" + data);
                    mediaEntries = new ArrayList<>();
                    int startOfName = data.lastIndexOf("\"name\"");
                    int indexOfStart = data.indexOf(":", startOfName);
                    int indexOfStop= data.indexOf(",", startOfName);
                    String nameValue = data.substring(indexOfStart, indexOfStop);
                    // TODO: F U C K vine frontend engineer
                    // fix invalid json string
                    if (!nameValue.contains("\"")); {
                        data = data.substring(0, indexOfStart + 1) + "\"\"" +
                                data.substring(indexOfStart + 1, data.length());
                    }
                    JSONObject appData = new JSONObject(data);
                    JSONObject videoJson = appData.optJSONObject("sharedContent");
                    String videoUrl = videoJson.optString("contentUrl", null);
                    String thumbUrl = videoJson.optString("thumbnailUrl", null);
                    String strTitle = videoJson.optString("name", null);
                    LogUtils.d(TAG, "strTitle:" + strTitle + ", videoUrl:" + videoUrl +
                            ", thumbUrl:" + thumbUrl);
                    VideoEntry video = new VideoEntry();
                    video.setKey(videoKey);
                    video.setQuality("Unknown");
                    video.setMimeType("video/mp4");
                    video.setUrl(videoUrl);

                    video.setTitle(TextUtils.isEmpty(strTitle) ? title : strTitle);
                    video.setThumbUrl(thumbUrl);
                    if (!mediaEntries.contains(video)) {
                        mediaEntries.add(video);
                    }
                    return mediaEntries;
                } else {
                    int startMatch = 0, stopMatch;
                    int startUrl, stopUrl;
                    mediaEntries = new ArrayList<>();
                    do {
                        LogUtils.d(TAG, "startMatch:" + startMatch);
                        startMatch = rawHtml.indexOf("<video", startMatch);
                        if (startMatch == -1) break;
                        startMatch = startMatch + 6;
                        stopMatch = rawHtml.indexOf(">", startMatch);
                        String videoTag = rawHtml.substring(startMatch, stopMatch);
                        LogUtils.d(TAG, "videoTag:" + videoTag);
                        startUrl = videoTag.indexOf("src=\"");
                        if (startUrl == -1) continue;
                        stopUrl = videoTag.indexOf("\"", startUrl + 5);
                        String url = videoTag.substring(startUrl + 5, stopUrl);
                        LogUtils.d(TAG, "url:" + url);
                        if (!url.equals("default.mp4")) {
                            VideoEntry video = new VideoEntry();
                            //video.setKey(videoKey);
                            video.setQuality("Unknown");
                            video.setMimeType("video/mp4");
                            video.setUrl(url);
                            video.setTitle(title);
                            if (!mediaEntries.contains(video)) {
                                mediaEntries.add(video);
                            }
                        }
                    } while (true);
                    return mediaEntries;
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;*/
    }
}
