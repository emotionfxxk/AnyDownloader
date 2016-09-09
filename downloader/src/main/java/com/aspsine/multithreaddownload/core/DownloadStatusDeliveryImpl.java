package com.aspsine.multithreaddownload.core;

import android.os.Handler;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.aspsine.multithreaddownload.architecture.DownloadStatusDelivery;
import com.aspsine.multithreaddownload.DownloadInfo;

import java.util.concurrent.Executor;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadStatusDeliveryImpl implements DownloadStatusDelivery {
    private Executor mDownloadStatusPoster;

    public DownloadStatusDeliveryImpl(final Handler handler) {
        mDownloadStatusPoster = new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        };
    }

    @Override
    public void post(DownloadInfo downloadInfo, DownloadStatus status) {
        mDownloadStatusPoster.execute(new DownloadStatusDeliveryRunnable(downloadInfo, status));
    }

    private static class DownloadStatusDeliveryRunnable implements Runnable {
        private final DownloadStatus mDownloadStatus;
        private final CallBack mCallBack;
        private DownloadInfo mDownloadInfo;

        public DownloadStatusDeliveryRunnable(DownloadInfo downloadInfo, DownloadStatus downloadStatus) {
            mDownloadInfo = downloadInfo;
            this.mDownloadStatus = downloadStatus;
            this.mCallBack = mDownloadStatus.getCallBack();
        }

        @Override
        public void run() {
            switch (mDownloadStatus.getStatus()) {
                case DownloadStatus.STATUS_CONNECTING:
                    mCallBack.onConnecting(mDownloadInfo);
                    break;
                case DownloadStatus.STATUS_CONNECTED:
                    mCallBack.onConnected(mDownloadInfo,
                            mDownloadStatus.getLength(), mDownloadStatus.isAcceptRanges());
                    break;
                case DownloadStatus.STATUS_PROGRESS:
                    mCallBack.onProgress(mDownloadInfo, mDownloadStatus.getFinished(),
                            mDownloadStatus.getLength(), mDownloadStatus.getPercent(),
                            mDownloadStatus.getSpeed());
                    break;
                case DownloadStatus.STATUS_COMPLETED:
                    mCallBack.onCompleted(mDownloadInfo);
                    break;
                case DownloadStatus.STATUS_PAUSED:
                    mCallBack.onDownloadPaused(mDownloadInfo);
                    break;
                case DownloadStatus.STATUS_AUTO_PAUSED:
                    mCallBack.onDownloadAutoPaused(mDownloadInfo);
                    break;
                case DownloadStatus.STATUS_CANCELED:
                    mCallBack.onDownloadCanceled(mDownloadInfo);
                    break;
                case DownloadStatus.STATUS_FAILED:
                    mCallBack.onFailed(mDownloadInfo,
                            (DownloadException) mDownloadStatus.getException());
                    break;
            }
        }
    }
}
