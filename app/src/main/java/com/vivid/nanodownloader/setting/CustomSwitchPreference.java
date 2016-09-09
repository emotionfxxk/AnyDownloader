package com.vivid.nanodownloader.setting;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.TextView;

import com.vivid.nanodownloader.R;


/**
 * Created by sean on 8/17/15.
 */
public class CustomSwitchPreference extends SwitchPreference {
    public CustomSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public CustomSwitchPreference(Context context) {
        super(context);
    }

    private TextView mCustomSummary;

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        View checkableView = view.findViewById(R.id.switcher);
        checkableView.setClickable(false);
        if (checkableView instanceof Checkable) {
            ((Checkable) checkableView).setChecked(isChecked());
        }
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        View root = super.onCreateView(parent);
        mCustomSummary = (TextView) root.findViewById(R.id.summary);

        if (mCustomSummary != null) {
            mCustomSummary.setText(getSummary());
        }
        final ViewGroup widgetFrame = (ViewGroup) root
                .findViewById(R.id.widget_frame);
        if (widgetFrame != null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            layoutInflater.inflate(R.layout.list_preference_switch_widget, widgetFrame);
        }
        return root;
    }

    @Override
    public void setSwitchTextOn(CharSequence onText) {
        notifyChanged();
    }

    @Override
    public void setSwitchTextOff(CharSequence offText) {
        notifyChanged();
    }

    public void setSummary(CharSequence summary) {
        super.setSummary(summary);
        if (mCustomSummary != null) {
            mCustomSummary.setVisibility(View.VISIBLE);
            mCustomSummary.setText(getSummary());
        }
    }
}
