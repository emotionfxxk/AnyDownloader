package com.vivid.nanodownloader.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.vivid.nanodownloader.R;

/**
 * Created by sean on 7/21/16.
 */
public class ConfirmDeleteDialog extends DialogFragment {
    private DialogInterface.OnClickListener onClickOk;
    private DialogInterface.OnDismissListener onDismiss;
    private AppCompatCheckBox checkBox;
    public void setOnClickOkListener(DialogInterface.OnClickListener onClickOk) {
        this.onClickOk = onClickOk;
    }
    public void setOnDismiss(DialogInterface.OnDismissListener onDismiss) {
        this.onDismiss = onDismiss;
    }
    public boolean isSelectDeleteFileAlso() {
        return checkBox.isChecked();
    }

    public static ConfirmDeleteDialog newInstance() {
        ConfirmDeleteDialog fragment = new ConfirmDeleteDialog();
        return fragment;
    }

    public ConfirmDeleteDialog() {}

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_confirm_delete, null);
        checkBox = (AppCompatCheckBox) dialogView.findViewById(R.id.checkbox);
        AppCompatButton btnOk = (AppCompatButton)dialogView.findViewById(R.id.btn_ok);
        AppCompatButton btnCancel = (AppCompatButton)dialogView.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                onClickOk.onClick(getDialog(), 0);
            }
        });

        Dialog dialog = new Dialog (getContext(), R.style.MaterialDialogSheet);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismiss != null) {
            onDismiss.onDismiss(dialog);
        }
    }
}
