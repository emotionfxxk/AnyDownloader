package com.vivid.nanodownloader.setting;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vivid.nanodownloader.R;


/**
 * Created by sean on 8/17/15.
 */
public class CustomPreference extends Preference {
    private TextView mCustomSummary;
    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomPreference(Context context) {
        super(context);
    }
    @Override
    protected View onCreateView(ViewGroup parent) {
        View layout = super.onCreateView(parent);
        mCustomSummary = (TextView) layout.findViewById(R.id.summary);
        if (mCustomSummary != null) {
            mCustomSummary.setVisibility(View.VISIBLE);
            mCustomSummary.setText(getSummary());
        }
        return layout;
    }

    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (mCustomSummary != null) {
            mCustomSummary.setText(getSummary());
        }
    }
}
