package truecolor.threadpooldome.download;

import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpConnection;
import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import truecolor.threadpooldome.db.db.sqlite.DbModel;
import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.model.Status;
import truecolor.threadpooldome.service.DownloadService;

/**
 * Created by xiaowu on 15/5/27.
 */
public class DownloadTask extends Thread{

    private final boolean IS_DUBUG = true;
    private final String BASE_PATH = "/sdcard/XW/download/";
    private DownloadInfo downloadInfo;
    private DownloadTaskListener downloadTaskListener;
    public boolean isTaskPause = false;

    private final int MSG_WHAT_WATING = 0;
    private final int MSG_WHAT_STARTED = 1;
    private final int MSG_WHAT_LOADING = 2;
    private final int MSG_WHAT_SUCCESS = 3;
    private final int MSG_WHAT_PAUSE = 4;
    private final int MSG_WHAT_FAILE = 5;

    public  DownloadTask(DownloadInfo downloadInfo ){
        this.downloadInfo = downloadInfo;
        this.downloadTaskListener = downloadInfo.getDownloadTaskListener();
}

        @Override
        public void run() {

            if (IS_DUBUG)Log.e("DownloadTask---","DownloadTask runing----");

            URL url = null;
            HttpURLConnection allFileConn = null;
            HttpURLConnection rangeFileConn = null;
            RandomAccessFile raf = null;
            InputStream is = null;
            int bytesum = 0;
            int byteread = 0;
            long progress = 0;

            List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAllByWhere(DownloadInfo.class, "file_id = " + downloadInfo.getFile_id());
            DownloadInfo di = null;
            if (downloadInfos != null && downloadInfos.size() > 0){
                di = downloadInfos.get(0);
            }

            if (di.getProgress() > 0 && di.getFileLength() > 0) {

                try {

                    url = new URL(di.getUrl());
                    rangeFileConn = (HttpURLConnection) url.openConnection();
                    rangeFileConn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
                    rangeFileConn.setRequestMethod("GET");
                    long start = di.getProgress();
                    long end = di.getFileLength();
                    rangeFileConn.setRequestProperty("Range", "bytes=" + start + "-" + end);

                    if (IS_DUBUG)Log.e("DownloadTask","get range file task started---" + start);
                    di.setStatus(Status.STARTED.ordinal());
                    updateDownloadInfo(di);
                    mhandler.obtainMessage(MSG_WHAT_STARTED).sendToTarget();

                    File targetFile = new File(BASE_PATH + downloadInfo.getFileName());
                    if (!targetFile.exists()) {
                        File dir = targetFile.getParentFile();
                        if (dir.exists() || dir.mkdirs()) {
                            targetFile.createNewFile();
                        }
                    }

                    File file = new File(BASE_PATH, downloadInfo.getFileName());
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(start);

                    if (rangeFileConn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
                        long time = System.currentTimeMillis();
                        is = rangeFileConn.getInputStream();
                        byte[] buffer = new byte[1024];
                        while ((byteread = is.read(buffer)) != -1) {
                            bytesum += byteread;
                            progress = start + bytesum;
                            if (IS_DUBUG)Log.e("DownloadTask","get range file task progress---" + progress);

                            raf.write(buffer, 0, byteread);

                            if ((System.currentTimeMillis() - time) > 1000){
                                time = System.currentTimeMillis();
                                di.setProgress(progress);
                                updateDownloadInfo(di);
                                mhandler.obtainMessage(MSG_WHAT_LOADING, di).sendToTarget();
                            }

                            if(isTaskPause){
                                if (IS_DUBUG)Log.e("DownloadTask","get range file task pause---" + progress);
                                di.setProgress(progress);
                                di.setStatus(Status.PAUSE.ordinal());
                                updateDownloadInfo(di);
                                mhandler.obtainMessage(MSG_WHAT_PAUSE).sendToTarget();
                                DownloadService.TASKS.remove(di.getFile_id());
                                break;
                            }
                        }

                    }
                }catch (Exception e){
                    e.printStackTrace();
                    di.setStatus(Status.FAILE.ordinal());
                    updateDownloadInfo(di);
                    mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
                    DownloadService.TASKS.remove(di.getFile_id());
                }finally {
                    try {
                        rangeFileConn.disconnect();
                        raf.close();
                        is.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (progress == di.getFileLength()){
                        if (IS_DUBUG)Log.e("DownloadTask","get range file task success---" + progress);
                        di.setProgress(progress);
                        di.setStatus(Status.SUCCESS.ordinal());
                        updateDownloadInfo(di);
                        mhandler.obtainMessage(MSG_WHAT_LOADING, di).sendToTarget();
                        mhandler.obtainMessage(MSG_WHAT_SUCCESS).sendToTarget();
                    }
                }


            }else {

                try {
                    if (IS_DUBUG)Log.e("DownloadTask","get all file task started---");
                    downloadInfo.setStatus(Status.STARTED.ordinal());
                    updateDownloadInfo(downloadInfo);
                    mhandler.obtainMessage(MSG_WHAT_STARTED).sendToTarget();

                    url = new URL(downloadInfo.getUrl());
                    allFileConn = (HttpURLConnection) url.openConnection();
                    allFileConn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
                    allFileConn.setRequestMethod("GET");

                    if (allFileConn.getResponseCode() == HttpStatus.SC_OK){
                        downloadInfo.setFileLength(allFileConn.getContentLength());
                        downloadInfo.setStatus(Status.LOADING.ordinal());
                        updateDownloadInfo(downloadInfo);

                        File targetFile = new File(BASE_PATH + downloadInfo.getFileName());
                        targetFile.delete();
                        if (!targetFile.exists()) {
                            File dir = targetFile.getParentFile();
                            if (dir.exists() || dir.mkdirs()) {
                                targetFile.createNewFile();
                            }
                        }

                        File file = new File(BASE_PATH, downloadInfo.getFileName());

                        long time = System.currentTimeMillis();
                        raf = new RandomAccessFile(file, "rwd");
                        is=allFileConn.getInputStream();
                        byte[] buffer=new byte[1024];
                        while ((byteread = is.read(buffer)) != -1) {
                            bytesum += byteread;
                            if (IS_DUBUG)Log.e("DownloadTask","get all file stak progress---" + bytesum);

                            if ((System.currentTimeMillis() - time) > 1000){
                                time = System.currentTimeMillis();
                                downloadInfo.setStatus(Status.LOADING.ordinal());
                                downloadInfo.setProgress(bytesum);
                                updateDownloadInfo(downloadInfo);
                                mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                            }

                            raf.write(buffer, 0, byteread);

//                            if(isTaskPause){
//                                if (IS_DUBUG)Log.e("DownloadTask","get all file task pause---" + bytesum);
//                                downloadInfo.setProgress(bytesum);
//                                downloadInfo.setStatus(Status.PAUSE.ordinal());
//                                updateDownloadInfo(downloadInfo);
//                                mhandler.obtainMessage(MSG_WHAT_PAUSE).sendToTarget();
//                                DownloadService.TASKS.remove(downloadInfo.getFile_id());
//                                break;
//                            }

                            int sleepCount = 0;
                            while (isTaskPause){
                                if (sleepCount == 0){
                                    downloadInfo.setProgress(bytesum);
                                    downloadInfo.setStatus(Status.PAUSE.ordinal());
                                    updateDownloadInfo(downloadInfo);
                                    mhandler.obtainMessage(MSG_WHAT_PAUSE).sendToTarget();
                                }
                                sleepCount++;
                                sleep(1000);
                            }
                        }
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    downloadInfo.setStatus(Status.FAILE.ordinal());
                    updateDownloadInfo(downloadInfo);
                    mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
                    DownloadService.TASKS.remove(downloadInfo.getFile_id());
                }finally{
                    try{
                        allFileConn.disconnect();
                        raf.close();
                        is.close();
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }

                    if (bytesum == downloadInfo.getFileLength() && bytesum > 0){
                        downloadInfo.setProgress(bytesum);
                        downloadInfo.setStatus(Status.SUCCESS.ordinal());
                        updateDownloadInfo(downloadInfo);
                        mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                        mhandler.obtainMessage(MSG_WHAT_SUCCESS).sendToTarget();
                        if (IS_DUBUG)Log.e("DownloadTask","get all file task success---" + bytesum);
                    }
                }
            }


        }


    private void updateDownloadInfo(DownloadInfo di){
        synchronized (DownloadManager.WRITE_LOCK){
            DownloadService.DOWNLOAD_DB.update(di, "file_id=" + di.getFile_id());
        }
    }

    private void checekTaskCancle(){
        if(isTaskPause){
            downloadInfo.setStatus(Status.PAUSE.ordinal());
            updateDownloadInfo(downloadInfo);

        }
    }

    Handler mhandler=new Handler()
    {
        @Override
        public void handleMessage(android.os.Message msg)
        {
            switch(msg.what)
            {
                case MSG_WHAT_WATING:
                    break;
                case MSG_WHAT_STARTED:
                    downloadTaskListener.onStarted();
                    break;
                case MSG_WHAT_LOADING:
                    DownloadInfo downloadInfo = (DownloadInfo) msg.obj;
                    downloadTaskListener.onLoading(downloadInfo.getFileLength(), downloadInfo.getProgress());
                    break;
                case MSG_WHAT_SUCCESS:
                    downloadTaskListener.onSuccess();
                    break;
                case MSG_WHAT_PAUSE:
                    downloadTaskListener.onPause();
                    break;
                case MSG_WHAT_FAILE:
                    Exception exception = (Exception) msg.obj;
                    downloadTaskListener.onFailed(exception, exception.getMessage());
                    break;
                default:
                    break;
            }

        };
    };

    public void stopDownloadTask(){
        isTaskPause = true;
    }

    public void resumDownloadTask(){
        isTaskPause = false;
    }

    public DownloadInfo getDownloadInfo(){
        return  downloadInfo;
    }
}
