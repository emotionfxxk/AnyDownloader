package com.vivid.nanodownloader.utils;

import android.net.Uri;
import android.text.TextUtils;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;

import java.util.UUID;


public class HttpUtils {

    public static String getFileNameFromContentDisposition(String contentDisposition) {
        BasicHeader header = new BasicHeader("Content-Disposition", contentDisposition);
        HeaderElement[] helelms = header.getElements();
        if (helelms.length > 0) {
            HeaderElement helem = helelms[0];
            if (helem.getName().equalsIgnoreCase("attachment")) {
                NameValuePair nmv = helem.getParameterByName("filename");
                if (nmv != null) {
                    return nmv.getValue();
                }
            }
        }
        return null;
    }

    public static String getFilenameFromURL(String url) {
        String filename = null;
        String decodedUrl = Uri.decode(url);
        if (decodedUrl != null) {
            int queryIndex = decodedUrl.indexOf('?');
            if (queryIndex > 0) {
                decodedUrl = decodedUrl.substring(0, queryIndex);
            }
            if (!decodedUrl.endsWith("/")) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedUrl.substring(index);
                }
            }
        }
        if (!TextUtils.isEmpty(filename)) {
            return filename;
        } else {
            return UUID.randomUUID().toString();
        }
    }
}
