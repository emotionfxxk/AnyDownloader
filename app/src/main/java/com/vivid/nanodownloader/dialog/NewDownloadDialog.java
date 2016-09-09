package com.vivid.nanodownloader.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vivid.nanodownloader.DownloadService;
import com.vivid.nanodownloader.R;
import com.vivid.nanodownloader.utils.ClipboardUtils;
import com.vivid.nanodownloader.utils.LogUtils;
import com.vivid.nanodownloader.utils.MimeTypes;
import com.vivid.nanodownloader.utils.UrlVerifier;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by sean on 7/21/16.
 */
public class NewDownloadDialog extends DialogFragment {
    private final static String TAG = "NewDownloadDialog";
    private String url;
    private View contentInput, contentProgress, contentFileInfo, errorInfo;
    private AppCompatButton btnNext, btnCancelVerify;
    private AppCompatEditText urlInput;
    private UrlVerifier verifier;
    private UrlVerifier.ResultEvent event;

    private ImageView fileIcon;
    private TextView fileName, fileSize;

    public static NewDownloadDialog newInstance() {
        NewDownloadDialog fragment = new NewDownloadDialog();
        /*
        Bundle args = new Bundle();
        args.putString(EXTRA_KEY_URL, url);
        args.putString(EXTRA_KEY_TITLE, title);
        fragment.setArguments(args);*/
        return fragment;
    }
    public NewDownloadDialog() {}
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_download, null);
        contentProgress = view.findViewById(R.id.content_progress);
        contentFileInfo = view.findViewById(R.id.content_file_info);
        contentInput = view.findViewById(R.id.content_input);
        errorInfo = view.findViewById(R.id.content_error);

        // for input content
        btnNext = (AppCompatButton)view.findViewById(R.id.btn_next);
        urlInput = (AppCompatEditText)view.findViewById(R.id.input);
        String textInClipboard = ClipboardUtils.getText();
        LogUtils.d(TAG, "textInClipboard:" + textInClipboard);
        if (textInClipboard != null && !TextUtils.isEmpty(textInClipboard) &&
                URLUtil.isValidUrl(textInClipboard)) {
            urlInput.setText(textInClipboard);
            btnNext.setEnabled(true);
        }
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentInput.setVisibility(View.GONE);
                contentProgress.setVisibility(View.VISIBLE);
                if (verifier != null) {
                    verifier.cancel();
                }
                verifier = new UrlVerifier();
                verifier.doVerify(url);
            }
        });
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                url = charSequence.toString();
                btnNext.setEnabled(!TextUtils.isEmpty(url));
            }
            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        // for progress content
        btnCancelVerify = (AppCompatButton)view.findViewById(R.id.btn_cancel_verify);
        btnCancelVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        // for file info
        fileIcon = (ImageView) view.findViewById(R.id.file_icon);
        fileName = (TextView) view.findViewById(R.id.file_name);
        fileSize = (TextView) view.findViewById(R.id.file_size);
        AppCompatButton btnInfoCancel = (AppCompatButton) view.findViewById(R.id.btn_cancel_file_info);
        AppCompatButton btnInfoOk = (AppCompatButton) view.findViewById(R.id.btn_ok_file_info);
        btnInfoCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btnInfoOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // start download here!
                DownloadService.intentDownload(getContext(), fileName.getText().toString(),
                        event.finalUrl,
                        event.contentType, event.fileSize, null);
                dismiss();
            }
        });

        AppCompatButton btnErrorCancel = (AppCompatButton) view.findViewById(R.id.btn_cancel_error);
        btnErrorCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        Dialog dialog = new Dialog (getContext(), R.style.MaterialDialogSheet);
        dialog.setContentView(view);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (verifier != null) {
            verifier.cancel();
            verifier = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(UrlVerifier.ResultEvent event) {
        LogUtils.d(TAG, "onEvent UrlVerifier.ResultEvent:" + event.isValidUrl);
        if (event.isValidUrl && event.fileSize > 0) {
            contentProgress.setVisibility(View.GONE);
            contentFileInfo.setVisibility(View.VISIBLE);
            fileIcon.setImageResource(MimeTypes.instance().getIcon(event.contentType));
            fileName.setText(event.fileName);
            fileSize.setText(Formatter.formatFileSize(getContext(), event.fileSize));
            this.event = event;
        } else {
            contentProgress.setVisibility(View.GONE);
            errorInfo.setVisibility(View.VISIBLE);
        }
    }
}
