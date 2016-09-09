package com.vivid.nanodownloader.statistics;

import android.content.Context;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.vivid.nanodownloader.BuildConfig;

import java.util.Map;


public class StatisticManager {
    private static GoogleAnalytics analytics;
    private static Tracker tracker;
    public static void init(Context appContext) {
        /*
        analytics = GoogleAnalytics.getInstance(appContext);
        tracker = analytics.newTracker(BuildConfig.GA_TRACK_ID);
        tracker.enableExceptionReporting(true);
        tracker.enableAdvertisingIdCollection(true);
        tracker.enableAutoActivityTracking(true);
        tracker.set("&cd1", BuildConfig.CID);*/
    }

    public static void logClickShortCut(String site) {
        /*
        Map<String, String> event = new HitBuilders.EventBuilder()
                .setCategory("click_shortcut")
                .setAction(site)
                .setLabel(site)
                .build();
        tracker.send(event);*/
    }

    public static void logDownloadStart() {
        /*
        Map<String, String> event = new HitBuilders.EventBuilder()
                .setCategory("Download_status")
                .setAction("start")
                .setLabel("start")
                .build();
        tracker.send(event);*/
    }

    public static void logDownloadFailed(String url) {
        /*
        Map<String, String> event = new HitBuilders.EventBuilder()
                .setCategory("Download_status")
                .setAction("failed")
                .setLabel(url)
                .build();
        tracker.send(event);*/
    }

    public static void logDownloadCanceled(String url, boolean isFinished) {
        /*
        Map<String, String> event = new HitBuilders.EventBuilder()
                .setCategory("Download_status")
                .setAction("canceled" + (isFinished ? "finished" : "unfinished"))
                .setLabel(url)
                .build();
        tracker.send(event);*/
    }

    public static void logDownloadFinished(long size, int averageSpeed) {
        /*
        Map<String, String> event = new HitBuilders.EventBuilder()
                .setCategory("Download_status")
                .setAction("finished")
                .setLabel("avgSpeed")
                .setValue(averageSpeed)
                .build();
        tracker.send(event);

        Map<String, String> event1 = new HitBuilders.EventBuilder()
                .setCategory("Download_status")
                .setAction("finished:size")
                .setLabel("size")
                .setValue(size)
                .build();
        tracker.send(event1);*/
    }
}
