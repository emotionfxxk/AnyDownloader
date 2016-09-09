package com.aspsine.multithreaddownload;


import java.io.Serializable;

/**
 * Created by Aspsine on 2015/4/20.
 */
public class DownloadRequest implements Serializable {
    private String mUri;
    private boolean mScannable;
    // extension by sean
    private String mOriginalName;
    private String mActualName;
    private String mSavePath;
    private String mThumbnailUrl;
    private String mMimeType;
    private long mContentLength;

    private DownloadRequest(String uri,
                            boolean scannable,
                            String originalName,
                            String actualName,
                            String savePath,
                            String thumbUrl,
                            String mimeType,
                            long contentLength) {
        this.mUri = uri;
        this.mScannable = scannable;
        this.mOriginalName = originalName;
        this.mActualName = actualName;
        this.mSavePath = savePath;
        this.mThumbnailUrl = thumbUrl;
        this.mMimeType = mimeType;
        this.mContentLength = contentLength;
    }

    public String getUri() {
        return mUri;
    }
    public boolean isScannable() {
        return mScannable;
    }
    public String getOriginalName() {
        return mOriginalName;
    }
    public String getActualName() {
        return mActualName;
    }
    public String getSavePath() {
        return mSavePath;
    }
    public String getThumbnailUrl() {
        return mThumbnailUrl;
    }
    public String getMimeType() {
        return mMimeType;
    }
    public long getContentLength() { return mContentLength; }
    public static class Builder {
        private String mUri;
        private boolean mScannable;
        // extension by sean
        private String mOriginalName;
        private String mActualName;
        private String mSavePath;
        private String mThumbnailUrl;
        private String mMimeType;
        private long mContentLength;

        public Builder() {
        }

        public Builder setUri(String uri) {
            this.mUri = uri;
            return this;
        }
        public Builder setScannable(boolean scannable) {
            this.mScannable = scannable;
            return this;
        }
        public Builder setOriginalName(String originalName) {
            this.mOriginalName = originalName;
            return this;
        }
        public Builder setActualName(String actualName) {
            this.mActualName = actualName;
            return this;
        }
        public Builder setSavePath(String savePath) {
            this.mSavePath = savePath;
            return this;
        }
        public Builder setThumbUrl(String thumbUrl) {
            this.mThumbnailUrl = thumbUrl;
            return this;
        }
        public Builder setMimeType(String mimeType) {
            this.mMimeType = mimeType;
            return this;
        }
        public Builder setContentLength(long contentLength) {
            this.mContentLength = contentLength;
            return this;
        }
        public DownloadRequest build() {
            DownloadRequest request = new DownloadRequest(
                    mUri, mScannable, mOriginalName, mActualName,
                    mSavePath, mThumbnailUrl, mMimeType, mContentLength);
            return request;
        }
    }
}
