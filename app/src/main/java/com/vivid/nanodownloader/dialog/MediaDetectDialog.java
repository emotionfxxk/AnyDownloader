package com.vivid.nanodownloader.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vivid.nanodownloader.adapter.MediaItemAdapter;
import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.detector.MediaDetectorAgent;
import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.widget.DividerItemDecorator;

import java.util.List;

/**
 * Created by sean on 7/25/16.
 */
public class MediaDetectDialog extends DialogFragment implements MediaDetectorAgent.Callback {
    private final static String TAG = "MediaDetectDialog";
    private final static String EXTRA_KEY_URL = "extra_url";
    private final static String EXTRA_KEY_TITLE = "extra_title";
    private String url, strTitle;
    private RecyclerView recyclerView;
    private View listInfoGroup, progressInfoGroup;
    private TextView title, subTitle, message;
    private List<MediaEntry> mediaEntries;
    private MediaItemAdapter adapter;
    public static MediaDetectDialog newInstance(String url, String title) {
        MediaDetectDialog fragment = new MediaDetectDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_KEY_URL, url);
        args.putString(EXTRA_KEY_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }
    public MediaDetectDialog() {}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_detect_media, null);
        recyclerView = (RecyclerView) view.findViewById(R.id.video_list);
        subTitle = (TextView) view.findViewById(R.id.subtitle);
        message = (TextView) view.findViewById(R.id.no_video_found);
        listInfoGroup = view.findViewById(R.id.media_info_group);
        progressInfoGroup = view.findViewById(R.id.progress_info_group);
        title = (TextView) view.findViewById(R.id.title);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecorator(getContext()));
        /*
        recyclerView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (mediaEntries != null) {
                    EventBus.getDefault().post(mediaEntries.get(i));
                    MediaDetectDialog.this.dismiss();
                }
            }
        });*/
        Dialog dialog = new Dialog (getContext(), R.style.MaterialDialogSheet);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);


        Bundle bundle = getArguments();
        url = bundle.getString(EXTRA_KEY_URL, null);
        strTitle = bundle.getString(EXTRA_KEY_TITLE, null);
        title.setText(strTitle);
        MediaDetectorAgent.instance().detect(url, strTitle, this);
        return dialog;
    }

    @Override
    public void onDetectStarted() {

    }

    @Override
    public void onDetectFinished(List<MediaEntry> mediaEntries) {
        if (getContext() != null) {
            if (mediaEntries != null && mediaEntries.size() > 0) {
                this.mediaEntries = mediaEntries;
                listInfoGroup.setVisibility(View.VISIBLE);
                progressInfoGroup.setVisibility(View.INVISIBLE);
                subTitle.setText(getContext().getString(R.string.dlg_detect_subtitle,
                        mediaEntries.size()));

                adapter = new MediaItemAdapter(mediaEntries);
                recyclerView.setAdapter(adapter);
            } else {
                progressInfoGroup.setVisibility(View.INVISIBLE);
                message.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onGetContentLength(MediaEntry entry) {
        if (adapter != null) {
            int index = mediaEntries.indexOf(entry);
            adapter.notifyItemChanged(index);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
