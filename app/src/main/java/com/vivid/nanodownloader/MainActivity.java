package com.vivid.nanodownloader;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;

import com.vivid.nanodownloader.detector.MediaDetector;
import com.vivid.nanodownloader.dialog.NewDownloadDialog;
import com.vivid.nanodownloader.event.MultiSelectionEvent;
import com.vivid.nanodownloader.model.DownloadModel;
import com.vivid.nanodownloader.utils.ClipboardUtils;
import com.vivid.nanodownloader.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DownloadModel.OnDataChangeListener {
    private final static String TAG = "MainActivity";
    private SectionsPagerAdapter sectionsPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FloatingActionButton floatingActionButton;

    private final static int INDEX_DISCOVER = 0;
    private final static int INDEX_RUNNING = 1;
    private final static int INDEX_FINISHED = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        if (floatingActionButton != null) {
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NewDownloadDialog dialog = NewDownloadDialog.newInstance();
                    dialog.show(getSupportFragmentManager(), NewDownloadDialog.class.getSimpleName());
                }
            });
        }

        EventBus.getDefault().register(this);
        DownloadModel.instance().addOnDataChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final String textInClipboard = ClipboardUtils.getText();
        LogUtils.d(TAG, "textInClipboard:" + textInClipboard);
        if (textInClipboard != null && !TextUtils.isEmpty(textInClipboard) &&
                URLUtil.isValidUrl(textInClipboard)) {
            String host = null;
            try {
                URL currentUrl = new URL(textInClipboard);
                LogUtils.d(TAG, "host:" + currentUrl.getHost());
                host =  currentUrl.getHost();
            } catch (Exception e) {}
            if (host != null && host.contains("www.instagram.com")) {
                Snackbar.make(floatingActionButton, getResources().getString(R.string.snack_bar_message),
                        Snackbar.LENGTH_LONG)
                        .setDuration(6000)
                        .setAction(getResources().getString(R.string.snack_action), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                BrowserActivity.browserUrl(MainActivity.this, textInClipboard);
                                ClipboardUtils.clear();
                            }
                        }).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        DownloadModel.instance().removeOnDataChangeListener(this);
    }


    @Subscribe
    public void onEvent(MultiSelectionEvent event) {
        LogUtils.d(TAG, "onEvent MultiSelectionEvent:" + event.active);
        if (tabLayout != null) {
            tabLayout.setVisibility(event.active ? View.GONE : View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingActivity.class));
            return true;
        } else if (id == R.id.action_browser) {
            BrowserActivity.browserUrl(this, null);
            //startActivity(new Intent(MainActivity.this, BrowserActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTaskCompleted(String taskId, int removePos, int insertPos) {
        updateTabText();
    }

    @Override
    public void onTaskCanceled(List<String> taskIds) {
        updateTabText();
    }

    @Override
    public void onNewTask(String taskId, int insertPos) {
        updateTabText();
    }

    private void updateTabText() {
        int size = DownloadModel.instance().getUnFinishedDownloads().size();
        if (tabLayout.getTabAt(INDEX_RUNNING) != null) {
            tabLayout.getTabAt(INDEX_RUNNING).setText(size > 0 ? getResources().getString(
                    R.string.tab_running_fmt, size) : getResources().getString(
                    R.string.tab_running));
        }
        int finishedSize = DownloadModel.instance().getFinishedDownloads().size();
        if (tabLayout.getTabAt(INDEX_FINISHED) != null) {
            tabLayout.getTabAt(INDEX_FINISHED).setText(finishedSize > 0 ? getResources().getString(
                    R.string.tab_finished_fmt, finishedSize) : getResources().getString(
                    R.string.tab_finished));
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override
        public Fragment getItem(int position) {
            if (position == INDEX_RUNNING) {
                return DownloadingFragment.newInstance();
            } else if (position == INDEX_FINISHED) {
                return DownloadedFragment.newInstance();
            } else if (position == INDEX_DISCOVER) {
                return DiscoverFragment.newInstance();
            } else {
                throw new IllegalStateException("invalid: pos=" + position);
            }
        }
        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case INDEX_DISCOVER:
                    return getResources().getString(R.string.tab_discover);
                case INDEX_RUNNING:
                    int runningSize = DownloadModel.instance().getUnFinishedDownloads().size();
                    return runningSize > 0 ? getResources().getString(
                            R.string.tab_running_fmt, runningSize) :
                            getResources().getString(R.string.tab_running);
                case INDEX_FINISHED:
                    int finishedSize = DownloadModel.instance().getFinishedDownloads().size();
                    return finishedSize > 0 ? getResources().getString(
                            R.string.tab_finished_fmt, finishedSize) :
                            getResources().getString(R.string.tab_finished);
            }
            return null;
        }
    }
}
