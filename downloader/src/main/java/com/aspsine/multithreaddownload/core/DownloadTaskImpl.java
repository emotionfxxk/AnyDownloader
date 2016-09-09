package com.aspsine.multithreaddownload.core;


import android.os.Process;
import com.aspsine.multithreaddownload.Constants.HTTP;
import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadTask;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.db.ThreadInfo;
import com.aspsine.multithreaddownload.util.IOCloseUtils;
import com.aspsine.multithreaddownload.util.L;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * Created by Aspsine on 2015/7/27.
 */
public abstract class DownloadTaskImpl implements DownloadTask {
    private final String TAG = "DownloadTaskImpl";

    private final DownloadInfo mDownloadInfo;
    private final ThreadInfo mThreadInfo;
    private final OnDownloadListener mOnDownloadListener;
    private InputStream mInputStream;
    private DownloadConfiguration mConfig;
    private int mDownloadRetryCount;

    private volatile int mStatus;

    private volatile int mCommend = 0;

    public DownloadTaskImpl(DownloadInfo downloadInfo, ThreadInfo threadInfo,
                            DownloadConfiguration config, OnDownloadListener listener) {
        this.mDownloadInfo = downloadInfo;
        this.mThreadInfo = threadInfo;
        this.mConfig = config;
        this.mOnDownloadListener = listener;
        L.d(TAG, "DownloadTaskImpl()  task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId() +
                ", start:" + mThreadInfo.getStart() + ", finished Size:" + mThreadInfo.getFinished());
    }

    @Override
    public void cancel() {
        L.d(TAG, "cancel()  task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId());
        mCommend = DownloadStatus.STATUS_CANCELED;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            L.w(TAG, "cancel() exception:" + e.getMessage());
        }
    }

    @Override
    public void pause() {
        L.d(TAG, "pause()  task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId());
        mCommend = DownloadStatus.STATUS_PAUSED;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            L.w(TAG, "pause() exception:" + e.getMessage());
        }
    }

    @Override
    public void autoPause() {
        L.d(TAG, "pause()  task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId());
        mCommend = DownloadStatus.STATUS_AUTO_PAUSED;
        try {
            if (mInputStream != null) {
                mInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            L.w(TAG, "pause() exception:" + e.getMessage());
        }
    }

    @Override
    public boolean isDownloading() {
        return mStatus == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public boolean isComplete() {
        return mStatus == DownloadStatus.STATUS_COMPLETED;
    }

    @Override
    public boolean isPaused() {
        return mStatus == DownloadStatus.STATUS_PAUSED;
    }

    @Override
    public boolean isAutoPaused() {
        return mStatus == DownloadStatus.STATUS_AUTO_PAUSED;
    }

    @Override
    public boolean isCanceled() {
        return mStatus == DownloadStatus.STATUS_CANCELED;
    }

    @Override
    public boolean isFailed() {
        return mStatus == DownloadStatus.STATUS_FAILED;
    }

    @Override
    public void run() {
        L.d(TAG, "run() task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId());
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        insertIntoDB(mThreadInfo);
        mStatus = DownloadStatus.STATUS_PROGRESS;
        mDownloadRetryCount = 0;
        while (mDownloadRetryCount++ < mConfig.getDownloadAutoRecoverCount()) {
            L.d(TAG, "run() retryCount:" + mDownloadRetryCount);
            try {
                executeDownload();
                synchronized (mOnDownloadListener) {
                    L.d(TAG, "run() task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId() +
                            "task completed!!!!");
                    mStatus = DownloadStatus.STATUS_COMPLETED;
                    mOnDownloadListener.onDownloadCompleted();
                }
                break;
            } catch (DownloadException e) {
                if (!handleDownloadException(e)) {
                    break;
                }
            }
        }
    }

    private boolean handleDownloadException(DownloadException e) {
        boolean recover = false;
        switch (e.getStatus()) {
            case DownloadStatus.STATUS_FAILED:
                // if the failure is unrecoverable or the recoverable count exceed the threshold
                if(!e.isRecoverable() ||
                        (mDownloadRetryCount >= mConfig.getDownloadAutoRecoverCount())) {
                    synchronized (mOnDownloadListener) {
                        mStatus = DownloadStatus.STATUS_FAILED;
                        mOnDownloadListener.onDownloadFailed(e);
                    }
                } else {
                    recover = true;
                }
                break;
            case DownloadStatus.STATUS_PAUSED:
                synchronized (mOnDownloadListener) {
                    mStatus = DownloadStatus.STATUS_PAUSED;
                    mOnDownloadListener.onDownloadPaused();
                }
                break;
            case DownloadStatus.STATUS_AUTO_PAUSED:
                synchronized (mOnDownloadListener) {
                    mStatus = DownloadStatus.STATUS_AUTO_PAUSED;
                    mOnDownloadListener.onDownloadAutoPaused();
                }
                break;
            case DownloadStatus.STATUS_CANCELED:
                synchronized (mOnDownloadListener) {
                    mStatus = DownloadStatus.STATUS_CANCELED;
                    mOnDownloadListener.onDownloadCanceled();
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown state");
        }
        return recover;
    }

    private void executeDownload() throws DownloadException {
        L.d(TAG, "executeDownload()  task id:" + mDownloadInfo.getId() + ", tid:" + mThreadInfo.getId());
        final URL url;
        try {
            url = new URL(mThreadInfo.getUri());
        } catch (MalformedURLException e) {
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_URL_MAL_FORMAT, "Bad url.", e);
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) url.openConnection();
            httpConnection.setConnectTimeout(HTTP.CONNECT_TIME_OUT);
            httpConnection.setReadTimeout(HTTP.READ_TIME_OUT);
            httpConnection.setRequestMethod(HTTP.GET);
            setHttpHeader(getHttpHeaders(mThreadInfo), httpConnection);
            final int responseCode = httpConnection.getResponseCode();
            if (responseCode == getResponseCode()) {
                transferData(httpConnection);
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
            throw new DownloadException(DownloadStatus.STATUS_FAILED,
                    DownloadException.ERROR_NORMAL_IO,
                    "IO error", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    private void setHttpHeader(Map<String, String> headers, URLConnection connection) {
        if (headers != null) {
            for (String key : headers.keySet()) {
                connection.setRequestProperty(key, headers.get(key));
            }
        }
    }
    private void transferData(HttpURLConnection httpConnection) throws DownloadException {
        mInputStream = null;
        RandomAccessFile raf = null;
        try {
            try {
                mInputStream = httpConnection.getInputStream();
            } catch (IOException e) {
                throw new DownloadException(DownloadStatus.STATUS_FAILED,
                        DownloadException.ERROR_FAILED_GET_INPUT_STREAM,
                        "http get inputStream error", e);
            }
            final long offset = mThreadInfo.getStart() + mThreadInfo.getFinished();
            try {
                raf = getFile(new File(mDownloadInfo.getSavePath()), mDownloadInfo.getActualFileName(), offset);
            } catch (IOException e) {
                throw new DownloadException(DownloadStatus.STATUS_FAILED,
                        DownloadException.ERROR_FILE_IO,
                        "File error", e);
            }
            transferData(mInputStream, raf);
        } finally {
            try {
                IOCloseUtils.close(mInputStream);
                IOCloseUtils.close(raf);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void transferData(InputStream inputStream, RandomAccessFile raf) throws DownloadException {
        final byte[] buffer = new byte[1024 * 16];
        while (true) {
            checkPausedOrCanceled();
            int len = -1;
            try {
                len = inputStream.read(buffer);
            } catch (IOException e) {
                if (mCommend == DownloadStatus.STATUS_PAUSED ||
                        mCommend == DownloadStatus.STATUS_AUTO_PAUSED) {
                    // pause or auto paused
                    updateDB(mThreadInfo);
                    throw new DownloadException(mCommend,
                            DownloadException.ERROR_NORMAL_IO,
                            "Download paused!");
                } else {
                    throw new DownloadException(DownloadStatus.STATUS_FAILED,
                            DownloadException.ERROR_NORMAL_IO,
                            "Http inputStream read error", e);
                }
            }

            if (len == -1) {
                L.d(TAG, "transferData len=" + len + ", reach the end of the stream!!!");
                break;
            }

            try {
                raf.write(buffer, 0, len);
                mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
                updateDB(mThreadInfo);
                synchronized (mOnDownloadListener) {
                    //long oldFinishedSize = mDownloadInfo.getFinishedSize();
                    mDownloadInfo.accumulateFinishedSize(len);
                    //long newFinishedSize = mDownloadInfo.getFinishedSize();
                    //L.d(TAG, "total=" + mDownloadInfo.getTotalSize() + ", oldSize=" + oldFinishedSize +
                            //", newSize=" + newFinishedSize +
                            //", len:" + len);
                    mOnDownloadListener.onDownloadProgress(mDownloadInfo.getFinishedSize(),
                            mDownloadInfo.getTotalSize());
                }
            } catch (IOException e) {
                if (mCommend == DownloadStatus.STATUS_PAUSED ||
                        mCommend == DownloadStatus.STATUS_AUTO_PAUSED) {
                    // pause or auto pause
                    updateDB(mThreadInfo);
                    throw new DownloadException(mCommend,
                            DownloadException.ERROR_NORMAL_IO,
                            "Download paused!");
                } else {
                    throw new DownloadException(DownloadStatus.STATUS_FAILED,
                            DownloadException.ERROR_NORMAL_IO,
                            "Fail write buffer to file", e);
                }
            }
        }
    }


    private void checkPausedOrCanceled() throws DownloadException {
        if (mCommend == DownloadStatus.STATUS_CANCELED) {
            // cancel
            throw new DownloadException(DownloadStatus.STATUS_CANCELED,
                    DownloadException.ERROR_NORMAL_IO,
                    "Download canceled!");
        } else if (mCommend == DownloadStatus.STATUS_PAUSED ||
                mCommend == DownloadStatus.STATUS_AUTO_PAUSED) {
            // pause or auto pause
            updateDB(mThreadInfo);
            throw new DownloadException(mCommend,
                    DownloadException.ERROR_NORMAL_IO,
                    "Download paused!");
        }
    }


    protected abstract void insertIntoDB(ThreadInfo info);

    protected abstract int getResponseCode();

    protected abstract void updateDB(ThreadInfo info);

    protected abstract Map<String, String> getHttpHeaders(ThreadInfo info);

    protected abstract RandomAccessFile getFile(File dir, String name, long offset) throws IOException;
}