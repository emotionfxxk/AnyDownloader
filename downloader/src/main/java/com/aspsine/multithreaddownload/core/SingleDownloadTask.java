package com.aspsine.multithreaddownload.core;


import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.db.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Created by Aspsine on 2015/7/22.
 */
public class SingleDownloadTask extends DownloadTaskImpl {
    private DataBaseManager mDBManager;
    public SingleDownloadTask(DownloadInfo downloadInfo, ThreadInfo mThreadInfo, DownloadConfiguration config,
                              DataBaseManager dbManager, OnDownloadListener mOnDownloadListener) {
        super(downloadInfo, mThreadInfo, config, mOnDownloadListener);
        mDBManager = dbManager;
    }

    @Override
    protected void insertIntoDB(ThreadInfo info) {
        // don't support
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_OK;
    }

    @Override
    protected void updateDB(ThreadInfo info) {
        // needn't Override this
        mDBManager.update(info.getTag(), info.getId(), info.getFinished());
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        // simply return null
        return null;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        raf.seek(0);
        return raf;
    }
}

