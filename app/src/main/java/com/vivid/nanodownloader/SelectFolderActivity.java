package com.vivid.nanodownloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.vivid.nanodownloader.setting.DownloadConfigure;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SelectFolderActivity extends AppCompatActivity {

    private static class FolderFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            return file != null && file.exists() && file.isDirectory();
        }
    }

    private static class ItemData {

        public ItemData(String name, int count, File file) {
            this.name = name;
            this.count = count;
            this.file = file;
        }

        String name;
        int count;
        File file;
    }

    private class FolderAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubFolders.size();
        }

        @Override
        public ItemData getItem(int i) {
            File file = mSubFolders.get(i);
            String name = file.getName();
            int count;
            if (file.canWrite()) {
                File[] childFiles = file.listFiles(new FolderFilter());
                count = childFiles == null ? -1 : childFiles.length;
            } else {
                count = -1;
            }
            return new ItemData(name, count, file);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v;
            if (view != null) {
                v = view;
            } else {
                v = getLayoutInflater().inflate(R.layout.file_list_item, viewGroup, false);
            }
            TextView titleTV = (TextView) v.findViewById(R.id.title);
            TextView subtitleTV = (TextView) v.findViewById(R.id.subtitle);
            String name = getItem(i).name;
            int count = getItem(i).count;
            titleTV.setText(name);
            if (count < 0) {
                subtitleTV.setText(R.string.select_folder_no_permission);
            } else if (count == 0) {
                subtitleTV.setText(R.string.select_folder_count_none);
            } else {
                subtitleTV.setText(getString(R.string.select_folder_count, count));
            }
            return v;
        }
    }

    private TextView mPathTV;
    private FolderAdapter mAdapter;

    private File mCurFolder;
    private List<File> mSubFolders;
    private File mBaseFolder;

    private void changeFolder(File toFile) {
        if (!toFile.exists()) {
            return;
        }
        if (!toFile.canWrite()) {
            Toast.makeText(this, R.string.select_folder_no_permission_to_entry, Toast.LENGTH_SHORT).show();
            return;
        }
        mCurFolder = toFile;
        mSubFolders.clear();

        File[] subFiles = mCurFolder.listFiles(new FolderFilter());
        if (subFiles != null) {
            for (File f : subFiles) {
                mSubFolders.add(f);
            }
        }
        Collections.sort(mSubFolders, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && o2.isFile())
                    return -1;
                if (o1.isFile() && o2.isDirectory())
                    return 1;
                return o1.getName().compareTo(o2.getName());
            }
        });
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        mPathTV.setText(mCurFolder.getAbsolutePath());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_folder);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        String path = intent.getStringExtra("basePath");
        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        mCurFolder = new File(path);
        if (!mCurFolder.exists()) {
            finish();
            return;
        }

        mPathTV = (TextView) findViewById(R.id.path);
        mBaseFolder = mCurFolder;

        mSubFolders = new ArrayList<>();
        changeFolder(mCurFolder);
        mAdapter = new FolderAdapter();

        ListView lv = (ListView) findViewById(R.id.list);
        View emptyView = findViewById(R.id.empty_view);
        lv.setEmptyView(emptyView);
        lv.setAdapter(mAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                changeFolder(mAdapter.getItem(i).file);
            }
        });
        findViewById(R.id.bottom_btn).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                DownloadConfigure.setDownloadPath(mCurFolder.getAbsolutePath());
                finish();
            }
        });
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!mCurFolder.equals(mBaseFolder)) {
                changeFolder(mCurFolder.getParentFile());
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //treat as back
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
