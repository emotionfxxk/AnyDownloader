package com.vivid.nanodownloader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vivid.nanodownloader.setting.CustomPreference;
import com.vivid.nanodownloader.setting.CustomSwitchPreference;
import com.vivid.nanodownloader.setting.DownloadConfigure;
import com.vivid.nanodownloader.setting.SettingManager;

public class SettingActivity extends AppCompatActivity {
    private final static String TAG = "SettingActivity";
    public static class SettingPreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(SettingManager.PREFERENCE_NAME);
            addPreferencesFromResource(R.xml.settings);
            update();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = super.onCreateView(inflater, container, savedInstanceState);
            return rootView;
        }

        @Override
        public boolean onPreferenceTreeClick (PreferenceScreen preferenceScreen,
                                              Preference preference) {
            String key = preference.getKey();
            boolean bRet = false;
            if (key.equals(SettingManager.SettingKey.DOWNLOAD_PATH)) {
                bRet = true;
                chooseFolder(DownloadConfigure.getDownloadBasePath(this.getActivity()));
            }
            return bRet;
        }

        @Override
        public void onResume() {
            super.onResume();
            update();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            if (sp != null) {
                sp.registerOnSharedPreferenceChangeListener(SettingManager.getInstance());
                sp.registerOnSharedPreferenceChangeListener(this);
            }
        }

        @Override
        public void onPause() {
            super.onPause();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            if (sp != null) {
                sp.unregisterOnSharedPreferenceChangeListener(SettingManager.getInstance());
                sp.unregisterOnSharedPreferenceChangeListener(this);
            }
        }

        private void update() {
            CustomPreference preference = (CustomPreference) getPreferenceScreen().findPreference(
                    SettingManager.SettingKey.DOWNLOAD_PATH);
            preference.setSummary(SettingManager.getInstance().getDownloadPath());

            CustomSwitchPreference wifiOnly = (CustomSwitchPreference)
                    getPreferenceScreen().findPreference(SettingManager.SettingKey.WIFI_ONLY);
            wifiOnly.setChecked(SettingManager.getInstance().isWifiOnlyEnabled());

            CustomSwitchPreference autoResume = (CustomSwitchPreference)
                    getPreferenceScreen().findPreference(SettingManager.SettingKey.AUTO_RESUME);
            autoResume.setChecked(SettingManager.getInstance().isAutoResumeEnabled());
        }


        private void chooseFolder(String basePath) {
            Intent intent = new Intent(this.getActivity(), SelectFolderActivity.class);
            intent.putExtra("basePath", basePath);
            startActivity(intent);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            update();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(R.id.content_frame,
                new SettingPreferenceFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
