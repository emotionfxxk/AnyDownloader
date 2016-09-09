package com.vivid.nanodownloader.detector;



import android.os.AsyncTask;
import android.os.Process;


import com.vivid.nanodownloader.detector.entry.CacheEntry;
import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.event.HtmlAbstractEvent;
import com.vivid.nanodownloader.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * Created by sean on 7/25/16.
 */
public class MediaDetectorAgent {
    private final static String TAG = "MediaDetectorAgent";
    private static MediaDetectorAgent sAgent;
    private Map<String, CacheEntry> mediaCache = new HashMap<>();

    private String currentUrl;
    private String currentHtml;

    private String pendingUrl;
    private String pendingTitle;
    private Callback pendingCallback;
    private static class HitMissException extends Exception {}
    public static MediaDetectorAgent instance() {
        if (sAgent == null) {
            sAgent = new MediaDetectorAgent();
        }
        return sAgent;
    }

    public interface Callback {
        void onDetectStarted();
        void onDetectFinished(List<MediaEntry> mediaEntries);
        void onGetContentLength(MediaEntry entry);
    }

    public void init() {
        EventBus.getDefault().register(this);
    }

    @Subscribe
    public void onEvent(HtmlAbstractEvent event) {
        currentUrl = event.url;
        currentHtml = event.rawHtml;
        if (pendingUrl != null && pendingUrl.equals(currentUrl) && currentHtml != null) {
            MediaDetector mediaDetector = MediaDetectorFactory.createMatchingDetector(currentUrl, pendingTitle);
            if (mediaDetector != null && mediaDetector.isAvailable()) {
                MediaDetectTask task = new MediaDetectTask(currentUrl, currentHtml, mediaDetector, pendingCallback);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            pendingUrl = null;
        }

    }

    public void detect(String url, String title, Callback callback) {
        try {
            CacheEntry cacheEntry = mediaCache.get(url);
            if (cacheEntry != null) {
                if (!cacheEntry.isExpired()) {
                    callback.onDetectFinished(cacheEntry.getEntries());
                } else {
                    mediaCache.remove(cacheEntry);
                    throw new HitMissException();
                }
            } else {
                throw new HitMissException();
            }
        } catch (HitMissException e) {
            detectIndeed(url, title, callback);
        }
    }

    public boolean isDetectable(String url, String title) {
        MediaDetector mediaDetector = MediaDetectorFactory.createMatchingDetector(url, title);
        return (mediaDetector != null && mediaDetector.isAvailable());
    }

    private void detectIndeed(String url, String title, Callback callback) {
        if (currentUrl != null && currentUrl.equals(url) && currentHtml != null) {
            MediaDetector mediaDetector = MediaDetectorFactory.createMatchingDetector(url, title);
            if (mediaDetector != null && mediaDetector.isAvailable()) {
                MediaDetectTask task = new MediaDetectTask(url, currentHtml, mediaDetector, callback);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        } else {
            pendingUrl = url;
            pendingTitle = title;
            pendingCallback = callback;
        }
    }

    private class MediaDetectTask extends AsyncTask<Void, Integer, Integer> {
        private static final int PROGRESS_URL_FETCHED = -1;
        private static final int PROGRESS_FINISHED = Integer.MAX_VALUE;
        private static final int PROGRESS_FAILED = Integer.MIN_VALUE;
        private MediaDetector mediaDetector;
        private Callback callback;
        private long startMillions;
        private String url;
        private String rawHtml;

        private MediaDetectTask(String url, String rawHtml, MediaDetector mediaDetector, Callback callback) {
            this.url = url;
            this.mediaDetector = mediaDetector;
            this.callback = callback;
            this.rawHtml = rawHtml;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LogUtils.d(TAG, "MediaDetectTask onPreExecute");
            startMillions = System.currentTimeMillis();
            if (callback != null) {
                callback.onDetectStarted();
            }
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            try {
                mediaDetector.detect(rawHtml);
                publishProgress(PROGRESS_URL_FETCHED);

                List<MediaEntry> entries = mediaDetector.getMediaEntries();
                if (entries != null && entries.size() > 0) {
                    final Semaphore semaphore = new Semaphore(entries.size());
                    for (int i = 0; i < entries.size(); i++) {
                        try {
                            semaphore.acquire();
                        } catch (Exception e) {
                        }
                        final int index = i;
                        final MediaEntry mediaEntry = entries.get(i);
                        new Thread() {
                            @Override
                            public void run() {
                                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                                try {
                                    LogUtils.d(TAG, "video.getUrl():" + mediaEntry.getUrl());
                                    URL url = new URL(mediaEntry.getUrl());
                                    URLConnection conn = url.openConnection();
                                    mediaEntry.setContentLength(conn.getContentLength());
                                    LogUtils.d(TAG, "conn.getContentLength():" +
                                            mediaEntry.getContentLength());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    mediaEntry.setContentLength(-1);
                                } finally {
                                    semaphore.release();
                                }
                                publishProgress(index);
                            }
                        }.start();
                    }
                    while (semaphore.availablePermits() != entries.size()) {
                    }
                }
                return PROGRESS_FINISHED;
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.w(TAG, "e:" + e.getMessage());
                return PROGRESS_FAILED;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            int progressValue = values[0];
            LogUtils.d(TAG, "onProgressUpdate: progressValue" + progressValue);
            if (progressValue == PROGRESS_URL_FETCHED) {
                List<MediaEntry> entries = mediaDetector.getMediaEntries();
                if (entries != null && entries.size() > 0) {
                    String host = null;
                    try {
                        URL currentUrl = new URL(url);
                        LogUtils.d(TAG, "host:" + currentUrl.getHost());
                        host =  currentUrl.getHost();
                    } catch (Exception e) {}
                    if (host != null && !host.equals("m.facebook.com") &&
                            !host.equals("www.tumblr.com") &&
                            !host.equals("mobile.twitter.com")) {
                        mediaCache.put(url, new CacheEntry(entries));
                    }
                }
                if (callback != null) {
                    callback.onDetectFinished(entries);
                }
            } else {
                if (callback != null) {
                    List<MediaEntry> entries = mediaDetector.getMediaEntries();
                    if (entries != null && progressValue < entries.size() && progressValue >= 0) {
                        callback.onGetContentLength(mediaDetector.getMediaEntries().get(progressValue));
                    }
                }
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            LogUtils.d(TAG, "onPostExecute result:" + result);
            if (result == PROGRESS_FAILED) {
                if (callback != null) {
                    callback.onDetectFinished(null);
                }
            }
        }
    }
}

