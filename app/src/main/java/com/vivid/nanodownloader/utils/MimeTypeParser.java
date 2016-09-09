package com.vivid.nanodownloader.utils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MimeTypeParser {
    //private static final String LOG_TAG = "MimeTypeParser";
    public static final String TAG_MIME_TYPES = "MimeTypes";
    public static final String TAG_TYPE = "type";
    public static final String ATTR_EXTENSION = "extension";
    public static final String ATTR_MIME_TYPE = "mimeType";
    public static final String ATTR_ICON = "icon";

    private XmlPullParser mXpp;
    private MimeTypes mMimeTypes;
    private Resources resources;
    private String packageName;

    public MimeTypeParser(Context ctx) throws NameNotFoundException {
        packageName = ctx.getPackageName();
        resources = ctx.getPackageManager().getResourcesForApplication(packageName);
    }

    public void fromXmlResource(MimeTypes mimeTypes, XmlResourceParser in)
            throws XmlPullParserException, IOException {
        mXpp = in;
        mMimeTypes = mimeTypes;
        parse();
    }

    public void parse()
        throws XmlPullParserException, IOException {
        int eventType = mXpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tag = mXpp.getName();
            if (eventType == XmlPullParser.START_TAG) {
                if (tag.equals(TAG_TYPE)) {
                    addMimeTypeStart();
                }
            }
            eventType = mXpp.next();
        }
    }

    private void addMimeTypeStart() {
        String extension = mXpp.getAttributeValue(null, ATTR_EXTENSION);
        String mimeType = mXpp.getAttributeValue(null, ATTR_MIME_TYPE);
        String icon = mXpp.getAttributeValue(null, ATTR_ICON);
        if(icon != null){
            int id = resources.getIdentifier(icon.substring(1) /* to cut the @ */, null, packageName);
            if (id > 0) {
                mMimeTypes.put(extension, mimeType, id);
                return;
            }
        }
        mMimeTypes.put(extension, mimeType);
    }

}
