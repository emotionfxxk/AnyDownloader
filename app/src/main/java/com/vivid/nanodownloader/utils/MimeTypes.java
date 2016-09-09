package com.vivid.nanodownloader.utils;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.vivid.nanodownloader.R;

import java.util.HashMap;
import java.util.Map;

public class MimeTypes {
    private static final String MIME_UNKNOWN = "application/octet-stream";
    private Map<String, String> extToMimeMap = new HashMap<>();
    private Map<String, Integer> mimeToIconMap = new HashMap<>();
    private MimeTypes() {}
    private static MimeTypes sInstance;

    public static MimeTypes instance() {
        if (sInstance == null) {
            sInstance = new MimeTypes();
        }
        return sInstance;
    }

    public void init(Context context) {
        try {
            MimeTypeParser mtp = new MimeTypeParser(context);
            XmlResourceParser in = context.getResources().getXml(R.xml.mimetypes);
            mtp.fromXmlResource(this, in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void put(String type, String extension, int icon) {
        put(type, extension);
        mimeToIconMap.put(extension, icon);
    }

    public void put(String type, String extension) {
        extension = extension.toLowerCase();
        extToMimeMap.put(type, extension);
    }

    private static String getExtension(String uri) {
        if (uri == null || TextUtils.isEmpty(uri)) {
            return null;
        }
        int lastDotIndex = uri.lastIndexOf(".");
        if (lastDotIndex >= 0) {
            return uri.substring(lastDotIndex).toLowerCase();
        } else {
            return null;
        }
    }

    public String mimeFromUrl(String url) {
        String extString = getExtension(url);
        String mime = null;
        if (extString != null && !TextUtils.isEmpty(extString)) {
            String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extString.substring(1));
            if (webkitMimeType != null) {
                // Found one. Let's take it!
                mime = webkitMimeType;
            } else {
                mime = extToMimeMap.get(extString);
            }
        }
        return mime != null ? mime : MIME_UNKNOWN;
    }

    public String getMime(String contentType, String url) {
        if (contentType != null && !TextUtils.isEmpty(contentType)
                && !contentType.equals(MIME_UNKNOWN)) {
            return contentType;
        }
        String extString = getExtension(url);
        String mime = null;
        if (extString != null && !TextUtils.isEmpty(extString)) {
            String webkitMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extString.substring(1));
            if (webkitMimeType != null) {
                // Found one. Let's take it!
                mime = webkitMimeType;
            } else {
                mime = extToMimeMap.get(extString);
            }
        }
        return mime != null ? mime : MIME_UNKNOWN;
    }

    public int getIcon(String mimeType) {
        Integer iconResId = mimeToIconMap.get(mimeType);
        return iconResId != null ? iconResId : R.mipmap.file_icon_default;
    }
}
