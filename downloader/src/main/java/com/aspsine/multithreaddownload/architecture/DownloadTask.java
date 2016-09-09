package com.aspsine.multithreaddownload.architecture;

import com.aspsine.multithreaddownload.DownloadException;

/**
 * Created by Aspsine on 2015/7/22.
 */
public interface DownloadTask extends Runnable {

    interface OnDownloadListener {

        void onDownloadProgress(long finished, long length);

        void onDownloadCompleted();

        void onDownloadPaused();

        void onDownloadAutoPaused();

        void onDownloadCanceled();

        void onDownloadFailed(DownloadException de);
    }

    void cancel();

    void pause();

    void autoPause();

    boolean isDownloading();

    boolean isComplete();

    boolean isPaused();

    boolean isAutoPaused();

    boolean isCanceled();

    boolean isFailed();

    @Override
    void run();
}
