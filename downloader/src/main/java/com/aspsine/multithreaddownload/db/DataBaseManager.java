package com.aspsine.multithreaddownload.db;

import android.content.Context;

import com.aspsine.multithreaddownload.DownloadInfo;

import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class DataBaseManager {
    private static DataBaseManager sDataBaseManager;
    private final ThreadInfoDao mThreadInfoDao;
    private final DownloadInfoDao mDownloadInfoDao;

    public static DataBaseManager getInstance(Context context) {
        if (sDataBaseManager == null) {
            sDataBaseManager = new DataBaseManager(context);
        }
        return sDataBaseManager;
    }

    private DataBaseManager(Context context) {
        mDownloadInfoDao = new DownloadInfoDao(context);
        mThreadInfoDao = new ThreadInfoDao(context);
    }

    public synchronized void insert(ThreadInfo threadInfo) {
        mThreadInfoDao.insert(threadInfo);
    }

    public synchronized void delete(String tag) {
        mThreadInfoDao.delete(tag);
    }

    public synchronized void update(String tag, int threadId, long finished) {
        mThreadInfoDao.update(tag, threadId, finished);
    }

    public List<ThreadInfo> getThreadInfos(String tag) {
        return mThreadInfoDao.getThreadInfos(tag);
    }

    public boolean exists(String tag, int threadId) {
        return mThreadInfoDao.exists(tag, threadId);
    }

    // manipulation methods for task information
    public synchronized void insertTask(DownloadInfo downloadInfo) {
        mDownloadInfoDao.insert(downloadInfo);
    }

    public synchronized void deleteTask(DownloadInfo downloadInfo) {
        mDownloadInfoDao.delete(downloadInfo);
    }

    public synchronized void deleteTask(String taskId) {
        mDownloadInfoDao.delete(taskId);
    }

    public synchronized void updateTask(DownloadInfo downloadInfo) {
        mDownloadInfoDao.update(downloadInfo);
    }

    public List<DownloadInfo> getTaskInfos() {
        return mDownloadInfoDao.getTaskInfos();
    }

    public boolean isTaskExist(String url) {
        return mDownloadInfoDao.exists(url);
    }

    public List<DownloadInfo> getFinishedDownloadTasks() {
        return mDownloadInfoDao.getFinishedDownloadTasks();
    }

    public List<DownloadInfo> getUnfinishedDownloadTasks() {
        return mDownloadInfoDao.getUnfinishedDownloadTasks();
    }
}
