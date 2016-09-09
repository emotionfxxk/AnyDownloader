package com.vivid.nanodownloader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.adapter.card.BaseCardViewHolder;
import com.vivid.nanodownloader.adapter.card.CardFactory;
import com.vivid.nanodownloader.data.CardData;

import java.util.ArrayList;
import java.util.List;

public class DiscoverAdapter extends RecyclerView.Adapter<BaseCardViewHolder> {
    private final static String TAG = "DiscoverAdapter";
    private Context context;
    private List<CardData> cardDataList = new ArrayList<>();
    public DiscoverAdapter(Context context) {
        this.context = context;
    }

    public void appendData(CardData cardData) {
        cardDataList.add(cardData);
        notifyItemInserted(cardDataList.size() - 1);
    }

    public void insertData(CardData cardData, int index) {
        cardDataList.add(index, cardData);
        notifyItemInserted(index);
    }

    public void setCardDataList(List<CardData> cards) {
        cardDataList.clear();
        cardDataList.addAll(cards);
        notifyDataSetChanged();
    }

    @Override
    public BaseCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseCardViewHolder holder = CardFactory.createCardViewHolder(parent, viewType);
        if (holder == null) {
            throw new IllegalStateException("Illegal type:" + viewType);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(BaseCardViewHolder holder, int position) {
        holder.bindData(cardDataList.get(position).data);
    }

    @Override
    public int getItemCount() {
        return cardDataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return cardDataList.size() > 0 ? cardDataList.get(position).type : 0;
    }
}
