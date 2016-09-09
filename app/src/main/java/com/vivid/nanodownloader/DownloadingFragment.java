package com.vivid.nanodownloader;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.vivid.nanodownloader.adapter.DownloadTaskAdapter;
import com.vivid.nanodownloader.dialog.ConfirmDeleteDialog;
import com.vivid.nanodownloader.event.MultiSelectionEvent;
import com.vivid.nanodownloader.model.DownloadModel;
import com.vivid.nanodownloader.utils.LogUtils;
import com.vivid.nanodownloader.widget.DividerItemDecorator;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by sean on 7/4/16.
 */
public class DownloadingFragment extends Fragment implements DownloadTaskAdapter.OnModeChange {
    private RecyclerView downloadingList;
    private DownloadTaskAdapter downloadingAdapter;
    public DownloadingFragment() {
    }
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DownloadingFragment newInstance() {
        DownloadingFragment fragment = new DownloadingFragment();
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_running, container, false);
        downloadingList = (RecyclerView) rootView.findViewById(R.id.downloading_list);
        downloadingList.setLayoutManager(new LinearLayoutManager(getContext()));
        downloadingList.addItemDecoration(new DividerItemDecorator(getContext()));
        downloadingAdapter = new DownloadTaskAdapter(getContext(), this,
                DownloadTaskAdapter.TYPE_DOWNLOADING);
        downloadingList.setAdapter(downloadingAdapter);
        LogUtils.d("SEAN", "onCreateView downloadingAdapter:" + downloadingAdapter);
        DownloadModel.instance().addOnDataChangeListener(downloadingAdapter);
        return rootView;
    }

    @Override
    public void onEnterMultiSelection() {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.startSupportActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    getActivity().getMenuInflater().inflate(R.menu.menu_download_multiselection, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }

                @Override
                public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                    if (item.getItemId() == R.id.action_delete) {
                        final ConfirmDeleteDialog dFragment = new ConfirmDeleteDialog();
                        dFragment.setOnClickOkListener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                LogUtils.d("SEAN", "dFragment:" +
                                        dFragment.isSelectDeleteFileAlso());
                                downloadingAdapter.deleteSelections(dFragment.isSelectDeleteFileAlso());
                            }
                        });
                        dFragment.setOnDismiss(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface var1) {
                                LogUtils.d("SEAN", "onDismiss");
                                mode.finish();
                            }
                        });
                        dFragment.show(getFragmentManager(), ConfirmDeleteDialog.class.getSimpleName());
                        return true;
                    } else if(item.getItemId() == R.id.action_deselect_all) {
                        if (downloadingAdapter != null) {
                            downloadingAdapter.deSelectAll();
                        }
                    } else if(item.getItemId() == R.id.action_select_all) {
                        if (downloadingAdapter != null) {
                            downloadingAdapter.selectAll();
                        }
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    downloadingAdapter.exitMultiSelectionMode();
                }
            });
        }
        EventBus.getDefault().post(new MultiSelectionEvent(true));
    }

    @Override
    public void onExitMultiSelection() {
        EventBus.getDefault().post(new MultiSelectionEvent(false));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DownloadModel.instance().removeOnDataChangeListener(downloadingAdapter);
    }
}
