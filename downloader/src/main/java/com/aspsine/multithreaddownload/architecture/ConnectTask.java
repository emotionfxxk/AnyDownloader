package com.aspsine.multithreaddownload.architecture;

import com.aspsine.multithreaddownload.DownloadException;

/**
 * Created by Aspsine on 2015/10/29.
 */
public interface ConnectTask extends Runnable {

    interface OnConnectListener {
        void onConnecting();

        void onConnected(long time, long length, boolean isAcceptRanges);

        void onConnectCanceled();

        void onConnectStopped();

        void onConnectFailed(DownloadException de);
    }

    void cancel();

    void stop();

    boolean isConnecting();

    boolean isConnected();

    boolean isCanceled();

    boolean isFailed();

    @Override
    void run();
}
