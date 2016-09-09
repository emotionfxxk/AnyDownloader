package com.vivid.nanodownloader;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.vivid.nanodownloader.dialog.MediaDetectDialog;
import com.vivid.nanodownloader.event.HtmlAbstractEvent;
import com.vivid.nanodownloader.utils.BrowserJsInterface;
import com.vivid.nanodownloader.utils.LogUtils;
import com.vivid.nanodownloader.utils.UrlUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.URL;

public class BrowserActivity extends AppCompatActivity {
    private final static String TAG = "BrowserActivity";
    private final static String EXTRA_KEY_URL = "url";
    private final static String DEFAULT_URL = "https://www.instagram.com";
    private WebView webView;
    private ProgressBar progressBar;
    private SearchView address;
    private String websiteTitle;
    private LinearLayout bannerContainer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isLoading = true;
    private AppCompatImageButton btnBackward, btnForward, btnHome, btnDownload;

    public static void browserUrl(Context context, String url) {
        Intent intent = new Intent(context, BrowserActivity.class);
        if (url != null) {
            intent.putExtra(BrowserActivity.EXTRA_KEY_URL, url);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browser);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        webView = (WebView) findViewById(R.id.web);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        btnBackward = (AppCompatImageButton) findViewById(R.id.btn_back);
        btnForward = (AppCompatImageButton) findViewById(R.id.btn_forward);
        btnHome = (AppCompatImageButton) findViewById(R.id.btn_home);
        btnDownload = (AppCompatImageButton) findViewById(R.id.btn_download);
        address = (SearchView) findViewById(R.id.address);
        bannerContainer = (LinearLayout) findViewById(R.id.banner_container);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String host = null;
                try {
                    URL currentUrl = new URL(webView.getUrl());
                    LogUtils.d(TAG, "host:" + currentUrl.getHost());
                    host =  currentUrl.getHost();
                } catch (Exception e) {}
                if (host != null && (host.equals("m.youtube.com") || host.equals("www.youtube.com"))) {
                    Toast.makeText(BrowserActivity.this,
                            R.string.toast_youtube_not_support, Toast.LENGTH_LONG).show();
                    return;
                }
                MediaDetectDialog detectDialog =
                        MediaDetectDialog.newInstance(webView.getUrl(), websiteTitle);
                detectDialog.show(getSupportFragmentManager(),
                        MediaDetectDialog.class.getSimpleName());
                if (!isLoading) {
                    loadScript();
                }
            }
        });

        btnBackward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoBack()) {
                    webView.goBack();
                }
            }
        });
        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (webView.canGoForward()) {
                    webView.goForward();
                }
            }
        });
        address.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                LogUtils.d(TAG, "onQueryTextSubmit query:" + query);
                address.clearFocus();
                webView.loadUrl(UrlUtils.prefixHttpScheme(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        address.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                LogUtils.d(TAG, "onFocusChange:" + b);
                if (b) {
                    address.setQuery(webView.getUrl(), false);
                } else {
                    if (websiteTitle != null) {
                        address.setQuery(websiteTitle, false);
                    }
                }
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setDomStorageEnabled(true);
        webView.addJavascriptInterface(new HtmlAbstractor(), "HtmlViewer");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                LogUtils.d(TAG, "shouldOverrideUrlLoading:" + url);
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                LogUtils.d(TAG, "onPageStarted:" + url);
                super.onPageStarted(view, url, favicon);
                progressBar.setProgress(0);
                progressBar.setVisibility(View.VISIBLE);
                updateBackForwardState();
                isLoading = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished:" + url);
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.INVISIBLE);
                updateBackForwardState();
                isLoading = false;
                loadScript();
            }
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                LogUtils.d(TAG, "onReceivedError:" + error);
                isLoading = false;
                loadScript();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
            }
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                websiteTitle = title;
                if (!address.isFocused()) {
                    address.setQuery(title, false);
                }
            }
        });

        Intent intent = getIntent();
        String url = DEFAULT_URL;
        if (intent != null && intent.hasExtra(EXTRA_KEY_URL)) {
            url = intent.getStringExtra(EXTRA_KEY_URL);
            LogUtils.d(TAG, "onCreate url:" + url);
        }

        webView.loadUrl(url);
        address.setQuery(url, false);
        address.clearFocus();
        updateBackForwardState();
    }

    private void loadScript() {
        webView.loadUrl("javascript:window.HtmlViewer.showHTML" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
    }

    private class HtmlAbstractor extends BrowserJsInterface {
        public HtmlAbstractor() {}
        @JavascriptInterface
        public void showHTML(final String html) {
            LogUtils.d(TAG, "showHTML --------->");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    EventBus.getDefault().post(new HtmlAbstractEvent(webView.getUrl(), html));
                }
            });
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.destroy();
        }
    }

    private void updateBackForwardState() {
        Log.d(TAG, "updateBackForwardState() result.canGoBack:" + webView.canGoBack() +
                ", result.canGoForward():" + webView.canGoForward());
        btnBackward.setEnabled(webView.canGoBack());
        btnForward.setEnabled(webView.canGoForward());
    }
}
