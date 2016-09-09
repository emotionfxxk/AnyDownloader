package com.aspsine.multithreaddownload.core;

import android.os.Process;
import android.text.TextUtils;

import com.aspsine.multithreaddownload.Constants;
import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.util.L;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Aspsine on 2015/7/20.
 */
public class ConnectTaskImpl implements ConnectTask {
    private final static String TAG = "ConnectTaskImpl";
    private final String mUri;
    private final OnConnectListener mOnConnectListener;
    private DownloadConfiguration mConfig;
    private int mConnectRetryCount;

    private volatile int mStatus;

    private volatile long mStartTime;

    public ConnectTaskImpl(String uri, DownloadConfiguration config, OnConnectListener listener) {
        this.mUri = uri;
        this.mConfig = config;
        this.mOnConnectListener = listener;
    }

    public void cancel() {
        mStatus = DownloadStatus.STATUS_CANCELED;
    }

    public void stop() {
        mStatus = DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isConnecting() {
        return mStatus == DownloadStatus.STATUS_CONNECTING;
    }

    @Override
    public boolean isConnected() {
        return mStatus == DownloadStatus.STATUS_CONNECTED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    private boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        mStatus = DownloadStatus.STATUS_CONNECTING;
        mOnConnectListener.onConnecting();
        mConnectRetryCount = 0;
        while (mConnectRetryCount++ < mConfig.getConnectAutoRecoverCount()) {
            L.d(TAG, "run() retryCount:" + mConnectRetryCount);
            try {
                checkPaused();
                executeConnection();
                break;
            } catch (DownloadException e) {
                if (!handleDownloadException(e)) {
                    break;
                }
            }
        }
    }

    private void executeConnection() throws DownloadException {
        mStartTime = System.currentTimeMillis();
        HttpURLConnection httpConnection = null;
        final URL url;
        try {
            url = new URL(mUri);
        } catch (MalformedURLException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_URL_MAL_FORMAT, "Bad url.", e);
        }
        try {
            checkPaused();
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(Constants.HTTP.CONNECT_TIME_OUT);
            httpConnection.setReadTimeout(Constants.HTTP.READ_TIME_OUT);
            httpConnection.setRequestMethod(Constants.HTTP.GET);
            httpConnection.setRequestProperty("Range", "bytes=" + 0 + "-");
            final int responseCode = httpConnection.getResponseCode();
            L.d(TAG, "response:" + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) {
                parseResponse(httpConnection, false);
            } else if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                parseResponse(httpConnection, true);
            } else {
                throw new DownloadException(DownloadStatus.STATUS_FAILED,
                        DownloadException.ERROR_UNSUPPORTED_RESPONSE_CODE,
                        "UnSupported response code:" + responseCode);
            }
        } catch (ProtocolException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_PROTOCOL,
                    "Protocol error", e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_NORMAL_IO,
                    "IO error", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private void parseResponse(HttpURLConnection httpConnection, boolean isAcceptRanges) throws DownloadException {

        final long length;
        String contentLength = httpConnection.getHeaderField("Content-Length");
        L.d(TAG, "parseResponse contentLength:" + contentLength);
        if (TextUtils.isEmpty(contentLength) || contentLength.equals("0") || contentLength.equals("-1")) {
            length = httpConnection.getContentLength();
            L.d(TAG, "parseResponse length:" + length);
        } else {
            length = Long.parseLong(contentLength);
        }

        if (length <= 0) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_INVALID_CONTENT_LENGTH,
                    "length <= 0");
        }

        checkCanceled();
        checkPaused();

        //Successful
        mStatus = DownloadStatus.STATUS_CONNECTED;
        final long timeDelta = System.currentTimeMillis() - mStartTime;
        mOnConnectListener.onConnected(timeDelta, length, isAcceptRanges);
    }

    private void checkCanceled() throws DownloadException {
        if (isCanceled()) {
            // cancel
            throw new DownloadException(DownloadStatus.STATUS_CANCELED,
                    DownloadException.ERROR_NORMAL_IO, "Download cancel!");
        }
    }

    private void checkPaused() throws DownloadException {
        if (isPaused()) {
            // cancel
            throw new DownloadException(DownloadStatus.STATUS_PAUSED,
                    DownloadException.ERROR_NORMAL_IO, "Download paused!");
        }
    }

    private boolean handleDownloadException(DownloadException e) {
        boolean recover = false;
        switch (e.getStatus()) {
            case DownloadStatus.STATUS_FAILED:
                // if the failure is unrecoverable or the recoverable count exceed the threshold
                if(!e.isRecoverable() || (mConnectRetryCount >= mConfig.getConnectAutoRecoverCount())) {
                    synchronized (mOnConnectListener) {
                        mStatus = DownloadStatus.STATUS_FAILED;
                        mOnConnectListener.onConnectFailed(e);
                    }
                } else {
                    recover = true;
                }
                break;
            case DownloadStatus.STATUS_CANCELED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_CANCELED;
                    mOnConnectListener.onConnectCanceled();
                }
                break;
            case DownloadStatus.STATUS_PAUSED:
                synchronized (mOnConnectListener) {
                    mStatus = DownloadStatus.STATUS_PAUSED;
                    mOnConnectListener.onConnectStopped();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
        return recover;
    }
}
