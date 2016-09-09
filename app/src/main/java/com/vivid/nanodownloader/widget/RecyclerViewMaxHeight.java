package com.vivid.nanodownloader.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.vivid.nanodownloader.R;


/**
 * Created by sean on 7/25/16.
 */
public class RecyclerViewMaxHeight extends RecyclerView {
    private final int maxHeight;

    public RecyclerViewMaxHeight(Context context) {
        this(context, null);
    }

    public RecyclerViewMaxHeight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewMaxHeight(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        maxHeight = context.getResources().getDimensionPixelSize(R.dimen.recycler_view_max_height);
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
