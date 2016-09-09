package com.vivid.nanodownloader.utils;

import android.os.AsyncTask;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.webkit.URLUtil;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Map;


public class UrlVerifier {
    private final static String TAG = "UrlVerifier";
    private String originalUrl;
    private boolean markedCancel = false;

    public static class ResultEvent {
        public final boolean isValidUrl;
        public final String orgUrl;
        public final String finalUrl;
        public final long fileSize;
        public final String contentType;
        public final String fileName;
        public ResultEvent(boolean isValidUrl, String orgUrl, String finalUrl, long fileSize,
                           String contentType, String fileName) {
            this.isValidUrl = isValidUrl;
            this.orgUrl = orgUrl;
            this.finalUrl = finalUrl;
            this.fileSize = fileSize;
            this.contentType = contentType;
            this.fileName = fileName;
        }
    }

    public void doVerify(String url) {
        this.originalUrl = url;
        boolean isUrl = !TextUtils.isEmpty(originalUrl) && URLUtil.isValidUrl(originalUrl);
        LogUtils.d(TAG, "doVerify isUrl:" + isUrl + ", url:" + url);
        if (isUrl) {
            performVerify();
        } else {
            EventBus.getDefault().post(new ResultEvent(false, originalUrl, null, 0, null, null));
        }
    }

    public void cancel() {
        markedCancel = true;
    }

    private void performVerify() {
        new AsyncTask<Void, Void, ResultEvent>() {
            @Override
            protected ResultEvent doInBackground(Void... params) {
                LogUtils.d(TAG, "performVerify start url:" + originalUrl);
                boolean isValid = false;
                String destUrl = null;
                String srcUrl = originalUrl;
                String fileName = null, contentType = null;
                long contentLength = -1;
                while (!markedCancel) {
                    HttpURLConnection conn = null;
                    try {
                        URL originalURL = new URL(srcUrl);
                        conn = (HttpURLConnection) originalURL.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setInstanceFollowRedirects(false);
                        conn.setConnectTimeout(10 * 1000);
                        conn.connect();
                        int responseCode = conn.getResponseCode();
                        LogUtils.d(TAG, "performVerify responseCode:" + responseCode +
                                ", srcUrl:" + srcUrl);
                        if (responseCode == 302) {
                            srcUrl = conn.getHeaderField("Location");
                            LogUtils.d(TAG, "performVerify jump to new url:" + srcUrl);
                        } else if (responseCode == HttpURLConnection.HTTP_OK ||
                                responseCode == HttpURLConnection.HTTP_PARTIAL ) {
                            isValid = true;
                            destUrl = srcUrl;
                            contentLength = conn.getContentLength();
                            String contentDescription = conn.getHeaderField("Content-Description");
                            contentType = MimeTypes.instance().getMime(conn.getContentType(), destUrl);
                            if (contentDescription != null) {
                                fileName = HttpUtils.getFileNameFromContentDisposition(
                                        contentDescription);
                            }
                            if (TextUtils.isEmpty(fileName)) {
                                fileName = HttpUtils.getFilenameFromURL(destUrl);
                            }
                            LogUtils.d(TAG, "performVerify ok contentLength:" + contentLength +
                                    ", contentDescription:" + contentDescription +
                                    ", contentType:" + contentType +
                                    ", fileName:" + fileName);
                            break;
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    } finally {
                        try {
                            if (conn != null) {
                                conn.disconnect();
                            }
                        } catch (Exception e) {}
                    }
                }
                return new ResultEvent(isValid, originalUrl, destUrl, contentLength, contentType, fileName);
            }

            @Override
            protected void onPostExecute(ResultEvent resultEvent) {
                super.onPostExecute(resultEvent);
                if (!markedCancel) {
                    EventBus.getDefault().post(resultEvent);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
