package truecolor.threadpooldome.model;

import truecolor.threadpooldome.db.sqlite.Table;

/**
 * Created by xiaowu on 15/6/15.
 */

@Table(name = "sub_download_info_table")
public class SubDownloadInfo {

    private int id;
    private int file_id;
    private int sub_index;
    private long file_length;
    private String url;

    public SubDownloadInfo(){}

    public SubDownloadInfo(int file_id, int sub_index, long file_length, String url){
        this.file_id = file_id;
        this.sub_index =sub_index;
        this.file_length = file_length;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFile_id() {
        return file_id;
    }

    public void setFile_id(int file_id) {
        this.file_id = file_id;
    }

    public int getSub_index() {
        return sub_index;
    }

    public void setSub_index(int sub_index) {
        this.sub_index = sub_index;
    }

    public long getFile_length() {
        return file_length;
    }

    public void setFile_length(long file_length) {
        this.file_length = file_length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}
