package com.vivid.nanodownloader.adapter.card;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by sean on 8/22/16.
 */
public abstract class BaseCardViewHolder extends RecyclerView.ViewHolder {
    public BaseCardViewHolder(View itemView) {
        super(itemView);
    }
    public abstract void bindData(Object data);
}
