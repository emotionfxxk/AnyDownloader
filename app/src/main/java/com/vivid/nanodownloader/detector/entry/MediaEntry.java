package com.vivid.nanodownloader.detector.entry;

/**
 * Created by sean on 7/25/16.
 */
public abstract class MediaEntry {
    protected String url;
    protected String mimeType;
    protected String quality;
    protected long contentLength;
    protected String title;
    protected String source;

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public String getQuality() {
        return quality;
    }
    public void setQuality(String quality) {
        this.quality = quality;
    }
    public long getContentLength() {
        return contentLength;
    }
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getTitle() {
        return title;
    }
    public void setSource(String source) {
        this.source = source;
    }
    public String getSource() {
        return source;
    }

}
