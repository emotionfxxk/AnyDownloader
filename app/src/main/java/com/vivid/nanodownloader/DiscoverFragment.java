package com.vivid.nanodownloader;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vivid.nanodownloader.adapter.DiscoverAdapter;
import com.vivid.nanodownloader.adapter.card.CardFactory;
import com.vivid.nanodownloader.data.AssociateAppData;
import com.vivid.nanodownloader.data.CardData;

/**
 * Created by sean on 8/19/16.
 */
public class DiscoverFragment extends Fragment {
    private RecyclerView discoverList;
    private DiscoverAdapter discoverAdapter;
    private int cardSpace;
    public DiscoverFragment() {}
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DiscoverFragment newInstance() {
        DiscoverFragment fragment = new DiscoverFragment();
        /*
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discover, container, false);
        discoverList = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        discoverList.setLayoutManager(new LinearLayoutManager(getContext()));
        cardSpace = getResources().getDimensionPixelSize(R.dimen.card_space);
        discoverList.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                       RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                int pos = parent.getChildAdapterPosition(view);
                int count = parent.getAdapter().getItemCount();
                outRect.top = (pos == 0) ? 2 * cardSpace : cardSpace;
                outRect.bottom = (pos == (count - 1)) ? 2 * cardSpace : cardSpace;
                outRect.left = outRect.right = 2 * cardSpace;
            }
        });
        discoverAdapter = new DiscoverAdapter(getContext());
        discoverList.setAdapter(discoverAdapter);

        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_tumblr,
                        getResources().getString(R.string.app_tumblr_name),
                        getResources().getString(R.string.app_tumblr_description),
                        "com.tumblr",
                        "https://www.tumblr.com/")));
        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_facebook,
                        getResources().getString(R.string.app_facebook_name),
                        getResources().getString(R.string.app_facebook_description),
                        "com.facebook.katana",
                        "https://m.facebook.com")));
        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_insta,
                        getResources().getString(R.string.app_ins_name),
                        getResources().getString(R.string.app_ins_description),
                        "com.instagram.android",
                        "https://www.instagram.com")));
        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_twitter,
                        getResources().getString(R.string.app_twitter_name),
                        getResources().getString(R.string.app_twitter_description),
                        "com.twitter.android",
                        "https://mobile.twitter.com")));
        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_vimeo,
                        getResources().getString(R.string.app_vimeo_name),
                        getResources().getString(R.string.app_vimeo_description),
                        "com.vimeo.android",
                        "https://vimeo.com")));
        discoverAdapter.appendData(new CardData(CardFactory.Type.TYPE_ASSOCIATE_APP,
                new AssociateAppData(R.mipmap.ic_vine,
                        getResources().getString(R.string.app_vine_name),
                        getResources().getString(R.string.app_vine_description),
                        "co.vine.android",
                        "https://vine.co")));
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
