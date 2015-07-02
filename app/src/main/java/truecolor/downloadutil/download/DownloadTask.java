package truecolor.downloadutil.download;

import android.os.Handler;
import android.util.Log;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import truecolor.downloadutil.model.Status;
import truecolor.downloadutil.model.DownloadInfo;
import truecolor.downloadutil.model.SubDownloadInfo;
import truecolor.downloadutil.service.DownloadService;

/**
 * Created by xiaowu on 15/5/27.
 */
public class DownloadTask extends Thread{

    private final boolean IS_DUBUG = true;
    private final String BASE_PATH = "/sdcard/XW/download/";
    private DownloadInfo downloadInfo;
    private DownloadTaskListener downloadTaskListener;
    public boolean isTaskPause = false;
    public boolean isComputeNowProgress = false;
    private long mStopProgress;
    private long mRangeProgress;

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

            if (downloadInfo.getDownloadType() == Constant.DOWNLOAD_TYPE_SINGLE){
                List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAllByWhere(DownloadInfo.class, "file_id = " + downloadInfo.getFile_id());
                DownloadInfo di = null;
                if (downloadInfos != null && downloadInfos.size() > 0){
                    di = downloadInfos.get(0);
                }

                if (di.getProgress() > 0 && di.getFileLength() > 0) {
                    downloadRangeFile(di);
                }else {
                    downloadAllFile();
                }
            }else {
                downloadFileList();
            }
        }


    public static void updateDownloadInfo(DownloadInfo di){
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
            if (downloadTaskListener == null)
                return;

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
                    if (exception != null){
                        downloadTaskListener.onFailed(exception, exception.getMessage());
                    }else {
                        downloadTaskListener.onFailed(exception, "null exception");
                    }
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
        isComputeNowProgress = true;
    }

    public DownloadInfo getDownloadInfo(){
        return  downloadInfo;
    }


    /**
     * 检查任务是否暂停
     * @param isUpdateStatus 是否保存当前任务的信息
     * @param progress 任务进度
     */
    private void checkTaskPaues(boolean isUpdateStatus, long progress){
        while (isTaskPause){
            if (isUpdateStatus){
                downloadInfo.setProgress(progress);
                downloadInfo.setStatus(Status.PAUSE.ordinal());
                updateDownloadInfo(downloadInfo);
                mhandler.obtainMessage(MSG_WHAT_PAUSE).sendToTarget();
                mStopProgress = progress;
                mRangeProgress = 0;
            }
            isUpdateStatus = false;
            try {
                this.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取下载任务文件队列的所有文件大小
     */
    private long getFileListLenght(){
        long fileListLenght = 0;
        HttpURLConnection conn;
        URL url;

        downloadInfo.setStatus(Status.STARTED.ordinal());
        updateDownloadInfo(downloadInfo);
        mhandler.obtainMessage(MSG_WHAT_STARTED).sendToTarget();
        for (int i = 0; i < downloadInfo.getUrlList().size(); i++) {
            try {
                url = new URL(downloadInfo.getUrlList().get(i));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpStatus.SC_OK){
                    long subFileLength = conn.getContentLength();
                    fileListLenght += subFileLength;
                    SubDownloadInfo subDownloadInfo = new SubDownloadInfo(downloadInfo.getFile_id(), i,
                            subFileLength, downloadInfo.getUrlList().get(i));
                    DownloadService.SUB_DOWNLOAD_DB.save(subDownloadInfo);
                }

            }catch (Exception e){
                if (e != null) e.printStackTrace();
                downloadInfo.setStatus(Status.FAILE.ordinal());
                updateDownloadInfo(downloadInfo);
                mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
                DownloadManager.removeDownloadTask(downloadInfo.getFile_id());
                return 0;
            }

        }
        return fileListLenght;
    }

    private void downloadFileList(){
        List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAllByWhere(DownloadInfo.class, "file_id = " + downloadInfo.getFile_id());
        DownloadInfo di = null;
        if (downloadInfos != null && downloadInfos.size() > 0){
            di = downloadInfos.get(0);

            if (di.getProgress() > 0 && di.getFileLength() > 0) {
                downloadRangeFileList(di);
            }else {
                downloadAllFileList();
            }
        }
    }

    /**
     * 下载单个文件的指定部分
     */
    private void  downloadRangeFile(DownloadInfo di){
        HttpURLConnection conn = null;
        URL url;
        RandomAccessFile raf = null;
        InputStream is = null;
        int bytesum = 0;
        int byteread = 0;
        long progress = 0;

        try {
            url = new URL(di.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
            conn.setRequestMethod("GET");
            long start = di.getProgress();
            long end = di.getFileLength();
            conn.setRequestProperty("Range", "bytes=" + start + "-" + end);

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

            if (conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT) {
                long time = System.currentTimeMillis();
                is = conn.getInputStream();
                byte[] buffer = new byte[1024];
                while ((byteread = is.read(buffer)) != -1) {
                    bytesum += byteread;
                    progress = start + bytesum;
                    if (IS_DUBUG)Log.e("DownloadTask","get range file task progress---" + progress);

                    raf.write(buffer, 0, byteread);

                    if ((System.currentTimeMillis() - time) > 1000){
                        time = System.currentTimeMillis();
                        di.setProgress(progress);
                        di.setStatus(Status.LOADING.ordinal());
                        updateDownloadInfo(di);
                        mhandler.obtainMessage(MSG_WHAT_LOADING, di).sendToTarget();
                    }
                    boolean isUpdateStatus = true;
                    checkTaskPaues(isUpdateStatus, progress);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            di.setStatus(Status.FAILE.ordinal());
            updateDownloadInfo(di);
            mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
            DownloadManager.removeDownloadTask(di.getFile_id());
        }finally {
            try {
                conn.disconnect();
                if (raf != null) raf.close();
                if (is != null) is.close();
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
    }

    /**
     * 下载单个文件的所有部分
     */
    private void downloadAllFile(){
        HttpURLConnection conn = null;
        URL url;
        RandomAccessFile raf = null;
        InputStream is = null;
        int bytesum = 0;
        int byteread = 0;
        long progress = 0;

        try {
            if (IS_DUBUG)Log.e("DownloadTask","get all file task started---");
            downloadInfo.setStatus(Status.STARTED.ordinal());
            updateDownloadInfo(downloadInfo);
            mhandler.obtainMessage(MSG_WHAT_STARTED).sendToTarget();

            url = new URL(downloadInfo.getUrl());
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == HttpStatus.SC_OK){
                downloadInfo.setFileLength(conn.getContentLength());
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
                is=conn.getInputStream();
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
                    boolean isUpdateStatus = true;
                    checkTaskPaues(isUpdateStatus, bytesum);

                }
            }

        }catch (Exception e){
            e.printStackTrace();
            downloadInfo.setStatus(Status.FAILE.ordinal());
            updateDownloadInfo(downloadInfo);
            mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
            DownloadManager.removeDownloadTask(downloadInfo.getFile_id());
        }finally{
            try{
                conn.disconnect();
                if (raf != null) raf.close();
                if (is != null) is.close();
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

    /**
     * 下载当前下载任务队列里的全部文件
     */
    private void downloadAllFileList(){
        long allFileLength = getFileListLenght();
        if (allFileLength == 0){
            synchronized (DownloadManager.WRITE_LOCK){
                DownloadService.DOWNLOAD_DB.deleteByWhere(DownloadInfo.class,"file_id = " + downloadInfo.getFile_id());
            }
            return;
        }
        downloadInfo.setStatus(Status.LOADING.ordinal());
        downloadInfo.setFileLength(allFileLength);
        updateDownloadInfo(downloadInfo);

        for (int i = 0; i < downloadInfo.getUrlList().size(); i++) {
            HttpURLConnection conn = null;
            URL url;
            RandomAccessFile raf = null;
            InputStream is = null;
            int bytesum = 0;
            int byteread = 0;
            long progress = 0;

            downloadInfo.setSub_download_index(i);
            updateDownloadInfo(downloadInfo);
            try {
                url = new URL(downloadInfo.getUrlList().get(i));
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == HttpStatus.SC_OK){
                    File targetFile = new File(BASE_PATH + downloadInfo.getFileName() + i + ".apk");
                    targetFile.delete();
                    if (!targetFile.exists()) {
                        File dir = targetFile.getParentFile();
                        if (dir.exists() || dir.mkdirs()) {
                            targetFile.createNewFile();
                        }
                    }

                    File file = new File(BASE_PATH, downloadInfo.getFileName() + i + ".apk");

                    long time = System.currentTimeMillis();
                    raf = new RandomAccessFile(file, "rwd");
                    is=conn.getInputStream();
                    byte[] buffer=new byte[1024];
                    while ((byteread = is.read(buffer)) != -1) {
                        bytesum += byteread;
                        progress = bytesum + getFileListLengthOfIndex(i);

                        if (IS_DUBUG)Log.e("DownloadTask","get all file list stak progress---" + allFileLength + "/" + progress);

                        if ((System.currentTimeMillis() - time) > 1000){
                            time = System.currentTimeMillis();
                            downloadInfo.setStatus(Status.LOADING.ordinal());
                            downloadInfo.setProgress(progress);
                            updateDownloadInfo(downloadInfo);
                            mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                        }

                        raf.write(buffer, 0, byteread);

                        boolean isUpdateStatus = true;
                        checkTaskPaues(isUpdateStatus, progress);

                    }
                }

            }catch (Exception e){
                e.printStackTrace();
                downloadInfo.setStatus(Status.FAILE.ordinal());
                updateDownloadInfo(downloadInfo);
                mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
                DownloadManager.removeDownloadTask(downloadInfo.getFile_id());
                return;
            }finally{
                try{
                    conn.disconnect();
                    if (raf != null) raf.close();
                    if (is != null) is.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                if (progress == downloadInfo.getFileLength() && progress > 0){
                    downloadInfo.setProgress(progress);
                    downloadInfo.setStatus(Status.SUCCESS.ordinal());
                    updateDownloadInfo(downloadInfo);
                    mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                    mhandler.obtainMessage(MSG_WHAT_SUCCESS).sendToTarget();
                    if (IS_DUBUG)Log.e("DownloadTask","get all file task success---" + bytesum);
                }
            }
        }
    }


    /**
     * 下载文件队列中指定范围的文件
     */
    private void downloadRangeFileList(DownloadInfo downloadInfo){
        downloadInfo.setStatus(Status.STARTED.ordinal());
        updateDownloadInfo(downloadInfo);
        mhandler.obtainMessage(MSG_WHAT_STARTED).sendToTarget();

        boolean getOldProgress = true;
        List<SubDownloadInfo> subDownloadInfoList = getSubDownloadInfoList();
        for (int i = downloadInfo.getSub_download_index(); i < subDownloadInfoList.size(); i++) {
            HttpURLConnection conn = null;
            URL url;
            RandomAccessFile raf = null;
            InputStream is = null;
            int bytesum = 0;
            int byteread = 0;
            long progress = 0;

            downloadInfo.setSub_download_index(i);
            updateDownloadInfo(downloadInfo);
            try {
                url = new URL(subDownloadInfoList.get(i).getUrl());
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(Constant.CONNECT_TIME_OUT);
                conn.setRequestMethod("GET");
                long start = getSubFileProgress(downloadInfo, downloadInfo.getSub_download_index());
                if (start < 0){
                    start = 0;
                }
                long end = subDownloadInfoList.get(downloadInfo.getSub_download_index()).getFile_length();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                if (conn.getResponseCode() == HttpStatus.SC_OK || conn.getResponseCode() == HttpStatus.SC_PARTIAL_CONTENT){
                    File targetFile = new File(BASE_PATH + downloadInfo.getFileName() + i + ".apk");
                    if (!targetFile.exists()) {
                        File dir = targetFile.getParentFile();
                        if (dir.exists() || dir.mkdirs()) {
                            targetFile.createNewFile();
                        }
                    }

                    File file = new File(BASE_PATH, downloadInfo.getFileName() + i + ".apk");

                    long time = System.currentTimeMillis();
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(start);

                    is=conn.getInputStream();
                    byte[] buffer=new byte[1024];
                    while ((byteread = is.read(buffer)) != -1) {
                        bytesum += byteread;
                        mRangeProgress += byteread;
                        if (isComputeNowProgress){
                            progress = mRangeProgress + mStopProgress;
                        }else {
                            progress = bytesum + getFileListRangeLengthOfIndex(i, getOldProgress);
                        }

                        if (IS_DUBUG)Log.e("DownloadTask","get range file list stak progress---" + downloadInfo.getFileLength() + "/" + progress);

                        if ((System.currentTimeMillis() - time) > 1000){
                            time = System.currentTimeMillis();
                            downloadInfo.setStatus(Status.LOADING.ordinal());
                            downloadInfo.setProgress(progress);
                            updateDownloadInfo(downloadInfo);
                            mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                        }

                        raf.write(buffer, 0, byteread);

                        boolean isUpdateStatus = true;
                        checkTaskPaues(isUpdateStatus, progress);

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                downloadInfo.setStatus(Status.FAILE.ordinal());
                updateDownloadInfo(downloadInfo);
                mhandler.obtainMessage(MSG_WHAT_FAILE, e).sendToTarget();
                DownloadManager.removeDownloadTask(downloadInfo.getFile_id());
                return;
            }finally{
                try{
                    conn.disconnect();
                    if (raf != null) raf.close();
                    if (is != null) is.close();
                }
                catch(Exception e){
                    e.printStackTrace();
                }

                if (progress == downloadInfo.getFileLength() && progress > 0){
                    downloadInfo.setProgress(progress);
                    downloadInfo.setStatus(Status.SUCCESS.ordinal());
                    updateDownloadInfo(downloadInfo);
                    mhandler.obtainMessage(MSG_WHAT_LOADING, downloadInfo).sendToTarget();
                    mhandler.obtainMessage(MSG_WHAT_SUCCESS).sendToTarget();
                    if (IS_DUBUG)Log.e("DownloadTask","get range file lsit task success---" + progress);
                }
            }
            getOldProgress = false;
            isComputeNowProgress = false;
        }
    }

    /**
     * 获取下载任务的队列当前下载文件的进度
     * @param downloadInfo 下载任务信息
     * @param subFileIndex 当前下载文件索引值
     */
    private long getSubFileProgress(DownloadInfo downloadInfo, int subFileIndex){
        long subFileProgress = downloadInfo.getProgress() - getFileListLengthOfIndex(subFileIndex);
        return subFileProgress;
    }

    /**
     * 获取下载队列里到指定个数文件的大小
     * @param index 指定文件个数
     */
    private long getFileListLengthOfIndex(int index){
        List<SubDownloadInfo> subDownloadInfoList = getSubDownloadInfoList();
        long fielLength = 0;
        for (int i = 0; i <index; i++) {
            fielLength += subDownloadInfoList.get(i).getFile_length();
        }
        return fielLength;
    }

    /**
     * 获取下载队列里到指定个数文件已经下载的大小
     * @param index 指定文件个数
     */
    private long getFileListRangeLengthOfIndex(int index, boolean getOldProgress){
        List<SubDownloadInfo> subDownloadInfoList = getSubDownloadInfoList();
        long fielLength = 0;
        if (getOldProgress){
            return downloadInfo.getProgress();
        }else {
            for (int i = 0; i <index; i++) {
                fielLength += subDownloadInfoList.get(i).getFile_length();
            }
            return fielLength;
        }
    }

    /**
     * 查询当前下载任务的下载队列
     */
    private List<SubDownloadInfo> getSubDownloadInfoList(){
        List<SubDownloadInfo> subDownloadInfoList = DownloadService.SUB_DOWNLOAD_DB.
                findAllByWhere(SubDownloadInfo.class, "file_id = " + downloadInfo.getFile_id());
        return subDownloadInfoList;
    }
}
