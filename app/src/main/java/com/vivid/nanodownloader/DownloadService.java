package com.vivid.nanodownloader;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.aspsine.multithreaddownload.CallBack;
import com.aspsine.multithreaddownload.DownloadException;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.DownloadManager;
import com.aspsine.multithreaddownload.DownloadRequest;
import com.vivid.nanodownloader.setting.SettingManager;
import com.vivid.nanodownloader.statistics.StatisticManager;
import com.vivid.nanodownloader.utils.Connectivity;
import com.vivid.nanodownloader.utils.FileUtils;
import com.vivid.nanodownloader.utils.LogUtils;

import java.io.File;
import java.util.List;

/**
 * Created by aspsine on 15/7/28.
 */
public class DownloadService extends Service implements SettingManager.Listener {

    private static final String TAG = "DownloadService";
    public static final String ACTION_DOWNLOAD_BROAD_CAST =
            "com.vivid.nanodownloader:action_download_broad_cast";
    public static final String ACTION_INIT = "com.vivid.nanodownloader:action_init";
    public static final String ACTION_DOWNLOAD = "com.vivid.nanodownloader:action_download";
    public static final String ACTION_PAUSE = "com.vivid.nanodownloader:action_pause";
    public static final String ACTION_CANCEL = "com.vivid.nanodownloader:action_cancel";
    public static final String ACTION_RESUME = "com.vivid.nanodownloader:action_resume";
    public static final String ACTION_PAUSE_ALL = "com.vivid.nanodownloader:action_pause_all";
    public static final String ACTION_CONNECTIVITY = "com.vivid.nanodownloader:action_connectivity_change";
    public static final String EXTRA_URL = "extra_url";
    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_MIME = "extra_mime";
    public static final String EXTRA_CONTENT_LEN = "extra_length";
    public static final String EXTRA_THUMB_URL = "extra_thumb_url";
    public static final String EXTRA_TASK_ID = "extra_task_id";
    public static final String EXTRA_TASK_IDS = "extra_task_ids";
    public static final String EXTRA_RM_FILE = "extra_rm_file_also";
    public static final String EXTRA_TASK = "extra_task";
    public static final String EXTRA_MESSAGE = "extra_message";
    public static final String EXTRA_MESSAGE_START = "extra_message_start";
    public static final String EXTRA_MESSAGE_COMPLETE = "extra_message_complete";
    public static final String EXTRA_MESSAGE_CANCEL = "extra_message_cancel";

    private DownloadManager mDownloadManager;
    private NotificationManagerCompat mNotificationManager;
    private DownloadCallBack mDownloadCallback;
    private boolean mIsConnected;
    private boolean mIsWifiConnected;
    private boolean mIsWifiOnly;

    public static void intentDownload(Context context, String name, String url,
                                      String mime, long contentLen, String thumbUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_NAME, name);
        intent.putExtra(EXTRA_MIME, mime);
        intent.putExtra(EXTRA_CONTENT_LEN, contentLen);
        intent.putExtra(EXTRA_THUMB_URL, thumbUrl);
        context.startService(intent);
    }

    public static void initDownload(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_INIT);
        context.startService(intent);
    }

    public static void intentPause(Context context, String taskId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_PAUSE);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startService(intent);
    }

    public static void intentResume(Context context, String taskId) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_RESUME);
        intent.putExtra(EXTRA_TASK_ID, taskId);
        context.startService(intent);
    }

    public static void intentCancel(Context context, String[] taskIds,
                                    boolean removeFileAlso) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_CANCEL);
        intent.putExtra(EXTRA_TASK_IDS, taskIds);
        intent.putExtra(EXTRA_RM_FILE, removeFileAlso);
        context.startService(intent);
    }

    public static void intentPauseAll(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        context.startService(intent);
    }

    public static void intentConnectivityChange(Context context) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_CONNECTIVITY);
        context.startService(intent);
    }

    public static List<DownloadInfo> getFinishedDownloadTasks() {
        return DownloadManager.getInstance().getFinishedDownloadTasks();
    }
    public static List<DownloadInfo> getUnfinishedDownloadTasks() {
        return DownloadManager.getInstance().getUnfinishedDownloadTasks();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            String taskId = intent.hasExtra(EXTRA_TASK_ID) ?
                    intent.getStringExtra(EXTRA_TASK_ID) : null;
            switch (action) {
                case ACTION_DOWNLOAD:
                    download(intent);
                    break;
                case ACTION_PAUSE:
                    pause(taskId);
                    break;
                case ACTION_RESUME:
                    resume(taskId);
                    break;
                case ACTION_CANCEL:
                    cancel(intent);
                    break;
                case ACTION_PAUSE_ALL:
                    pauseAll();
                    break;
                case ACTION_INIT:
                    LogUtils.d(TAG, "init ....");
                    init();
                    break;
                case ACTION_CONNECTIVITY:
                    onDownloadConditionChanged();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void download(Intent intent) {
        String name = intent.getStringExtra(EXTRA_NAME);
        String url = intent.getStringExtra(EXTRA_URL);
        String thumbUrl = intent.getStringExtra(EXTRA_THUMB_URL);
        String mime = intent.getStringExtra(EXTRA_MIME);
        String actualName = generateFileName(name);
        long length = intent.getLongExtra(EXTRA_CONTENT_LEN, 0);
        final DownloadRequest request = new DownloadRequest.Builder()
                .setActualName(actualName)
                .setOriginalName(name)
                .setMimeType(mime)
                .setUri(url)
                .setSavePath(SettingManager.getInstance().getDownloadPath())
                .setScannable(true)
                .setThumbUrl(thumbUrl)
                .setContentLength(length)
                .build();
        String taskId = DownloadManager.genTaskId(url);
        boolean isWifiConnected = Connectivity.isConnectedWifi(this);
        boolean isWifiOnly = SettingManager.getInstance().isWifiOnlyEnabled();
        LogUtils.d(TAG, "download() isWifiConnected:" + isWifiConnected +
                ", isWifiOnly:" + isWifiOnly);
        if (!isWifiConnected && isWifiOnly) {
            // add download task, but not start it
            mDownloadManager.bookDownload(request, taskId);
        } else {
            mDownloadManager.download(request, taskId, null);
        }
        StatisticManager.logDownloadStart();
    }

    public String generateFileName(String originalName) {
        String validName = FileUtils.removeIllegalChar(originalName, "_");
        int index = 0;
        while (true) {
            String tempName = (index > 0) ? ("(" + index + ")" + validName) : validName;
            if (!new File(SettingManager.getInstance().getDownloadPath() + File.separator + tempName).exists()) {
                validName = tempName;
                break;
            }
            ++index;
        }
        LogUtils.d(TAG, "generateFileName originalName:" + originalName);
        LogUtils.d(TAG, "generateFileName validName:" + validName);
        return validName;

    }

    private void pause(String taskId) {
        mDownloadManager.pause(taskId);
    }

    private void resume(String taskId) {
        mDownloadManager.resume(taskId);
    }

    private void cancel(Intent intent) {
        String[] taskIds = intent.hasExtra(EXTRA_TASK_IDS) ?
                intent.getStringArrayExtra(EXTRA_TASK_IDS) : null;
        boolean removeFileAlso = intent.getBooleanExtra(EXTRA_RM_FILE, false);
        mDownloadManager.cancel(taskIds, removeFileAlso);
    }

    private void pauseAll() {
        mDownloadManager.pauseAll();
    }

    @Override
    public void onSettingChanged(String key) {
        if (SettingManager.SettingKey.WIFI_ONLY.equals(key)) {
            onDownloadConditionChanged();
        }
    }

    private class DownloadCallBack implements CallBack {
        private LocalBroadcastManager mLocalBroadcastManager;
        private NotificationCompat.Builder mBuilder;
        private NotificationManagerCompat mNotificationManager;
        private long mLastTime;
        private int availableNotificationId = 1000;
        public DownloadCallBack(NotificationManagerCompat notificationManager,
                                Context context) {
            mNotificationManager = notificationManager;
            mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
            mBuilder = new NotificationCompat.Builder(context);
        }

        @Override
        public void onStarted(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onStart()");
            mBuilder.setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(downloadInfo.getActualFileName())
                    .setContentText("Init Download")
                    .setProgress(100, 0, true)
                    .setTicker("Start download " + downloadInfo.getActualFileName());
            downloadInfo.setNotificationId(availableNotificationId++);
            updateNotification(downloadInfo.getNotificationId());
            sendBroadCast(downloadInfo, EXTRA_MESSAGE_START);
        }

        @Override
        public void onConnecting(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onConnecting()");
            mBuilder.setContentText("Connecting")
                    .setProgress(100, 0, true);
            updateNotification(downloadInfo.getNotificationId());
            //sendBroadCast(downloadInfo);
        }

        @Override
        public void onConnected(DownloadInfo downloadInfo, long total, boolean isRangeSupport) {
            LogUtils.d(TAG, "onConnected()");
            mBuilder.setContentText("Connected")
                    .setProgress(100, 0, true);
            updateNotification(downloadInfo.getNotificationId());
        }

        @Override
        public void onProgress(DownloadInfo downloadInfo, long finished, long total,
                               int progress, int speed) {
            LogUtils.d(TAG, "onProgress() speed:" + speed + ", finished:" + finished);
            if (mLastTime == 0) {
                mLastTime = System.currentTimeMillis();
            }
            long currentTime = System.currentTimeMillis();
            if (currentTime - mLastTime > 500) {
                //L.i(TAG, "onProgress() total:" + total + ", finished:" + finished);
                mBuilder.setContentText("Downloading");
                mBuilder.setProgress(100, progress, false);
                updateNotification(downloadInfo.getNotificationId());
                //sendBroadCast(downloadInfo);
                mLastTime = currentTime;
            }
            downloadInfo.setSpeed(speed);
        }

        @Override
        public void onCompleted(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onCompleted()");
            mBuilder.setContentText("Download Complete");
            mBuilder.setProgress(0, 0, false);
            mBuilder.setTicker(downloadInfo.getActualFileName() + " download Complete");
            updateNotification(downloadInfo.getNotificationId());
            sendBroadCast(downloadInfo, EXTRA_MESSAGE_COMPLETE);

            int averageSpeed = downloadInfo.getAverageSpeed();
            StatisticManager.logDownloadFinished(downloadInfo.getTotalSize(), averageSpeed);
        }

        @Override
        public void onDownloadPaused(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onDownloadPaused()");
            mBuilder.setContentText("Download Paused");
            mBuilder.setTicker(downloadInfo.getActualFileName() + " download Paused");
            updateNotification(downloadInfo.getNotificationId());
            //sendBroadCast(downloadInfo);
        }

        @Override
        public void onDownloadAutoPaused(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onDownloadAutoPaused()");
            mBuilder.setContentText("Download auto Paused");
            mBuilder.setTicker(downloadInfo.getActualFileName() + " download auto Paused");
            updateNotification(downloadInfo.getNotificationId());
            //sendBroadCast(downloadInfo);
        }

        @Override
        public void onDownloadCanceled(DownloadInfo downloadInfo) {
            LogUtils.d(TAG, "onDownloadCanceled()");
            mBuilder.setContentText("Download Canceled");
            mBuilder.setTicker(downloadInfo.getActualFileName() + " download Canceled");
            updateNotification(downloadInfo.getNotificationId());
            mNotificationManager.cancel(downloadInfo.getNotificationId());
            sendBroadCast(downloadInfo, EXTRA_MESSAGE_CANCEL);

            StatisticManager.logDownloadCanceled(downloadInfo.getUrl(),
                    downloadInfo.getFinishedSize() == downloadInfo.getTotalSize());
        }

        @Override
        public void onFailed(DownloadInfo downloadInfo, DownloadException e) {
            LogUtils.d(TAG, "onFailed()");
            e.printStackTrace();
            mBuilder.setContentText("Download Failed");
            mBuilder.setTicker(downloadInfo.getActualFileName() + " download failed");
            updateNotification(downloadInfo.getNotificationId());
            //sendBroadCast(downloadInfo);
            StatisticManager.logDownloadFailed(downloadInfo.getUrl());
        }

        private void updateNotification(int id) {
            //mNotificationManager.notify(id, mBuilder.build());
        }

        private void sendBroadCast(DownloadInfo downloadInfo, String action) {
            Intent intent = new Intent();
            intent.setAction(ACTION_DOWNLOAD_BROAD_CAST);
            intent.putExtra(EXTRA_MESSAGE, action);
            intent.putExtra(EXTRA_TASK, downloadInfo);
            mLocalBroadcastManager.sendBroadcast(intent);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d(TAG, "onCreate() :" + SettingManager.getInstance().getDownloadPath());
        mNotificationManager = NotificationManagerCompat.from(getApplicationContext());
        mDownloadCallback = new DownloadCallBack(mNotificationManager, getApplicationContext());
        mDownloadManager = DownloadManager.getInstance();
        mDownloadManager.setDownloadCallback(mDownloadCallback);
        mIsConnected = Connectivity.isConnected(this);
        mIsWifiConnected = Connectivity.isConnectedWifi(this);
        SettingManager.getInstance().addListener(this);
        mIsWifiOnly = SettingManager.getInstance().isWifiOnlyEnabled();
    }

    private void init() {
        mIsConnected = Connectivity.isConnected(this);
        mIsWifiConnected = Connectivity.isConnectedWifi(this);
        mIsWifiOnly = SettingManager.getInstance().isWifiOnlyEnabled();
        boolean isAutoResume = SettingManager.getInstance().isAutoResumeEnabled();
        LogUtils.d(TAG, "init()...mIsConnected:" + mIsConnected +
                ", mIsWifiConnected:" + mIsWifiConnected +
                ", isWifiOnly:" + mIsWifiOnly +
                ", isAutoResume" + isAutoResume);
        if (isAutoResume) {
            if ((mIsWifiOnly && mIsWifiConnected) || (!mIsWifiOnly && mIsConnected)) {
                mDownloadManager.resumeDownloadTask();
            }
        }
    }

    private void onDownloadConditionChanged() {
        boolean isConnected = Connectivity.isConnected(this);
        boolean isWifiConnected = Connectivity.isConnectedWifi(this);
        boolean isWifiOnly = SettingManager.getInstance().isWifiOnlyEnabled();
        boolean isAutoResume = SettingManager.getInstance().isAutoResumeEnabled();
        LogUtils.d(TAG, "onDownloadConditionChanged()...isConnected:" + isConnected +
                ", mIsConnected:" + mIsConnected +
                ", isWifiConnected:" + isWifiConnected +
                ", mIsWifiConnected:" + mIsWifiConnected +
                ", isWifiOnly:" + isWifiOnly +
                ", isAutoResume:" + isAutoResume);
        if (!mIsWifiOnly && isWifiOnly) {
            // wifi only turned on
            // if wifi is not connected, auto pause all unfinished tasks
            if (!isWifiConnected) {
                LogUtils.d(TAG, "onDownloadConditionChanged() WifiOnly turn on auto pause download task ...");
                mDownloadManager.autoPause();
            }
            // if wifi is connected do noting
        } else if (mIsWifiOnly && !isWifiOnly) {
            // wifi only turned off
            // if network is connected, resume all unfinished tasks
            if (isConnected) {
                LogUtils.d(TAG, "onDownloadConditionChanged() WifiOnly turn off resume download task ...");
                mDownloadManager.resumeDownloadTask();
            }
            // else do nothing
        } else {
            // wifi only setting is not changed
            if (isWifiOnly) {
                if (isWifiConnected && !mIsWifiConnected) {
                    LogUtils.d(TAG, "onDownloadConditionChanged() wifi connected! resume download task ...");
                    mDownloadManager.resumeDownloadTask();
                } else if (!isWifiConnected && mIsWifiConnected) {
                    LogUtils.d(TAG, "onDownloadConditionChanged() wifi disconnected auto pause download task ...");
                    mDownloadManager.autoPause();
                } else {
                    // check bssid
                }
            } else {
                if (isConnected && !mIsConnected) {
                    LogUtils.d(TAG, "onDownloadConditionChanged() network connected resume download task ...");
                    mDownloadManager.resumeDownloadTask();
                }
            }
        }
        mIsWifiConnected = isWifiConnected;
        mIsConnected = isConnected;
        mIsWifiOnly = isWifiOnly;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDownloadManager.pauseAll();
        SettingManager.getInstance().removeListener(this);
    }
}
