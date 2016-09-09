package com.vivid.nanodownloader.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vivid.nanodownloader.DownloadService;
import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.model.DownloadModel;
import com.vivid.nanodownloader.setting.SettingManager;
import com.vivid.nanodownloader.utils.LogUtils;
import com.vivid.nanodownloader.utils.MimeTypes;
import com.vivid.nanodownloader.utils.SizeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Aspsine on 2015/7/8.
 */
public class DownloadTaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener, View.OnLongClickListener,
        DownloadModel.OnDataChangeListener {
    private final static String TAG = "DownloadingAdapter";
    private boolean inMultiSelectionMode = false;
    public final static int TYPE_DOWNLOADING = 0;
    public final static int TYPE_DOWNLOADED = 1;
    private List<DownloadInfo> taskList;
    private HashMap<String, Boolean> selections = new HashMap<>();
    private Handler handler;
    private OnModeChange onModeChange;
    private Context context;
    private int type;

    @Override
    public void onTaskCompleted(String taskId,  int removePos, int insertPos) {
        if (type == TYPE_DOWNLOADING) {
            notifyItemRemoved(removePos);
        } else if (type == TYPE_DOWNLOADED) {
            notifyItemInserted(insertPos);
        }
    }

    @Override
    public void onTaskCanceled(List<String> taskIds) {
        notifyDataSetChanged();
    }

    @Override
    public void onNewTask(String taskId, int insertPos) {
        if (type == TYPE_DOWNLOADING) {
            notifyItemInserted(insertPos);
        }
    }

    public interface OnModeChange {
        void onEnterMultiSelection();
        void onExitMultiSelection();
    }
    public DownloadTaskAdapter(Context context, OnModeChange onModeChange, int type) {
        this.context = context;
        this.onModeChange = onModeChange;
        this.type = type;
        if (type == TYPE_DOWNLOADING) {
            taskList = DownloadModel.instance().getUnFinishedDownloads();
        } else if (type == TYPE_DOWNLOADED) {
            taskList = DownloadModel.instance().getFinishedDownloads();
        }
        handler = new Handler(Looper.getMainLooper());
    }

    public void deleteSelections(boolean deleteFileAlso) {
        if (inMultiSelectionMode) {
            Iterator it = selections.entrySet().iterator();
            ArrayList<String> taskIds = new ArrayList<>();
            while (it.hasNext()) {
                Map.Entry<String, Boolean> entry = (Map.Entry<String, Boolean>) it.next();
                if (entry.getValue()) {
                    taskIds.add(entry.getKey());
                }
            }
            if (taskIds.size() > 0) {
                String[] tasks = new String[taskIds.size()];
                for(int index = 0; index < taskIds.size(); ++index) {
                    tasks[index] = taskIds.get(index);
                }
                DownloadModel.instance().removeTasks(taskIds);
                DownloadService.intentCancel(context, tasks, deleteFileAlso);
            }
        }
    }

    public void selectAll() {
        for (DownloadInfo downloadInfo : taskList) {
            selections.put(downloadInfo.getId(), true);
        }
        notifyDataSetChanged();
    }

    public void deSelectAll() {
        for (DownloadInfo downloadInfo : taskList) {
            selections.put(downloadInfo.getId(), false);
        }
        notifyDataSetChanged();
    }

    public void exitMultiSelectionMode() {
        if (inMultiSelectionMode) {
            selections.clear();
            inMultiSelectionMode = false;
            notifyDataSetChanged();
            onModeChange.onExitMultiSelection();
        }
    }

    public void enterMultiSelectionMode(String selectedTaskId) {
        if (!inMultiSelectionMode) {
            selections.clear();
            for (DownloadInfo downloadInfo : taskList) {
                if (selectedTaskId != null && selectedTaskId.equals(downloadInfo.getId())) {
                    selections.put(selectedTaskId, true);
                } else {
                    selections.put(downloadInfo.getId(), false);
                }

            }
            inMultiSelectionMode = true;
            notifyDataSetChanged();
            onModeChange.onEnterMultiSelection();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (type == TYPE_DOWNLOADING) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.running_item, parent, false);
            final DownloadingViewHolder holder = new DownloadingViewHolder(itemView, handler);
            return holder;
        } else if (type == TYPE_DOWNLOADED) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.finished_item, parent, false);
            final DownloadedViewHolder holder = new DownloadedViewHolder(itemView, handler);
            return holder;
        } else {
            throw new IllegalStateException("Illegal type:" + type);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (type == TYPE_DOWNLOADING) {
            bindData((DownloadingViewHolder) holder, position);
        } else if (type == TYPE_DOWNLOADED) {
            bindData((DownloadedViewHolder) holder, position);
        } else {
            throw new IllegalStateException("Illegal type:" + type);
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    private void bindData(DownloadingViewHolder holder, final int position) {
        final DownloadInfo downloadInfo = taskList.get(position);
        downloadInfo.bindUpdater(holder);
        holder.itemView.setTag(downloadInfo);
        holder.onUpdate(downloadInfo);
    }

    private void bindData(DownloadedViewHolder holder, final int position) {
        final DownloadInfo downloadInfo = taskList.get(position);
        downloadInfo.bindUpdater(holder);
        holder.itemView.setTag(downloadInfo);
        holder.onUpdate(downloadInfo);
    }

    @Override
    public void onClick(View view) {
        LogUtils.d(TAG, "onClick!");
        DownloadInfo downloadInfo = (DownloadInfo)view.getTag();
        if (inMultiSelectionMode) {
            // check or unCheck the item
            selections.put(downloadInfo.getId(), !selections.get(downloadInfo.getId()));
            AppCompatCheckBox checkBox = (AppCompatCheckBox) view.findViewById(R.id.checkbox);
            if (checkBox != null) {
                checkBox.setChecked(selections.get(downloadInfo.getId()));
            }
        } else {
            if (type == TYPE_DOWNLOADING) {
                if (downloadInfo.isPaused() || downloadInfo.isFailed()) {
                    DownloadService.intentResume(context, downloadInfo.getId());
                } else if (downloadInfo.isAutoPaused()) {
                    if (SettingManager.getInstance().isWifiOnlyEnabled()) {
                        //context.startActivity(new Intent(context, SettingActivity.class));
                        Toast.makeText(context,
                                "Settings -> Switch off 'Wifi Network Only' to resume downloading",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        DownloadService.intentResume(context, downloadInfo.getId());
                    }
                } else {
                    DownloadService.intentPause(context, downloadInfo.getId());
                }
            } else if (type == TYPE_DOWNLOADED) {
                try {
                    Intent viewContent = new Intent(Intent.ACTION_VIEW);
                    File file = new File(downloadInfo.getSavePath() + File.separator +
                            downloadInfo.getActualFileName());
                    Uri uri = Uri.fromFile(file);
                    String mime = downloadInfo.getMimeType();
                    if (mime.contains("video")) {
                        mime = "video/*";
                    } else if (mime.contains("audio")) {
                        mime = "audio/*";
                    }
                    LogUtils.d(TAG, "mime:" + mime + ", uri:" + uri.toString());
                    viewContent.setDataAndType(
                            uri,
                            mime);
                    context.startActivity(viewContent);
                } catch (Exception e) {
                    //Toast.makeText(context, R.string.toast_no_matching_app, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View view) {
        LogUtils.d(TAG, "onLongClick!");
        DownloadInfo downloadInfo = (DownloadInfo)view.getTag();
        if (!inMultiSelectionMode) {
            enterMultiSelectionMode(downloadInfo.getId());
        }
        return false;
    }

    private final class DownloadingViewHolder extends RecyclerView.ViewHolder
            implements DownloadInfo.Updater {
        public final ImageView icon, statusIcon;
        public final TextView title, downloadPerSize, status;
        public final ProgressBar downloadingProgress;
        private Handler handler;
        private AppCompatCheckBox checkBox;
        public DownloadingViewHolder(View itemView, Handler handler) {
            super(itemView);
            this.handler = handler;
            icon = (ImageView) itemView.findViewById(R.id.icon);
            statusIcon = (ImageView) itemView.findViewById(R.id.status_icon);
            checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
            title = (TextView) itemView.findViewById(R.id.title);
            downloadPerSize = (TextView) itemView.findViewById(R.id.download_per_size);
            status = (TextView) itemView.findViewById(R.id.status);
            downloadingProgress = (ProgressBar) itemView.findViewById(R.id.progressBar);
        }

        @Override
        public void onUpdate(final DownloadInfo downloadInfo) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkBox.setVisibility(inMultiSelectionMode ? View.VISIBLE : View.GONE);
                        if (inMultiSelectionMode) {
                            checkBox.setChecked(selections.get(downloadInfo.getId()));
                        }
                        itemView.setOnClickListener(DownloadTaskAdapter.this);
                        itemView.setOnLongClickListener(DownloadTaskAdapter.this);
                        icon.setImageResource(MimeTypes.instance().getIcon(downloadInfo.getMimeType()));
                        title.setText(downloadInfo.getActualFileName());
                        int progress = 0;
                        if (downloadInfo.getTotalSize() != 0) {
                            progress = (int) ((float) (downloadInfo.getFinishedSize() * 100) / downloadInfo.getTotalSize());
                        }
                        downloadingProgress.setProgress(progress);
                        downloadPerSize.setText(SizeFormatter.getDownloadPerSize(
                                downloadInfo.getFinishedSize(), downloadInfo.getTotalSize()));
                        int downloadStatus = downloadInfo.getStatus() ;
                        if (downloadStatus == DownloadStatus.STATUS_COMPLETED) {
                            status.setText(R.string.download_status_completed);
                        } else if (downloadStatus == DownloadStatus.STATUS_PAUSED ||
                                downloadStatus == DownloadStatus.STATUS_AUTO_PAUSED) {
                            status.setText(R.string.download_status_pause);
                        } else if (downloadStatus == DownloadStatus.STATUS_PROGRESS) {
                            status.setText(context.getResources().getString(
                                    R.string.download_status_speed,
                                    Formatter.formatFileSize(context, downloadInfo.getSpeed())));
                        } else if (downloadStatus == DownloadStatus.STATUS_FAILED) {
                            status.setText(R.string.download_status_error);
                        } else {
                            status.setText(R.string.download_status_pending);
                        }
                        if (downloadStatus == DownloadStatus.STATUS_PAUSED ||
                                downloadStatus == DownloadStatus.STATUS_AUTO_PAUSED) {
                            statusIcon.setImageResource(R.mipmap.ic_continue);
                        } else {
                            statusIcon.setImageResource(R.mipmap.ic_pause);
                        }
                    } catch (Exception e) {}
                }
            });
        }
    }

    private final class DownloadedViewHolder extends RecyclerView.ViewHolder
            implements DownloadInfo.Updater {
        public final ImageView icon, playIcon;
        public final TextView title, fileSize, date;
        private Handler handler;
        private AppCompatCheckBox checkBox;
        public DownloadedViewHolder(View itemView, Handler handler) {
            super(itemView);
            this.handler = handler;
            playIcon = (ImageView) itemView.findViewById(R.id.play_icon);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            checkBox = (AppCompatCheckBox) itemView.findViewById(R.id.checkbox);
            title = (TextView) itemView.findViewById(R.id.title);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            date = (TextView) itemView.findViewById(R.id.date);
        }

        @Override
        public void onUpdate(final DownloadInfo downloadInfo) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        checkBox.setVisibility(inMultiSelectionMode ? View.VISIBLE : View.GONE);
                        if (inMultiSelectionMode) {
                            checkBox.setChecked(selections.get(downloadInfo.getId()));
                        }
                        itemView.setOnClickListener(DownloadTaskAdapter.this);
                        itemView.setOnLongClickListener(DownloadTaskAdapter.this);
                        playIcon.setVisibility(View.INVISIBLE);
                        LogUtils.d(TAG, "downloadInfo.getSavePath():" + downloadInfo.getSavePath() +
                                ", name:" + downloadInfo.getActualFileName());
                        if (downloadInfo.getMimeType().contains("image") ||
                                downloadInfo.getMimeType().contains("video")) {
                            Glide.with(itemView.getContext()).load(new File(
                                    downloadInfo.getSavePath() + File.separator + downloadInfo.getActualFileName()))
                                    .listener(new RequestListener<File, GlideDrawable>() {
                                        @Override
                                        public boolean onException(Exception e, File model,
                                                                   Target<GlideDrawable> target,
                                                                   boolean isFirstResource) {
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(GlideDrawable resource,
                                                                       File model,
                                                                       Target<GlideDrawable> target,
                                                                       boolean isFromMemoryCache,
                                                                       boolean isFirstResource) {
                                            if (downloadInfo.getMimeType().contains("video")) {
                                                playIcon.setVisibility(View.VISIBLE);
                                            }
                                            return false;
                                        }
                                    })
                                    .placeholder(MimeTypes.instance().getIcon(downloadInfo.getMimeType()))
                                    .crossFade().centerCrop().into(icon);
                        } else {
                            icon.setImageResource(MimeTypes.instance().getIcon(downloadInfo.getMimeType()));
                        }
                        title.setText(downloadInfo.getActualFileName());
                        fileSize.setText(Formatter.formatFileSize(context, downloadInfo.getTotalSize()));
                        long now = System.currentTimeMillis();
                        long difference = now - downloadInfo.getFinishedTime();
                        String relativeDateString = (difference >= 0 && difference <= DateUtils.MINUTE_IN_MILLIS) ?
                                context.getString(R.string.downloaded_date_just_now) :
                                DateUtils.getRelativeTimeSpanString(
                                        downloadInfo.getFinishedTime(),
                                        now,
                                        DateUtils.MINUTE_IN_MILLIS,
                                        DateUtils.FORMAT_ABBREV_RELATIVE).toString();
                        date.setText(relativeDateString);
                    } catch (Exception e) {}
                }
            });
        }
    }
}
