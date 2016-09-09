package com.aspsine.multithreaddownload;

/**
 * Created by Aspsine on 2015/7/14.
 */
public class DownloadConfiguration {

    public static final int DEFAULT_MAX_THREAD_NUMBER = 10;

    public static final int DEFAULT_THREAD_NUMBER = 1;

    public static final int DEFAULT_MAX_CONNECT_AUTO_RECOVER_COUNT = 6;

    public static final int DEFAULT_MAX_TASK_AUTO_RECOVER_COUNT = 6;
    /**
     * thread number in the pool
     */
    private int maxThreadNum;

    /**
     * thread number for each download
     */
    private int threadNum;

    /**
     * max auto recover count for connect task
     */
    private int maxConnectAutoRecoverCount;

    /**
     * max auto recover count for download task
     */
    private int maxDownloadAutoRecoverCount;

    /**
     * init with default value
     */
    public DownloadConfiguration() {
        maxThreadNum = DEFAULT_MAX_THREAD_NUMBER;
        threadNum = DEFAULT_THREAD_NUMBER;
        maxConnectAutoRecoverCount = DEFAULT_MAX_CONNECT_AUTO_RECOVER_COUNT;
        maxDownloadAutoRecoverCount = DEFAULT_MAX_TASK_AUTO_RECOVER_COUNT;
    }

    public int getMaxThreadNum() {
        return maxThreadNum;
    }

    public void setMaxThreadNum(int maxThreadNum) {
        this.maxThreadNum = maxThreadNum;
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void setThreadNum(int threadNum) {
        this.threadNum = threadNum;
    }

    public int getConnectAutoRecoverCount() {
        return maxConnectAutoRecoverCount;
    }

    public void setConnectAutoRecoverCount(int count) {
        this.maxConnectAutoRecoverCount = count;
    }

    public int getDownloadAutoRecoverCount() {
        return maxDownloadAutoRecoverCount;
    }

    public void setDownloadAutoRecoverCount(int count) {
        this.maxDownloadAutoRecoverCount = count;
    }
}
