package com.vivid.nanodownloader.event;

/**
 * Created by sean on 8/5/16.
 */
public class HtmlAbstractEvent {
    public String url;
    public String rawHtml;
    public HtmlAbstractEvent(String url, String rawHtml) {
        this.url = url;
        this.rawHtml = rawHtml;
    }
}
