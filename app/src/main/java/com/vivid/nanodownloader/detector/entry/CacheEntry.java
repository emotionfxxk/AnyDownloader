package com.vivid.nanodownloader.detector.entry;

import java.util.List;

/**
 * Created by sean on 7/25/16.
 */
public class CacheEntry {
    private final static long DEFAULT_EXPIRE_DURATION = 3600000; // default set as 1 hour
    private final List<MediaEntry> entries;
    private final long expireDuration;
    private final long createTime;
    public CacheEntry(List<MediaEntry> entries) {
        this.entries = entries;
        createTime = System.currentTimeMillis();
        expireDuration = DEFAULT_EXPIRE_DURATION;
    }
    public CacheEntry(List<MediaEntry> entries, long expireDuration) {
        this.entries = entries;
        createTime = System.currentTimeMillis();
        this.expireDuration = expireDuration;
    }
    public boolean isExpired() {
        return (System.currentTimeMillis() - createTime) > expireDuration;
    }
    public List<MediaEntry> getEntries() {
        return entries;
    }
}
