package com.aspsine.multithreaddownload.core;

/**
 * Created by Aspsine on 2015/7/20.
 */

import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.db.DataBaseManager;
import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.db.ThreadInfo;
import com.aspsine.multithreaddownload.util.L;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * download thread
 */
public class MultiDownloadTask extends DownloadTaskImpl {
    private final static String TAG = "MultiDownloadTask";

    private DataBaseManager mDBManager;

    public MultiDownloadTask(DownloadInfo DownloadInfo, ThreadInfo threadInfo,
                             DownloadConfiguration config,
                             DataBaseManager dbManager, OnDownloadListener listener) {

        super(DownloadInfo, threadInfo, config, listener);
        this.mDBManager = dbManager;
    }


    @Override
    protected void insertIntoDB(ThreadInfo info) {
        /* Move insert action to DownloaderImpl
        if (!mDBManager.exists(info.getTag(), info.getId())) {
            mDBManager.insert(info);
        }*/
    }

    @Override
    protected int getResponseCode() {
        return HttpURLConnection.HTTP_PARTIAL;
    }

    @Override
    protected void updateDB(ThreadInfo info) {
        L.d(TAG, "updateDB :info.getStart():" + info.getStart() +
                ", info.getFinished():" + info.getFinished());
        mDBManager.update(info.getTag(), info.getId(), info.getFinished());
    }

    @Override
    protected Map<String, String> getHttpHeaders(ThreadInfo info) {
        Map<String, String> headers = new HashMap<String, String>();
        L.d(TAG, "getHttpHeaders :info.s:" + info.getStart() +
                ", info.F:" + info.getFinished());
        long start = info.getStart() + info.getFinished();
        long end = info.getEnd();
        L.d(TAG, "getHttpHeaders Range:" + "bytes=" + start + "-" + end);
        headers.put("Range", "bytes=" + start + "-" + end);
        return headers;
    }

    @Override
    protected RandomAccessFile getFile(File dir, String name, long offset) throws IOException {
        File file = new File(dir, name);
        RandomAccessFile raf = new RandomAccessFile(file, "rwd");
        L.d(TAG, "getFile() seek to:" + offset);
        raf.seek(offset);
        return raf;
    }
}