package com.aspsine.multithreaddownload.architecture;

import com.aspsine.multithreaddownload.DownloadInfo;

/**
 * Created by Aspsine on 2015/7/15.
 */
public interface DownloadStatusDelivery {

    void post(DownloadInfo downloadInfo, DownloadStatus status);

}
