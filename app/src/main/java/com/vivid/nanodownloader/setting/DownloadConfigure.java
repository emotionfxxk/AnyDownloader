package com.vivid.nanodownloader.setting;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class DownloadConfigure {

    public static String getDownloadPath() {
        String path = SettingManager.getInstance().getDownloadPath();
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        return path;
    }

    public static void setDownloadPath(String path) {
        File f = new File(path);
        if (!f.exists()) {
            f.mkdirs();
        }
        SettingManager.getInstance().setDownloadPath(path);
    }

    public static String getDownloadBasePath(Context context) {
        File rootFolder;
        if (Environment.isExternalStorageEmulated()) {
            rootFolder = new File(Environment.getExternalStorageDirectory().getPath());
            if (rootFolder.exists() && rootFolder.canWrite()) {
                return rootFolder.getAbsolutePath();
            }
        }
        rootFolder = Environment.getExternalStorageDirectory();
        if (rootFolder.canWrite()) {
            return rootFolder.getAbsolutePath();
        }
        rootFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
        if (!rootFolder.exists()) {
            try {
                rootFolder.mkdirs();
            } catch (Exception ignore){}
            if (rootFolder.exists() && rootFolder.canWrite()) {
                return rootFolder.getAbsolutePath();
            }
        }
        rootFolder = new File(context.getFilesDir(), Environment.DIRECTORY_DOWNLOADS);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }

        return rootFolder.getAbsolutePath();
    }
}
