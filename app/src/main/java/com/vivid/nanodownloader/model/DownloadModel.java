package com.vivid.nanodownloader.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.aspsine.multithreaddownload.DownloadInfo;
import com.vivid.nanodownloader.DownloadService;
import com.vivid.nanodownloader.utils.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sean on 7/12/16.
 */
public class DownloadModel {
    private final static String TAG = "DownloadModel";
    private Context context;
    private List<DownloadInfo> finishedDownloads;
    private List<DownloadInfo> unFinishedDownloads;
    private List<OnDataChangeListener> listeners = new LinkedList<>();
    public interface OnDataChangeListener {
        void onTaskCompleted(String taskId, int removePos, int insertPos);
        void onTaskCanceled(List<String> taskIds);
        void onNewTask(String taskId, int insertPos);
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra(DownloadService.EXTRA_MESSAGE);
            DownloadInfo dlInfo = (DownloadInfo)intent.getSerializableExtra(DownloadService.EXTRA_TASK);
            LogUtils.d(TAG, "onReceive message:" + message + ", taskId:" + dlInfo.getId());
            if (message.equals(DownloadService.EXTRA_MESSAGE_COMPLETE)) {
                for (int index = 0; index < unFinishedDownloads.size(); ++index) {
                    DownloadInfo downloadInfo = unFinishedDownloads.get(index);
                    if (downloadInfo.equals(dlInfo)) {
                        unFinishedDownloads.remove(index);
                        finishedDownloads.add(0, downloadInfo);
                        for (OnDataChangeListener listener : listeners) {
                            listener.onTaskCompleted(dlInfo.getId(), index, 0);
                        }
                        break;
                    }
                }
            } else if(message.equals(DownloadService.EXTRA_MESSAGE_CANCEL)) {
                List<String> taskIds = new ArrayList<>();
                taskIds.add(dlInfo.getId());
                for (OnDataChangeListener listener : listeners) {
                    listener.onTaskCanceled(taskIds);
                }
            } else if(message.equals(DownloadService.EXTRA_MESSAGE_START)) {
                for (DownloadInfo downloadInfo: unFinishedDownloads) {
                    if (downloadInfo.equals(dlInfo)) {
                        return;
                    }
                }
                LogUtils.d(TAG, "onReceive new download task with id:" + dlInfo.getId());
                unFinishedDownloads.add(0, dlInfo);
                for (OnDataChangeListener listener : listeners) {
                    listener.onNewTask(dlInfo.getId(), 0);
                }
            }
        }
    };
    public void addOnDataChangeListener(OnDataChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void removeOnDataChangeListener(OnDataChangeListener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }
    private static DownloadModel sModel;
    public static DownloadModel instance() {
        if (sModel == null) {
            sModel = new DownloadModel();
        }
        return sModel;
    }
    public void init(Context context) {
        this.context = context;
        finishedDownloads = DownloadService.getFinishedDownloadTasks();
        unFinishedDownloads = DownloadService.getUnfinishedDownloadTasks();
        Collections.sort(finishedDownloads);
        Collections.sort(unFinishedDownloads);
        IntentFilter intentFilter = new IntentFilter(DownloadService.ACTION_DOWNLOAD_BROAD_CAST);
        LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver, intentFilter);
    }
    private DownloadModel() {}
    public void removeTasks(List<String> taskIds) {
        for (String taskId: taskIds) {
            for (DownloadInfo downloadInfo: finishedDownloads) {
                if(downloadInfo.getId().equals(taskId)) {
                    finishedDownloads.remove(downloadInfo);
                    break;
                }
            }
            for (DownloadInfo downloadInfo: unFinishedDownloads) {
                if(downloadInfo.getId().equals(taskId)) {
                    unFinishedDownloads.remove(downloadInfo);
                    break;
                }
            }
        }
        for (OnDataChangeListener listener : listeners) {
            listener.onTaskCanceled(taskIds);
        }
    }
    public List<DownloadInfo> getFinishedDownloads() {
        return finishedDownloads;
    }
    public List<DownloadInfo> getUnFinishedDownloads() {
        return unFinishedDownloads;
    }
}
