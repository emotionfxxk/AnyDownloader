package com.vivid.nanodownloader.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;

import com.vivid.nanodownloader.R;

/**
 * Created by sean on 10/10/15.
 */
public class SolidFlatButton extends AppCompatButton {
    private boolean mIsRoundCorner;
    private float mCornerRadius;
    private int mNormalSolidColor, mPressedSolidColor, mNormalTextColor, mPressedTextColor;
    private final static float DEFAULT_PADDING = 16.0f;
    public SolidFlatButton(Context context) {
        super(context);
        loadDefaultAttr(context);
        init();
    }
    public SolidFlatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadDefaultAttr(context);
        loadAttr(context, attrs);
        init();
    }
    public SolidFlatButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadDefaultAttr(context);
        loadAttr(context, attrs);
        init();
    }
    private void loadDefaultAttr(Context context) {
        final float density = getResources().getDisplayMetrics().density;
        mIsRoundCorner = true;
        mCornerRadius = 3.0f * density; // default radius
        mNormalSolidColor =
                context.getResources().getColor(R.color.colorPrimary);
        mPressedSolidColor =
                context.getResources().getColor(R.color.colorPrimaryDark);
        mNormalTextColor = mPressedTextColor =
                context.getResources().getColor(android.R.color.white);

    }
    private void loadAttr(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SolidFlatButton);
        final int count = a.getIndexCount();
        for (int i = 0; i < count; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.SolidFlatButton_roundCorner:
                    mIsRoundCorner = a.getBoolean(attr, true);
                    break;
                case R.styleable.SolidFlatButton_cornerRadius:
                    mCornerRadius = a.getDimension(attr, mCornerRadius);
                    break;
                case R.styleable.SolidFlatButton_normalSolidColor:
                    mNormalSolidColor = a.getColor(attr,
                            context.getResources().getColor(R.color.colorPrimary));
                    break;
                case R.styleable.SolidFlatButton_pressedSolidColor:
                    mPressedSolidColor = a.getColor(attr,
                            context.getResources().getColor(R.color.colorPrimaryDark));
                    break;
                case R.styleable.SolidFlatButton_normalTextColor:
                    mNormalTextColor = a.getColor(attr, mNormalTextColor);
                    break;
                case R.styleable.SolidFlatButton_pressedTextColor:
                    mPressedTextColor = a.getColor(attr, mPressedTextColor);
                    break;
            }
        }
        a.recycle();
    }
    private void init() {
        // set padding
        final float density = getResources().getDisplayMetrics().density;
        int padding = (int) (density * DEFAULT_PADDING);
        setPadding(padding, 0, padding, 0);
        // create & set background drawable
        LayerDrawable bg_normal = (LayerDrawable) getResources().getDrawable(
                R.drawable.solid_flat_button_bg_normal);
        LayerDrawable bg_pressed = (LayerDrawable) getResources().getDrawable(
                R.drawable.solid_flat_button_bg_pressed);
        GradientDrawable normalShape = (GradientDrawable) bg_normal.findDrawableByLayerId(R.id.item);
        GradientDrawable pressedShape = (GradientDrawable)bg_pressed.findDrawableByLayerId(R.id.item);
        normalShape.setColor(mNormalSolidColor);
        pressedShape.setColor(mPressedSolidColor);
        ((GradientDrawable) normalShape.mutate()).setCornerRadius(mIsRoundCorner ? mCornerRadius : 0);
        ((GradientDrawable) pressedShape.mutate()).setCornerRadius(mIsRoundCorner ? mCornerRadius : 0);
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, bg_pressed);
        stateListDrawable.addState(new int[]{}, bg_normal);
        //setBackground(stateListDrawable);
        setBackgroundDrawable(stateListDrawable);
        // set text color
        ColorStateList colorStateList = new ColorStateList(
                new int[][] {new int[] {android.R.attr.state_pressed}, new int[] {}},
                new int[] {mPressedTextColor, mNormalTextColor}
        );
        setTextColor(colorStateList);
    }
}
