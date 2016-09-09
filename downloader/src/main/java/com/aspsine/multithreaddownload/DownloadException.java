package com.aspsine.multithreaddownload;

import com.aspsine.multithreaddownload.util.L;

/**
 * Created by Aspsine on 2015/7/15.
 */
public class DownloadException extends Exception {
    /** recoverable exceptions
     */
    public final static int ERROR_NORMAL_IO = 0;
    public final static int ERROR_FAILED_GET_INPUT_STREAM = 1;

    /** unrecoverable exceptions
     */
    public final static int ERROR_UNRECOVERABLE_START = 100;
    public final static int ERROR_URL_MAL_FORMAT = ERROR_UNRECOVERABLE_START;
    public final static int ERROR_UNSUPPORTED_RESPONSE_CODE = ERROR_UNRECOVERABLE_START + 1;
    public final static int ERROR_PROTOCOL = ERROR_UNRECOVERABLE_START + 2;
    public final static int ERROR_FILE_IO = ERROR_UNRECOVERABLE_START + 3;
    public final static int ERROR_INVALID_CONTENT_LENGTH = ERROR_UNRECOVERABLE_START + 4;

    private int errorCode;
    private int status;

    public DownloadException(int status, int errorCode, String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
        this.status = status;
    }

    public DownloadException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DownloadException(int status, int errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.errorCode = errorCode;
        this.status = status;
    }

    public DownloadException(Throwable throwable) {
        super(throwable);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isRecoverable() {
        return this.errorCode < ERROR_UNRECOVERABLE_START;
    }
}
