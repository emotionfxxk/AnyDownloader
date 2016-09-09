package com.vivid.nanodownloader;

import android.app.Application;

import com.aspsine.multithreaddownload.Constants;
import com.aspsine.multithreaddownload.DownloadConfiguration;
import com.aspsine.multithreaddownload.DownloadManager;
import com.vivid.nanodownloader.detector.MediaDetectorAgent;
import com.vivid.nanodownloader.model.DownloadModel;
import com.vivid.nanodownloader.setting.SettingManager;
import com.vivid.nanodownloader.statistics.StatisticManager;
import com.vivid.nanodownloader.utils.ClipboardUtils;
import com.vivid.nanodownloader.utils.MimeTypes;

/**
 * Created by sean on 7/5/16.
 */
public class NanoApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        StatisticManager.init(this);
        SettingManager.getInstance().init(this);
        DownloadService.initDownload(this);
        // init download manager here
        Constants.CONFIG.DEBUG = BuildConfig.DEBUG;
        DownloadConfiguration configuration = new DownloadConfiguration();
        configuration.setMaxThreadNum(10);
        configuration.setThreadNum(3);
        DownloadManager.getInstance().init(getApplicationContext(), configuration);
        DownloadModel.instance().init(this);

        // TODO: IO operation should be moved to background task
        MimeTypes.instance().init(this);
        ClipboardUtils.init(this);
        MediaDetectorAgent.instance().init();
    }
}
