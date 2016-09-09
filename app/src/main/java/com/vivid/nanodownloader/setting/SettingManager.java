package com.vivid.nanodownloader.setting;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.vivid.nanodownloader.utils.LogUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SettingManager implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SettingManager";
    private static SettingManager sInstance;
    public static final String PREFERENCE_NAME = "settings";
    public static final String DEFAULT_DOWNLOAD_PATH =
            Environment.getExternalStorageDirectory().getPath() + File.separator +
                    "NanoDownloader" + File.separator + "download";

    private List<Listener> listeners = new ArrayList<>();

    public interface Listener {
        void onSettingChanged(String key);
    }

    public static synchronized SettingManager getInstance() {
        if (sInstance == null) {
            sInstance = new SettingManager();
        }
        return sInstance;
    }

    public static class SettingKey {
        public static final String DOWNLOAD_PATH = "download_path";
        public static final String WIFI_ONLY = "download_wifi_only";
        public static final String AUTO_UPDATE = "auto_update";
        public static final String AUTO_RESUME = "auto_resume";
    }

    private SharedPreferences settingSharedPreference;
    private boolean initialized = false;

    public void init(Context context) {
        if (!initialized) {
            settingSharedPreference =
                    context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            LogUtils.d(TAG, "settingSharedPreference:" + settingSharedPreference);
            settingSharedPreference.registerOnSharedPreferenceChangeListener(this);
            initialized = true;
        }
    }

    public void addListener(Listener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(Listener listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        for (Listener listener : listeners) {
            if (listener != null) {
                listener.onSettingChanged(key);
            }
        }
    }

    public String getDownloadPath() {
        String path = settingSharedPreference.getString(SettingKey.DOWNLOAD_PATH, DEFAULT_DOWNLOAD_PATH);
        try {
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
            }
        } catch (Exception e) {}
        return path;
    }

    public void setDownloadPath(String downloadPath) {
        settingSharedPreference.edit().putString(SettingKey.DOWNLOAD_PATH, downloadPath).apply();
    }

    public boolean isWifiOnlyEnabled() {
        return settingSharedPreference.getBoolean(SettingKey.WIFI_ONLY, false);
    }

    public void setWifiOnly(boolean enable) {
        settingSharedPreference.edit().putBoolean(SettingKey.WIFI_ONLY, enable).apply();
    }

    public boolean isAutoUpdateEnabled() {
        return settingSharedPreference.getBoolean(SettingKey.AUTO_UPDATE, true);
    }

    public void setAutoUpdate(boolean enable) {
        settingSharedPreference.edit().putBoolean(SettingKey.AUTO_UPDATE, enable).apply();
    }

    public boolean isAutoResumeEnabled() {
        return settingSharedPreference.getBoolean(SettingKey.AUTO_RESUME, true);
    }

    public void setAutoResume(boolean enable) {
        settingSharedPreference.edit().putBoolean(SettingKey.AUTO_RESUME, enable).apply();
    }
}
