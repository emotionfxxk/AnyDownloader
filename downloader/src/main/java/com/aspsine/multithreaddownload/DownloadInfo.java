package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.architecture.DownloadStatus;

import java.io.Serializable;
import java.lang.ref.WeakReference;

/**
 * Created by sean on 16-7-8.
 */
public class DownloadInfo implements Serializable, Comparable<DownloadInfo> {
    private String id;
    private String url;
    private String originalFileName;
    private String actualFileName;
    private String mimeType;
    private String savePath;
    private int speed;
    private long totalSize;
    private long finishedSize;
    private int status;
    private String thumbnailUrl;
    private long startTime;
    private long finishedTime;
    private boolean acceptRange;
    private int notificationId;

    public DownloadInfo() {
    }

    private WeakReference<Updater> updaterWeakReference;

    @Override
    public int compareTo(DownloadInfo downloadInfo) {
        if (finishedTime != 0) {
            return (int) (downloadInfo.finishedTime - finishedTime);
        } else {
            return (int) (downloadInfo.startTime - startTime);
        }
    }

    public interface Updater {
        void onUpdate(DownloadInfo dlTask);
    }

    public void bindUpdater(Updater updater) {
        updaterWeakReference = new WeakReference<>(updater);
    }

    public synchronized int getSpeed() {
        return speed;
    }
    public synchronized void setSpeed(int speed) {
        this.speed = speed;
        notifyUpdater();
    }
    public synchronized String getId() {
        return id;
    }
    public synchronized void setId(String id) {
        this.id = id;
    }
    public synchronized String getUrl() {
        return url;
    }
    public synchronized void setUrl(String url) {
        this.url = url;
    }
    public synchronized String getOriginalFileName() {
        return originalFileName;
    }
    public synchronized void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }
    public synchronized String getActualFileName() {
        return actualFileName;
    }
    public synchronized void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }
    public synchronized String getMimeType() {
        return mimeType;
    }
    public synchronized void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public synchronized String getSavePath() {
        return savePath;
    }
    public synchronized void setSavePath(String savePath) {
        this.savePath = savePath;
    }
    public synchronized long getTotalSize() {
        return totalSize;
    }
    public synchronized void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
        notifyUpdater();
    }
    public synchronized long getFinishedSize() {
        return finishedSize;
    }
    public synchronized void setFinishedSize(long finishedSize) {
        this.finishedSize = finishedSize;
    }
    public synchronized void accumulateFinishedSize(long downloadedSize) {
        this.finishedSize += downloadedSize;
        notifyUpdater();
    }
    public synchronized int getStatus() {
        return status;
    }
    public synchronized void setStatus(int status) {
        this.status = status;
        notifyUpdater();
    }
    public synchronized String getThumbnailUrl() {
        return thumbnailUrl;
    }
    public synchronized void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    public synchronized long getStartTime() {
        return startTime;
    }
    public synchronized void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public synchronized long getFinishedTime() {
        return finishedTime;
    }
    public synchronized void setFinishedTime(long finishedTime) {
        this.finishedTime = finishedTime;
        notifyUpdater();
    }
    public synchronized boolean getAcceptRange() {
        return acceptRange;
    }
    public synchronized void setAcceptRange(boolean acceptRange) {
        this.acceptRange = acceptRange;
    }

    public synchronized int getNotificationId() {
        return notificationId;
    }
    public synchronized void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }
    @Override
    public String toString() {
        return "@File Name <<" + actualFileName + ">> @From url <<" + url + ">>";
    }

    public int getAverageSpeed() {
        return (int) (totalSize * 1000 / (finishedTime - startTime));
    }
    @Override
    public boolean equals(Object object) {
        return (object != null) && (object instanceof DownloadInfo) &&
                ((DownloadInfo)object).getId().equals(id);
    }

    private void notifyUpdater() {
        if (updaterWeakReference != null) {
            Updater updater = updaterWeakReference.get();
            if (updater != null) {
                updater.onUpdate(this);
            }
        }
    }

    public boolean isPaused() {
        return status == DownloadStatus.STATUS_PAUSED;
    }

    public boolean isAutoPaused() {
        return status == DownloadStatus.STATUS_AUTO_PAUSED;
    }

    public boolean isFailed() {
        return status == DownloadStatus.STATUS_FAILED;
    }
}
