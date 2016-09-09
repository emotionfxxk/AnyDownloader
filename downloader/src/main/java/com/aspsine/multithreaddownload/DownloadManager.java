package com.aspsine.multithreaddownload;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadStatusDelivery;
import com.aspsine.multithreaddownload.architecture.Downloader;
import com.aspsine.multithreaddownload.core.DownloadResponseImpl;
import com.aspsine.multithreaddownload.core.DownloadStatusDeliveryImpl;
import com.aspsine.multithreaddownload.core.DownloaderImpl;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.util.L;
import com.aspsine.multithreaddownload.util.MD5;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadManager implements Downloader.OnDownloaderDestroyedListener {

    public static final String TAG = "DownloadManager";

    /**
     * singleton of DownloadManager
     */
    private static DownloadManager sDownloadManager;

    private DataBaseManager mDBManager;

    private Map<String, Downloader> mDownloaderMap;

    private DownloadConfiguration mConfig;

    private ExecutorService mExecutorService;

    private DownloadStatusDelivery mDelivery;

    // extension
    private CallBack defaultCallback;
    private List<DownloadInfo> finishedDownloadTasks;
    private List<DownloadInfo> unfinishedDownloadTasks;
    private CallBack proxyCallback = new CallBack() {
        @Override
        public void onStarted(DownloadInfo downloadInfo) {
            if (defaultCallback != null) defaultCallback.onStarted(downloadInfo);
        }
        @Override
        public void onConnecting(DownloadInfo downloadInfo) {
            if (defaultCallback != null) defaultCallback.onConnecting(downloadInfo);
        }
        @Override
        public void onConnected(DownloadInfo downloadInfo, long total, boolean isRangeSupport) {
            if (defaultCallback != null) defaultCallback.onConnected(downloadInfo, total, isRangeSupport);
        }
        @Override
        public void onProgress(DownloadInfo downloadInfo, long finished, long total, int progress, int speed) {
            if (defaultCallback != null)
                defaultCallback.onProgress(downloadInfo, finished, total, progress, speed);
        }
        @Override
        public void onCompleted(DownloadInfo downloadInfo) {
            L.d(TAG, "onCompleted:" + downloadInfo.getId());
            if (defaultCallback != null) defaultCallback.onCompleted(downloadInfo);
            unfinishedDownloadTasks.remove(downloadInfo);
            finishedDownloadTasks.add(0, downloadInfo);
        }
        @Override
        public void onDownloadPaused(DownloadInfo downloadInfo) {
            if (defaultCallback != null) defaultCallback.onDownloadPaused(downloadInfo);
        }
        @Override
        public void onDownloadAutoPaused(DownloadInfo downloadInfo) {
            if (defaultCallback != null) defaultCallback.onDownloadAutoPaused(downloadInfo);
        }
        @Override
        public void onDownloadCanceled(DownloadInfo downloadInfo) {
            if (defaultCallback != null) defaultCallback.onDownloadCanceled(downloadInfo);
            // TODO: remove
        }
        @Override
        public void onFailed(DownloadInfo downloadInfo, DownloadException e) {
            if (defaultCallback != null) defaultCallback.onFailed(downloadInfo, e);
        }
    };

    public static DownloadManager getInstance() {
        if (sDownloadManager == null) {
            synchronized (DownloadManager.class) {
                sDownloadManager = new DownloadManager();
            }
        }
        return sDownloadManager;
    }

    /**
     * private construction
     */
    private DownloadManager() {
        mDownloaderMap = new LinkedHashMap<>();
    }

    public void init(Context context) {
        init(context, new DownloadConfiguration());
    }

    public void init(Context context, DownloadConfiguration config) {
        if (config.getThreadNum() > config.getMaxThreadNum()) {
            throw new IllegalArgumentException("thread num must < max thread num");
        }
        mConfig = config;
        mDBManager = DataBaseManager.getInstance(context);
        mExecutorService = Executors.newFixedThreadPool(mConfig.getMaxThreadNum());
        mDelivery = new DownloadStatusDeliveryImpl(new Handler(Looper.getMainLooper()));
        L.d(TAG, "init() ... threadNum:" + mConfig.getThreadNum() +
                ", pool size:" + mConfig.getMaxThreadNum());
        initTasks();
    }

    @Override
    public void onDestroyed(String key, Downloader downloader) {
        if (mDownloaderMap.containsKey(key)) {
            mDownloaderMap.remove(key);
        }
    }

    /**
     * record download task but not start immediately
     * use in WIFI only mode(wifi is not connected)
     * @param request
     * @param taskId
     */
    public void bookDownload(DownloadRequest request, String taskId) {
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.setId(taskId);
        downloadInfo.setUrl(request.getUri());
        downloadInfo.setOriginalFileName(request.getOriginalName());
        downloadInfo.setActualFileName(request.getActualName());
        downloadInfo.setMimeType(request.getMimeType());
        downloadInfo.setThumbnailUrl(request.getThumbnailUrl());
        downloadInfo.setSavePath(request.getSavePath());
        downloadInfo.setStartTime(new Date().getTime());
        downloadInfo.setFinishedTime(0);
        downloadInfo.setTotalSize(request.getContentLength());
        downloadInfo.setFinishedSize(0);
        downloadInfo.setStatus(DownloadStatus.STATUS_AUTO_PAUSED);
        mDBManager.insertTask(downloadInfo);
        unfinishedDownloadTasks.add(0, downloadInfo);
        L.d(TAG, "bookDownload() book download id:" + downloadInfo.getId());
        proxyCallback.onStarted(downloadInfo);
    }

    public void download(DownloadRequest request, String taskId,
                         DownloadInfo downloadInfo) {
        if (downloadInfo == null) {
            downloadInfo = new DownloadInfo();
            downloadInfo.setId(taskId);
            downloadInfo.setUrl(request.getUri());
            downloadInfo.setOriginalFileName(request.getOriginalName());
            downloadInfo.setActualFileName(request.getActualName());
            downloadInfo.setMimeType(request.getMimeType());
            downloadInfo.setThumbnailUrl(request.getThumbnailUrl());
            downloadInfo.setSavePath(request.getSavePath());
            downloadInfo.setStartTime(new Date().getTime());
            downloadInfo.setFinishedTime(0);
            downloadInfo.setTotalSize(request.getContentLength());
            downloadInfo.setFinishedSize(0);
            downloadInfo.setStatus(DownloadStatus.STATUS_STARTED);
            mDBManager.insertTask(downloadInfo);
            unfinishedDownloadTasks.add(0, downloadInfo);
        }
        DownloadResponse response = new DownloadResponseImpl(downloadInfo, mDelivery, proxyCallback);
        Downloader downloader = new DownloaderImpl(request, response, mExecutorService,
                mDBManager, mConfig, this, downloadInfo);
        mDownloaderMap.put(taskId, downloader);
        L.d(TAG, "download() start download id:" + downloadInfo.getId());
        downloader.start();
    }

    private void initTasks() {
        finishedDownloadTasks = mDBManager.getFinishedDownloadTasks();
        unfinishedDownloadTasks = mDBManager.getUnfinishedDownloadTasks();
        for (DownloadInfo downloadInfo: finishedDownloadTasks) {
            L.d(TAG, "initTasks() finished downloadInfo:" + downloadInfo.getId());
        }
        for (DownloadInfo downloadInfo: unfinishedDownloadTasks) {
            L.d(TAG, "initTasks() unfinished downloadInfo:" + downloadInfo.getId());
        }
    }
    public void setDownloadCallback(CallBack callback) {
        this.defaultCallback = callback;
    }

    public void resumeDownloadTask() {
        if (defaultCallback == null) {
            throw new IllegalStateException("defaultCallback should be set before resumeTask");
        }
        for (DownloadInfo downloadInfo : unfinishedDownloadTasks) {
            int status = downloadInfo.getStatus();
            // TODO: what about DownloadStatus.STATUS_FAILED?
            L.d(TAG, "resumeDownloadTask() try to resume downloadInfo:" + downloadInfo.getId() +
                    ", with status:" + status);
            if (status != DownloadStatus.STATUS_CANCELED &&
                    status != DownloadStatus.STATUS_COMPLETED &&
                    status != DownloadStatus.STATUS_PAUSED) {
                L.d(TAG, "resumeDownloadTask() resume >>>> task id:" + downloadInfo.getId());
                resumeDownload(downloadInfo);
            }
        }
    }

    private void resumeDownload(DownloadInfo downloadInfo) {
        final DownloadRequest request = new DownloadRequest.Builder()
                .setUri(downloadInfo.getUrl())
                .setMimeType(downloadInfo.getMimeType())
                .setActualName(downloadInfo.getActualFileName())
                .setOriginalName(downloadInfo.getOriginalFileName())
                .setSavePath(downloadInfo.getSavePath())
                .setScannable(true)
                .setThumbUrl(null)
                .build();
        L.d(TAG, "resumeDownload() resume Task id:" + downloadInfo.getId());
        download(request, downloadInfo.getId(), downloadInfo);
    }

    public void pause(String taskId) {
        L.d(TAG, "pause() taskId:" + taskId);
        if (mDownloaderMap.containsKey(taskId)) {
            Downloader downloader = mDownloaderMap.get(taskId);
            if (downloader != null) {
                L.d(TAG, "pause(): isRunning:" + downloader.isRunning());
                if (downloader.isRunning()) {
                    downloader.pause();
                }
            }
        }
    }

    public void autoPause() {
        L.d(TAG, "autoPause()");
        for (DownloadInfo downloadInfo : unfinishedDownloadTasks) {
            Downloader downloader = mDownloaderMap.get(downloadInfo.getId());
            if (downloader != null) {
                L.d(TAG, "autoPause(): isRunning:" + downloader.isRunning());
                if (downloader.isRunning()) {
                    downloader.autoPause();
                }
            } else {
                downloadInfo.setStatus(DownloadStatus.STATUS_AUTO_PAUSED);
                mDBManager.updateTask(downloadInfo);
            }
        }
    }

    public void resume(String taskId) {
        L.d(TAG, "resume() taskId:" + taskId);
        if (mDownloaderMap.containsKey(taskId)) {
            Downloader downloader = mDownloaderMap.get(taskId);
            L.d(TAG, "resume() downloader exist! taskId:" + taskId);
            if (downloader != null && downloader.isPaused()) {
                L.d(TAG, "resume() indeed! taskId:" + taskId);
                downloader.start();
            }
        } else {
            for (DownloadInfo downloadInfo: unfinishedDownloadTasks) {
                if (downloadInfo.getId().equals(taskId)) {
                    L.d(TAG, "resume() create new downloader,  taskId:" + taskId);
                    resumeDownload(downloadInfo);
                    break;
                }
            }
        }
    }

    public void cancel(String taskId) {
        L.d(TAG, "cancel() taskId:" + taskId);
        if (mDownloaderMap.containsKey(taskId)) {
            L.d(TAG, "cancel() indeed !!!! taskId:" + taskId);
            Downloader downloader = mDownloaderMap.get(taskId);
            if (downloader != null) {
                downloader.cancel();
            }
            mDownloaderMap.remove(taskId);
            removeDownloadTask(taskId, false);
        }
    }

    public void cancel(String[] taskIds, boolean removeFileAlso) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String taskId : taskIds) {
            sb.append(taskId).append(",");
        }
        sb.append("]");
        L.d(TAG, "cancel() taskIds:" + sb.toString() + ", removeFileAlso:" + removeFileAlso);
        for (String taskId : taskIds) {
            sb.append(taskId).append(",");
            if (mDownloaderMap.containsKey(taskId)) {
                Downloader downloader = mDownloaderMap.get(taskId);
                if (downloader != null) {
                    downloader.cancel();
                }
                mDownloaderMap.remove(taskId);
            }
            removeDownloadTask(taskId, removeFileAlso);
        }
    }

    public void pauseAll() {
        L.d(TAG, "pauseAll()");
        for (Downloader downloader : mDownloaderMap.values()) {
            if (downloader != null) {
                L.d(TAG, "pauseAll(): isRunning:" + downloader.isRunning());
                if (downloader.isRunning()) {
                    downloader.pause();
                }
            }
        }
    }

    public List<DownloadInfo> getFinishedDownloadTasks() {
        List<DownloadInfo> snapShot = new ArrayList<>();
        snapShot.addAll(finishedDownloadTasks);
        return snapShot;
    }
    public List<DownloadInfo> getUnfinishedDownloadTasks() {
        List<DownloadInfo> snapShot = new ArrayList<>();
        snapShot.addAll(unfinishedDownloadTasks);
        return snapShot;
    }

    private void removeDownloadTask(String taskId, boolean removeFileAlso) {
        L.d(TAG, "removeDownloadTask() taskId:" + taskId + ", removeFileAlso:" + removeFileAlso);
        mDBManager.deleteTask(taskId);
        mDBManager.delete(taskId);
        DownloadInfo downloadInfo = null;
        for (int index = 0; index < unfinishedDownloadTasks.size(); ++index) {
            if (unfinishedDownloadTasks.get(index).getId().equals(taskId)) {
                downloadInfo = unfinishedDownloadTasks.get(index);
                unfinishedDownloadTasks.remove(index);
                if (removeFileAlso) {
                    removeDownloadFile(downloadInfo);
                }
                return;
            }
        }

        for (int index = 0; index < finishedDownloadTasks.size(); ++index) {
            if (finishedDownloadTasks.get(index).getId().equals(taskId)) {
                downloadInfo = finishedDownloadTasks.get(index);
                finishedDownloadTasks.remove(index);
                if (removeFileAlso) {
                    removeDownloadFile(downloadInfo);
                }
                return;
            }
        }
    }

    private void removeDownloadFile(DownloadInfo downloadInfo) {
        try {
            File file = new File(downloadInfo.getSavePath() + File.separator +
                    downloadInfo.getActualFileName());
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
            L.w(TAG, "removeDownloadFile e:" + e.getMessage());
        }
    }

    public static String genTaskId(String url) {
        return MD5.getMD5(System.currentTimeMillis() + url);
    }
}
