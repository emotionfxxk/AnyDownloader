package com.vivid.nanodownloader.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.vivid.nanodownloader.R;


/**
 * Created by sean on 8/5/16.
 */
public class DividerItemDecorator extends RecyclerView.ItemDecoration {
    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDivider;
    private int paddingInPixel;

    /**
     * Default divider will be used
     */
    public DividerItemDecorator(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        mDivider = styledAttributes.getDrawable(0);
        styledAttributes.recycle();
        paddingInPixel = context.getResources().getDimensionPixelSize(
                R.dimen.download_list_divider_horizontal_padding);
    }

    /**
     * Custom divider will be used
     */
    public DividerItemDecorator(Context context, int resId) {
        mDivider = ContextCompat.getDrawable(context, resId);
        paddingInPixel = context.getResources().getDimensionPixelSize(
                R.dimen.download_list_divider_horizontal_padding);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft() + paddingInPixel;
        int right = parent.getWidth() - parent.getPaddingRight() - paddingInPixel;

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
