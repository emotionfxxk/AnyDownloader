package com.vivid.nanodownloader.adapter.card;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vivid.nanodownloader.R;

/**
 * Created by sean on 8/22/16.
 */
public class CardFactory {
    private CardFactory() {}
    public static class Type {
        public final static int TYPE_ASSOCIATE_APP = 0;
    }
    public static BaseCardViewHolder createCardViewHolder(ViewGroup parent, int type) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView;
        switch (type) {
            case Type.TYPE_ASSOCIATE_APP:
                itemView = layoutInflater.inflate(
                        R.layout.card_associate_app, parent, false);
                return new AssociateAppViewHolder(itemView);
        }
        throw new IllegalArgumentException("Not support type:" + type);
    }
}
