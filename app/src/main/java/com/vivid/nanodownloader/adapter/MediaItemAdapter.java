package com.vivid.nanodownloader.adapter;


import android.net.Uri;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.vivid.nanodownloader.DownloadService;
import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.detector.entry.ImageEntry;
import com.vivid.nanodownloader.detector.entry.MediaEntry;
import com.vivid.nanodownloader.detector.entry.VideoEntry;
import com.vivid.nanodownloader.utils.FileUtils;

import java.util.ArrayList;
import java.util.List;

public class MediaItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {
    private final static String TAG = "MediaItemAdapter";
    private List<Object> mediaEntries;

    public MediaItemAdapter(List<MediaEntry> entries) {
        this.mediaEntries = new ArrayList<>();
        mediaEntries.addAll(entries);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.detected_media_item, parent, false);
        final DetectedItemHolder holder = new DetectedItemHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object object = mediaEntries.get(position);
        if (holder instanceof DetectedItemHolder) {
            bindDetectedItem((DetectedItemHolder)holder, (MediaEntry)object);
        }
    }

    private void bindDetectedItem(final DetectedItemHolder holder, MediaEntry mediaEntry) {
        if (mediaEntry.getContentLength() < 0) {
            holder.progressBar.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.VISIBLE);
        } else if (mediaEntry.getContentLength() == 0) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.fileSize.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.fileSize.setVisibility(View.VISIBLE);
            holder.fileSize.setText(FileUtils.getShowSize(mediaEntry.getContentLength()));
        }
        holder.title.setText(mediaEntry.getTitle());
        holder.btnDownload.setTag(mediaEntry);
        holder.btnDownload.setOnClickListener(this);
        holder.videoIcon.setVisibility(View.INVISIBLE);
        if (mediaEntry instanceof VideoEntry) {
            String thumbUrl = ((VideoEntry) mediaEntry).getThumbnail();
            if (thumbUrl != null && !TextUtils.isEmpty(thumbUrl)) {
                Glide.with(holder.itemView.getContext()).load(Uri.parse(thumbUrl))
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                              @Override
                              public boolean onException(Exception e, Uri model,
                                                         Target<GlideDrawable> target,
                                                         boolean isFirstResource) {
                                  return false;
                              }

                              @Override
                              public boolean onResourceReady(GlideDrawable resource, Uri model,
                                                             Target<GlideDrawable> target,
                                                             boolean isFromMemoryCache,
                                                             boolean isFirstResource) {
                                  holder.videoIcon.setVisibility(View.VISIBLE);
                                  return false;
                              }
                          }
                        )
                        .placeholder(R.mipmap.file_icon_picture)
                        .crossFade().centerCrop().into(holder.icon);
            } else {
                holder.icon.setImageResource(R.mipmap.file_icon_video);
            }
        } else if (mediaEntry instanceof ImageEntry) {
            String thumbUrl = mediaEntry.getUrl();
            if (thumbUrl != null && !TextUtils.isEmpty(thumbUrl)) {
                Glide.with(holder.itemView.getContext()).load(Uri.parse(thumbUrl))
                        .placeholder(R.mipmap.file_icon_picture)
                        .crossFade().centerCrop().into(holder.icon);
            } else {
                holder.icon.setImageResource(R.mipmap.file_icon_picture);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mediaEntries.size();
    }

    @Override
    public void onClick(View view) {
        MediaEntry entry = (MediaEntry) view.getTag();
        if (entry != null) {
            DownloadService.intentDownload(view.getContext(), entry.getTitle(),
                    entry.getUrl(),
                    entry.getMimeType(), entry.getContentLength(), null);
            int index = mediaEntries.indexOf(entry);
            if (index != -1) {
                mediaEntries.remove(index);
                notifyItemRemoved(index);
            }
        }

    }

    private final class DetectedItemHolder extends RecyclerView.ViewHolder {
        public final ImageView icon, videoIcon;
        public final TextView title, fileSize;
        public final AppCompatButton btnDownload;
        public final ProgressBar progressBar;
        public DetectedItemHolder(View itemView) {
            super(itemView);
            videoIcon = (ImageView) itemView.findViewById(R.id.play_icon);
            icon = (ImageView) itemView.findViewById(R.id.icon);
            title = (TextView) itemView.findViewById(R.id.title);
            fileSize = (TextView) itemView.findViewById(R.id.file_size);
            btnDownload = (AppCompatButton) itemView.findViewById(R.id.btn_download);
            progressBar = (ProgressBar) itemView.findViewById(R.id.file_size_progressbar);
        }
    }
}
