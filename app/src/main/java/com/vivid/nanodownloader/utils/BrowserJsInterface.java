package com.vivid.nanodownloader.utils;

public class BrowserJsInterface {
    /*
     * Confuse Android Webkit JS engine,
     * Thus java.lang.Object.getClass() method can not be called in JS
     * to avoid 'Android Webview addJavaScriptInterface exploit'.
     */
    public int getClass = 0;

    public int toString = 0;
}
