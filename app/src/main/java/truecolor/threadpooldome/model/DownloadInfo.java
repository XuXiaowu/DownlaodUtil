package truecolor.threadpooldome.model;

import java.io.Serializable;

import truecolor.threadpooldome.db.sqlite.Id;
import truecolor.threadpooldome.db.sqlite.Table;
import truecolor.threadpooldome.db.sqlite.Transient;
import truecolor.threadpooldome.download.DownloadTaskListener;

/**
 * Created by xiaowu on 15/5/27.
 */

@Table(name = "download_info_table")
public class DownloadInfo implements Serializable{

    private int id;
    private String url;
    private String fileName;
    private long fileLength;
    private long progress;
    private int status;
    private int file_id;
    @Transient
    private DownloadTaskListener downloadTaskListener;

    public DownloadInfo(){}

    public DownloadInfo(String url, String fileName, int file_id, DownloadTaskListener downloadTaskListener){
        this.url = url;
        this.fileName = fileName;
        this.file_id = file_id;
        this.downloadTaskListener = downloadTaskListener;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getProgress() {
        return progress;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getFile_id() {
        return file_id;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public DownloadTaskListener getDownloadTaskListener() {
        return downloadTaskListener;
    }

    public void setDownloadTaskListener(DownloadTaskListener downloadTaskListener) {
        this.downloadTaskListener = downloadTaskListener;
    }

}
