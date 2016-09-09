package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.core.ConnectTaskImpl;

import java.net.HttpURLConnection;

/**
 * CallBack of download status
 */
public interface CallBack {

    void onStarted(DownloadInfo downloadInfo);

    /**
     * <p> this will be the the first method called by
     * {@link ConnectTaskImpl}.
     */
    void onConnecting(DownloadInfo downloadInfo);

    /**
     * <p> if {@link ConnectTaskImpl} is successfully
     * connected with the http/https server this method will be invoke. If not method
     * {@link #onFailed(DownloadInfo, DownloadException)} will be invoke.
     *
     * @param total          The length of the file. See {@link HttpURLConnection#getContentLength()}
     * @param isRangeSupport indicate whether download can be resumed from pause.
     *                       See {@link ConnectTaskImpl#run()}. If the value of http header field
     *                       {@code Accept-Ranges} is {@code bytes} the value of  isRangeSupport is
     *                       {@code true} else {@code false}
     */
    void onConnected(DownloadInfo downloadInfo, long total, boolean isRangeSupport);

    /**
     * <p> progress callback.
     *
     * @param finished the downloaded length of the file
     * @param total    the total length of the file same value with method {@link }
     * @param progress the percent of progress (finished/total)*100
     * @param speed    the download speed (bytes per second)
     */
    void onProgress(DownloadInfo downloadInfo, long finished, long total, int progress, int speed);

    /**
     * <p> download complete
     */
    void onCompleted(DownloadInfo downloadInfo);

    /**
     * <p> if you invoke {@link DownloadManager#pause(String)} or {@link DownloadManager#pauseAll()}
     * this method will be invoke if the downloading task is successfully paused.
     */
    void onDownloadPaused(DownloadInfo downloadInfo);

    /**
     * <p> if you invoke {@link DownloadManager#pause(String)} or {@link DownloadManager#pauseAll()}
     * this method will be invoke if the downloading task is successfully paused.
     */
    void onDownloadAutoPaused(DownloadInfo downloadInfo);

    /**
     * <p> if you invoke {@link DownloadManager#cancel(String)}
     * this method will be invoke if the downloading task is successfully canceled.
     */
    void onDownloadCanceled(DownloadInfo downloadInfo);

    /**
     * <p> download fail or exception callback
     *
     * @param e download exception
     */
    void onFailed(DownloadInfo downloadInfo, DownloadException e);
}
