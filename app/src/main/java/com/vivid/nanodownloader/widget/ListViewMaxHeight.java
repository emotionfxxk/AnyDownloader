package com.vivid.nanodownloader.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

import com.vivid.nanodownloader.R;


/**
 * Created by sean on 7/25/16.
 */
public class ListViewMaxHeight extends ListView {
    private final int maxHeight;

    public ListViewMaxHeight(Context context) {
        this(context, null);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListViewMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        maxHeight = context.getResources().getDimensionPixelSize(R.dimen.list_view_max_height);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (maxHeight > 0 && maxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
