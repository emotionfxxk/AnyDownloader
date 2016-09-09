package com.vivid.nanodownloader.detector.entry;

public class VideoEntry extends MediaEntry {
    protected String key;
    protected String thumbUrl;

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }
    public String getThumbnail() {
        return thumbUrl;
    }
    public void setKey(String key) {
        this.key = key;
    }
    public String getKey() {
        return key;
    }
}
