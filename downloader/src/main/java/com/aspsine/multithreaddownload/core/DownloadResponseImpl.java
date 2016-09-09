package com.aspsine.multithreaddownload.core;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.DownloadResponse;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadStatusDelivery;
import com.aspsine.multithreaddownload.DownloadInfo;

/**
 * Created by Aspsine on 2015/10/29.
 */
public class DownloadResponseImpl implements DownloadResponse {
    private DownloadStatusDelivery mDelivery;

    private DownloadStatus mDownloadStatus;
    private DownloadInfo mDownloadInfo;

    public DownloadResponseImpl(DownloadInfo downloadInfo, DownloadStatusDelivery delivery,
                                CallBack callBack) {
        mDownloadInfo = downloadInfo;
        mDelivery = delivery;
        mDownloadStatus = new DownloadStatus();
        mDownloadStatus.setCallBack(callBack);
    }

    @Override
    public void onStarted() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_STARTED);
        mDownloadStatus.getCallBack().onStarted(mDownloadInfo);
    }

    @Override
    public void onConnecting() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CONNECTING);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onConnected(long time, long length, boolean acceptRanges) {
        mDownloadStatus.setTime(time);
        mDownloadStatus.setAcceptRanges(acceptRanges);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CONNECTED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onConnectFailed(DownloadException e) {
        mDownloadStatus.setException(e);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_FAILED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onConnectCanceled() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CANCELED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadProgress(long finished, long length, int percent, int speed) {
        mDownloadStatus.setFinished(finished);
        mDownloadStatus.setLength(length);
        mDownloadStatus.setPercent(percent);
        mDownloadStatus.setSpeed(speed);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PROGRESS);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadCompleted() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_COMPLETED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadPaused() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_PAUSED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadAutoPaused() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_AUTO_PAUSED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadCanceled() {
        mDownloadStatus.setStatus(DownloadStatus.STATUS_CANCELED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }

    @Override
    public void onDownloadFailed(DownloadException e) {
        mDownloadStatus.setException(e);
        mDownloadStatus.setStatus(DownloadStatus.STATUS_FAILED);
        mDelivery.post(mDownloadInfo, mDownloadStatus);
    }
}
