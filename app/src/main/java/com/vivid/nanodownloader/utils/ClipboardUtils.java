package com.vivid.nanodownloader.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {
    private static ClipboardManager sClipboardContext;

    public static void init(Context context) {
        sClipboardContext = (ClipboardManager)context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public static String getText() {
        String clipResult = null;
        ClipData primaryClip = sClipboardContext.getPrimaryClip();
        ClipData.Item itemAt = null;
        if (primaryClip != null) {
            itemAt = primaryClip.getItemAt(0);
        }
        if (itemAt != null && itemAt.getText() != null) {
            String trim = itemAt.getText().toString().trim();
            clipResult = trim;
        }
        return clipResult;
    }

    public static void clear() {
        try {
            sClipboardContext.setText(null);
        } catch (Exception e) {}
    }
}
