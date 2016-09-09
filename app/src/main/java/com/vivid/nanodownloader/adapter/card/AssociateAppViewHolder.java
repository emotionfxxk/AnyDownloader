package com.vivid.nanodownloader.adapter.card;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vivid.nanodownloader.BrowserActivity;
import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.data.AssociateAppData;
import com.vivid.nanodownloader.statistics.StatisticManager;

/**
 * Created by sean on 8/22/16.
 */
public class AssociateAppViewHolder extends BaseCardViewHolder {
    public final ImageView icon;
    public final TextView titleView, subTitleView;
    private AssociateAppData associateAppData;
    public AssociateAppViewHolder(View itemView) {
        super(itemView);
        icon = (ImageView) itemView.findViewById(R.id.icon);
        titleView = (TextView) itemView.findViewById(R.id.title);
        subTitleView = (TextView) itemView.findViewById(R.id.subtitle);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StatisticManager.logClickShortCut(associateAppData.siteUrl);
                BrowserActivity.browserUrl(view.getContext(), associateAppData.siteUrl);
            }
        });
    }

    @Override
    public void bindData(Object data) {
        if (data instanceof AssociateAppData) {
            associateAppData = (AssociateAppData) data;
            icon.setImageResource(associateAppData.appIconResId);
            titleView.setText(associateAppData.appName);
            subTitleView.setText(associateAppData.description);
        } else {
            throw new IllegalArgumentException("Data should be an instance of type 'AssociateAppData'");
        }
    }
}
