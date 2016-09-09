package com.vivid.nanodownloader.data;

/**
 * Created by sean on 8/23/16.
 */
public class AssociateAppData {
    public final int appIconResId;
    public final String appName;
    public final String description;
    public final String packageName;
    public final String siteUrl;
    public AssociateAppData(int appIconResId, String appName, String description,
                            String packageName, String url) {
        this.appIconResId = appIconResId;
        this.appName = appName;
        this.description = description;
        this.packageName = packageName;
        this.siteUrl = url;
    }
}
