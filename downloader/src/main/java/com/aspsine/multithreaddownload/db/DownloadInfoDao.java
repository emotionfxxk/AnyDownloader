package com.aspsine.multithreaddownload.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.aspsine.multithreaddownload.DownloadInfo;
import com.aspsine.multithreaddownload.architecture.DownloadStatus;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by aspsine on 15-4-19.
 */
public class DownloadInfoDao extends AbstractDao<ThreadInfo> {
    private static final String TABLE_NAME = "TaskInfo";

    private static final String COL_NAME_ID = "_id";
    private static final String COL_NAME_URL = "_url";
    private static final String COL_NAME_ORIG_NAME = "_origName";
    private static final String COL_NAME_ACTUAL_NAME = "_actualName";
    private static final String COL_NAME_MIME = "_mime";
    private static final String COL_NAME_SAVE_PATH = "_savePath";
    private static final String COL_NAME_TOTAL_SIZE = "_totalSize";
    private static final String COL_NAME_FINISHED_SIZE = "_finishedSize";
    private static final String COL_NAME_STATUS = "_status";
    private static final String COL_NAME_THUMB_URL = "_thumbUrl";
    private static final String COL_NAME_START_TIME = "_startTime";
    private static final String COL_NAME_FINISHED_TIME = "_finishedTime";

    public DownloadInfoDao(Context context) {
        super(context);
    }

    public static void createTable(SQLiteDatabase db) {
        StringBuilder createSqlBuilder = new StringBuilder();
        createSqlBuilder.append("create table ").append(TABLE_NAME)
                .append("(")
                .append(COL_NAME_ID).append(" text primary key, ")
                .append(COL_NAME_URL).append(" text, ")
                .append(COL_NAME_ORIG_NAME).append(" text, ")
                .append(COL_NAME_ACTUAL_NAME).append(" text, ")
                .append(COL_NAME_MIME).append(" text, ")
                .append(COL_NAME_SAVE_PATH).append(" text, ")
                .append(COL_NAME_TOTAL_SIZE).append(" long, ")
                .append(COL_NAME_FINISHED_SIZE).append(" long, ")
                .append(COL_NAME_STATUS).append(" integer, ")
                .append(COL_NAME_THUMB_URL).append(" text, ")
                .append(COL_NAME_START_TIME).append(" long, ")
                .append(COL_NAME_FINISHED_TIME).append(" long")
                .append(")");
        db.execSQL(createSqlBuilder.toString());
    }

    public static void dropTable(SQLiteDatabase db) {
        db.execSQL("drop table if exists " + TABLE_NAME);
    }

    public void insert(DownloadInfo info) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder insertSqlBuilder = new StringBuilder();
        insertSqlBuilder.append("insert into ").append(TABLE_NAME)
                .append("(")
                .append(COL_NAME_ID).append(", ")
                .append(COL_NAME_URL).append(", ")
                .append(COL_NAME_ORIG_NAME).append(", ")
                .append(COL_NAME_ACTUAL_NAME).append(", ")
                .append(COL_NAME_MIME).append(", ")
                .append(COL_NAME_SAVE_PATH).append(", ")
                .append(COL_NAME_TOTAL_SIZE).append(", ")
                .append(COL_NAME_FINISHED_SIZE).append(", ")
                .append(COL_NAME_STATUS).append(", ")
                .append(COL_NAME_THUMB_URL).append(", ")
                .append(COL_NAME_START_TIME).append(", ")
                .append(COL_NAME_FINISHED_TIME)
                .append(")")
                .append(" values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        db.execSQL(insertSqlBuilder.toString(),
                new Object[] {
                        info.getId(), info.getUrl(), info.getOriginalFileName(),
                        info.getActualFileName(), info.getMimeType(), info.getSavePath(),
                        info.getTotalSize(), info.getFinishedSize(), info.getStatus(),
                        info.getThumbnailUrl(), info.getStartTime(), info.getFinishedTime()
                        });
    }

    public void delete(DownloadInfo downloadInfo) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder deleteSqlBuilder = new StringBuilder();
        deleteSqlBuilder.append("delete from ").append(TABLE_NAME)
                .append(" where ")
                .append(COL_NAME_ID).append(" = ?");
        db.execSQL(deleteSqlBuilder.toString(),
                new Object[] { downloadInfo.getId() });
    }

    public void delete(String taskId) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder deleteSqlBuilder = new StringBuilder();
        deleteSqlBuilder.append("delete from ").append(TABLE_NAME)
                .append(" where ")
                .append(COL_NAME_ID).append(" = ?");
        db.execSQL(deleteSqlBuilder.toString(),
                new Object[] { taskId });
    }

    public void update(DownloadInfo downloadInfo) {
        SQLiteDatabase db = getWritableDatabase();
        StringBuilder updateSqlBuilder = new StringBuilder();
        updateSqlBuilder.append("update ").append(TABLE_NAME)
                .append(" set ")
                .append(COL_NAME_STATUS).append(" = ?, ")
                .append(COL_NAME_FINISHED_SIZE).append(" = ?, ")
                .append(COL_NAME_FINISHED_TIME).append(" = ?")
                .append(" where ")
                .append(COL_NAME_ID).append(" = ?");
        db.execSQL(updateSqlBuilder.toString(),
                new Object[] {
                        downloadInfo.getStatus(),
                        downloadInfo.getFinishedSize(), downloadInfo.getFinishedTime(),
                        downloadInfo.getId(),
                });
    }

    public List<DownloadInfo> getTaskInfos() {
        List<DownloadInfo> list = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME, new String[]{});
        createDownloadTasks(list, cursor);
        return list;
    }

    public List<DownloadInfo> getFinishedDownloadTasks() {
        List<DownloadInfo> list = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME +
                " where " + COL_NAME_STATUS + " = ?",
                new String[] { String.valueOf(DownloadStatus.STATUS_COMPLETED) });
        createDownloadTasks(list, cursor);
        cursor.close();
        return list;
    }

    public List<DownloadInfo> getUnfinishedDownloadTasks() {
        List<DownloadInfo> list = new LinkedList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TABLE_NAME +
                " where " + COL_NAME_STATUS + " <> ?",
                new String[] { String.valueOf(DownloadStatus.STATUS_COMPLETED) });
        createDownloadTasks(list, cursor);
        cursor.close();
        return list;
    }

    private void createDownloadTasks(List<DownloadInfo> list, Cursor cursor) {
        while (cursor.moveToNext()) {
            DownloadInfo info = new DownloadInfo();
            info.setId(cursor.getString(cursor.getColumnIndex(COL_NAME_ID)));
            info.setUrl(cursor.getString(cursor.getColumnIndex(COL_NAME_URL)));
            info.setOriginalFileName(cursor.getString(cursor.getColumnIndex(COL_NAME_ORIG_NAME)));
            info.setActualFileName(cursor.getString(cursor.getColumnIndex(COL_NAME_ACTUAL_NAME)));
            info.setMimeType(cursor.getString(cursor.getColumnIndex(COL_NAME_MIME)));
            info.setSavePath(cursor.getString(cursor.getColumnIndex(COL_NAME_SAVE_PATH)));
            info.setTotalSize(cursor.getLong(cursor.getColumnIndex(COL_NAME_TOTAL_SIZE)));
            info.setFinishedSize(cursor.getLong(cursor.getColumnIndex(COL_NAME_FINISHED_SIZE)));
            info.setStatus(cursor.getInt(cursor.getColumnIndex(COL_NAME_STATUS)));
            info.setThumbnailUrl(cursor.getString(cursor.getColumnIndex(COL_NAME_THUMB_URL)));
            info.setStartTime(cursor.getLong(cursor.getColumnIndex(COL_NAME_START_TIME)));
            info.setFinishedTime(cursor.getLong(cursor.getColumnIndex(COL_NAME_FINISHED_TIME)));
            list.add(info);
        }
    }

    public boolean exists(String url) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * from " + TABLE_NAME + " where " + COL_NAME_URL + " = ?",
                new String[] { url });
        boolean isExists = cursor.moveToNext();
        cursor.close();
        return isExists;
    }

}
