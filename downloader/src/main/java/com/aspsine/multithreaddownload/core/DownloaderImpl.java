package com.aspsine.multithreaddownload.core;


import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.aspsine.multithreaddownload.architecture.ConnectTask;
import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadTask;
import com.aspsine.multithreaddownload.architecture.Downloader;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.db.ThreadInfo;
import com.aspsine.multithreaddownload.util.L;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Created by Aspsine on 2015/10/28.
 */
public class DownloaderImpl implements Downloader, ConnectTask.OnConnectListener,
        DownloadTask.OnDownloadListener {
    private final static String TAG = "DownloaderImpl";

    private DownloadRequest mRequest;
    private DownloadResponse mResponse;
    private Executor mExecutor;
    private DataBaseManager mDBManager;
    private DownloadConfiguration mConfig;
    private OnDownloaderDestroyedListener mListener;
    private ConnectTask mConnectTask;
    private List<DownloadTask> mDownloadTasks;
    private DownloadInfo mDownloadInfo;
    private final long speedCalculateThreshold = 2000;
    private int speed;
    private long lastFinishedLength;
    private long lastSpeedBaseTimeMs;

    public DownloaderImpl(DownloadRequest request, DownloadResponse response, Executor executor,
                          DataBaseManager dbManager, DownloadConfiguration config,
                          OnDownloaderDestroyedListener listener, DownloadInfo downloadInfo) {
        mRequest = request;
        mResponse = response;
        mExecutor = executor;
        mDBManager = dbManager;
        mConfig = config;
        mListener = listener;
        mDownloadInfo = downloadInfo;

        mDownloadTasks = new LinkedList<>();
    }

    @Override
    public boolean isRunning() {
        // TODO: @SEAN redefine RUNNING status
        int status = mDownloadInfo.getStatus();
        return status == DownloadStatus.STATUS_STARTED
                || status == DownloadStatus.STATUS_CONNECTING
                || status == DownloadStatus.STATUS_CONNECTED
                || status == DownloadStatus.STATUS_PROGRESS;
    }

    @Override
    public boolean isPaused() {
        return mDownloadInfo.getStatus() == DownloadStatus.STATUS_PAUSED;
    }


    @Override
    public void start() {
        L.d(TAG, "start() taskId:" + mDownloadInfo.getId() +
                " from status:" + mDownloadInfo.getStatus());
        mDownloadInfo.setStatus(DownloadStatus.STATUS_STARTED);
        mDBManager.updateTask(mDownloadInfo);
        mResponse.onStarted();
        connect();
    }

    @Override
    public void pause() {
        if (mConnectTask != null) {
            mConnectTask.stop();
        }
        // pause task on connecting phase
        if (mDownloadTasks.size() == 0) {
            onDownloadPaused();
        } else {
            for (DownloadTask task : mDownloadTasks) {
                task.pause();
            }
        }
    }

    @Override
    public void autoPause() {
        if (mConnectTask != null) {
            mConnectTask.stop();
        }
        // auto pause task on connecting phase
        if (mDownloadTasks.size() == 0) {
            onDownloadAutoPaused();
        } else {
            for (DownloadTask task : mDownloadTasks) {
                task.autoPause();
            }
        }
    }

    @Override
    public void cancel() {
        if (mConnectTask != null) {
            mConnectTask.cancel();
        }
        for (DownloadTask task : mDownloadTasks) {
            task.cancel();
        }
    }

    @Override
    public void onDestroy() {
        // trigger the onDestroy callback tell download manager
        L.d(TAG, "onDestroy() id:" + mDownloadInfo.getId());
        mListener.onDestroyed(mDownloadInfo.getId(), this);
    }

    @Override
    public void onConnecting() {
        L.d(TAG, "onConnecting() id:" + mDownloadInfo.getId());
        mDownloadInfo.setStatus(DownloadStatus.STATUS_CONNECTING);
        mResponse.onConnecting();
        mDBManager.updateTask(mDownloadInfo);
    }

    @Override
    public void onConnected(long time, long length, boolean isAcceptRanges) {
        mDownloadInfo.setStatus(DownloadStatus.STATUS_CONNECTED);
        mResponse.onConnected(time, length, isAcceptRanges);

        mDownloadInfo.setAcceptRange(isAcceptRanges);
        mDownloadInfo.setTotalSize(length);
        L.d(TAG, "onConnected() id:" + mDownloadInfo.getId() + ", isAcceptRanges:" + isAcceptRanges);
        download(length, isAcceptRanges);
        mDBManager.updateTask(mDownloadInfo);

        // reset speed;
        speed = 0;
        lastFinishedLength = mDownloadInfo.getFinishedSize();
        lastSpeedBaseTimeMs = 0;
    }

    @Override
    public void onConnectFailed(DownloadException de) {
        L.d(TAG, "onConnecting() id:" + mDownloadInfo.getId() + ", de:" + de.getMessage());
        // TODO: @SEAN
        // recoverable failure --> retry
        // unrecoverable failure --> report failure
        mDownloadInfo.setStatus(DownloadStatus.STATUS_FAILED);
        mResponse.onConnectFailed(de);
        onDestroy();
        mDBManager.updateTask(mDownloadInfo);
    }

    @Override
    public void onConnectCanceled() {
        L.d(TAG, "onConnectCanceled() id:" + mDownloadInfo.getId());
        mDownloadInfo.setStatus(DownloadStatus.STATUS_CANCELED);
        mResponse.onConnectCanceled();
        onDestroy();
        mDBManager.updateTask(mDownloadInfo);
    }

    @Override
    public void onConnectStopped() {
        //onDownloadPaused();
        L.d(TAG, "onConnectStopped() id:" + mDownloadInfo.getId());
    }

    @Override
    public void onDownloadProgress(long finished, long length) {
        //L.d(TAG, "onDownloadProgress() id:" + mDownloadInfo.getId() +
                //", finished:" + finished + ", length:" + length);
        if (lastSpeedBaseTimeMs == 0) {
            lastSpeedBaseTimeMs = System.currentTimeMillis();
        }
        long elapsedTimeMs = System.currentTimeMillis() - lastSpeedBaseTimeMs;
        if (elapsedTimeMs >= speedCalculateThreshold) {
            speed = (int)((float)(finished - lastFinishedLength) * 1000 / speedCalculateThreshold);
            lastSpeedBaseTimeMs = elapsedTimeMs + lastSpeedBaseTimeMs;
            lastFinishedLength = finished;
        }
        if (mDownloadInfo.getStatus() != DownloadStatus.STATUS_PROGRESS) {
            mDownloadInfo.setStatus(DownloadStatus.STATUS_PROGRESS);
        }
        mDBManager.updateTask(mDownloadInfo);
        // calculate percent
        final int percent = (int) (finished * 100 / length);
        mResponse.onDownloadProgress(finished, length, percent, speed);
    }

    @Override
    public void onDownloadCompleted() {
        boolean isAllComplete = isAllComplete();
        L.d(TAG, "onDownloadCompleted() id:" + mDownloadInfo.getId() +
                ", isAllComplete:" + isAllComplete);
        if (isAllComplete) {
            deleteFromDB();
            mDownloadInfo.setFinishedTime(System.currentTimeMillis());
            mDownloadInfo.setStatus(DownloadStatus.STATUS_COMPLETED);
            mDBManager.updateTask(mDownloadInfo);
            mResponse.onDownloadCompleted();
            onDestroy();
        }
    }

    @Override
    public void onDownloadPaused() {
        boolean isAllPaused = isAllPaused();
        L.d(TAG, "onDownloadPaused() id:" + mDownloadInfo.getId() +
                ", isAllPaused:" + isAllPaused);
        if (isAllPaused) {
            mDownloadInfo.setStatus(DownloadStatus.STATUS_PAUSED);
            mResponse.onDownloadPaused();
            onDestroy();
            mDBManager.updateTask(mDownloadInfo);
        }
    }

    @Override
    public void onDownloadAutoPaused() {
        boolean isAllAutoPaused = isAllAutoPaused();
        L.d(TAG, "onDownloadAutoPaused() id:" + mDownloadInfo.getId() +
                ", isAllAutoPaused:" + isAllAutoPaused);
        if (isAllAutoPaused) {
            mDownloadInfo.setStatus(DownloadStatus.STATUS_AUTO_PAUSED);
            mResponse.onDownloadAutoPaused();
            onDestroy();
            mDBManager.updateTask(mDownloadInfo);
        }
    }

    @Override
    public void onDownloadCanceled() {
        boolean isAllCanceled = isAllCanceled();
        L.d(TAG, "onDownloadCanceled() id:" + mDownloadInfo.getId() +
                ", isAllCanceled:" + isAllCanceled);
        if (isAllCanceled) {
            deleteFromDB();
            mDownloadInfo.setStatus(DownloadStatus.STATUS_CANCELED);
            mResponse.onDownloadCanceled();
            onDestroy();
            mDBManager.updateTask(mDownloadInfo);
        }
    }

    @Override
    public void onDownloadFailed(DownloadException de) {
        boolean isAllFailed = isAllFailed();
        L.d(TAG, "onDownloadFailed() id:" + mDownloadInfo.getId() +
                ", isAllFailed:" + isAllFailed + ", e:" + de.getMessage());
        if (isAllFailed) {
            mDownloadInfo.setStatus(DownloadStatus.STATUS_FAILED);
            mResponse.onDownloadFailed(de);
            onDestroy();
            mDBManager.updateTask(mDownloadInfo);
        }
    }

    private void connect() {
        L.d(TAG, "connect() taskId:" + mDownloadInfo.getId());
        mConnectTask = new ConnectTaskImpl(mRequest.getUri(), mConfig, this);
        mExecutor.execute(mConnectTask);
    }

    private void download(long length, boolean acceptRanges) {
        L.d(TAG, "download() taskId:" + mDownloadInfo.getId() +
                ", acceptRanges:" + acceptRanges);
        initDownloadTasks(length, acceptRanges);
        // start tasks
        for (DownloadTask downloadTask : mDownloadTasks) {
            mExecutor.execute(downloadTask);
        }
    }

    private void initDownloadTasks(long length, boolean acceptRanges) {
        L.d(TAG, "initDownloadTasks() taskId:" + mDownloadInfo.getId() +
                ", acceptRanges:" + acceptRanges);
        mDownloadTasks.clear();
        if (acceptRanges) {
            List<ThreadInfo> threadInfoList = getMultiThreadInfoList(length);
            for (ThreadInfo info : threadInfoList) {
                L.i(TAG, "initDownloadTasks() segment start from:" + info.getStart() +
                        ", end:" + info.getEnd() + ", finished size:" + info.getFinished());
                if (!info.isFinished()) {
                    mDownloadTasks.add(new MultiDownloadTask(mDownloadInfo, info, mConfig,
                            mDBManager, this));
                }
            }
        } else {
            L.i(TAG, "initDownloadTasks() singleThreadTask");
            ThreadInfo info = getSingleThreadInfo();
            // TODO: for single task, always start/resume from 0
            info.setFinished(0);
            mDBManager.update(info.getTag(), info.getId(), info.getFinished());
            mDownloadInfo.setFinishedSize(0);
            mDBManager.updateTask(mDownloadInfo);
            if (!info.isFinished()) {
                mDownloadTasks.add(new SingleDownloadTask(mDownloadInfo, info, mConfig,
                        mDBManager, this));
            }
        }
    }

    private List<ThreadInfo> getMultiThreadInfoList(long length) {
        final List<ThreadInfo> threadInfoList = mDBManager.getThreadInfos(mDownloadInfo.getId());
        L.d(TAG, "getMultiThreadInfoList() threadInfo size in db:" + threadInfoList.size());
        if (threadInfoList.isEmpty()) {
            final int threadNum = mConfig.getThreadNum();
            for (int i = 0; i < threadNum; i++) {
                // calculate average
                final long average = length / threadNum;
                final long start = average * i;
                final long end;
                if (i == threadNum - 1) {
                    end = length - 1;
                } else {
                    end = start + average - 1;
                }
                ThreadInfo threadInfo = new ThreadInfo(i, mDownloadInfo.getId(), mRequest.getUri(), start, end, 0);
                // Note: save thread info
                if (!mDBManager.exists(threadInfo.getTag(), threadInfo.getId())) {
                    mDBManager.insert(threadInfo);
                }
                threadInfoList.add(threadInfo);
            }
        }
        return threadInfoList;
    }

    private ThreadInfo getSingleThreadInfo() {
        final List<ThreadInfo> threadInfoList = mDBManager.getThreadInfos(mDownloadInfo.getId());
        L.d(TAG, "getSingleThreadInfo() threadInfo size" + threadInfoList.size());
        if (threadInfoList.isEmpty()) {
            ThreadInfo threadInfo = new ThreadInfo(0, mDownloadInfo.getId(), mRequest.getUri(), 0);
            if (!mDBManager.exists(threadInfo.getTag(), threadInfo.getId())) {
                mDBManager.insert(threadInfo);
            }
            return threadInfo;
        } else {
            return threadInfoList.get(0);
        }
    }

    private boolean isAllComplete() {
        boolean allFinished = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isComplete()) {
                allFinished = false;
                break;
            }
        }
        return allFinished;
    }

    private boolean isAllFailed() {
        boolean allFailed = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allFailed = false;
                break;
            }
        }
        return allFailed;
    }

    private boolean isAllPaused() {
        boolean allPaused = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allPaused = false;
                break;
            }
        }
        return allPaused;
    }

    private boolean isAllAutoPaused() {
        boolean allPaused = true;
        for (DownloadTask task : mDownloadTasks) {
            if (!task.isAutoPaused()) {
                allPaused = false;
                break;
            }
        }
        return allPaused;
    }

    private boolean isAllCanceled() {
        boolean allCanceled = true;
        for (DownloadTask task : mDownloadTasks) {
            if (task.isDownloading()) {
                allCanceled = false;
                break;
            }
        }
        return allCanceled;
    }

    private void deleteFromDB() {
        mDBManager.delete(mDownloadInfo.getId());
    }
}
