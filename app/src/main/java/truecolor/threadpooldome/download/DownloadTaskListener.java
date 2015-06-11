package truecolor.threadpooldome.download;

/**
 * Created by xiaowu on 15/5/21.
 */
public interface DownloadTaskListener {

    void onStarted();

    void onLoading(long total,long current);

    void onPause();

    void onSuccess();

    void onFailed(Exception e,String msg);
}
