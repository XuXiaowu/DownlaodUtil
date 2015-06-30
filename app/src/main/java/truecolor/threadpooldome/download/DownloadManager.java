package truecolor.threadpooldome.download;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.model.Status;
import truecolor.threadpooldome.model.SubDownloadInfo;
import truecolor.threadpooldome.service.DownloadService;

/**
 * Created by xiaowu on 15/6/7.
 */
public class DownloadManager {
    private static Map<Integer, DownloadTask> mTasks = new LinkedHashMap<Integer, DownloadTask>();
    public final static byte[] WRITE_LOCK = new byte[0];

    public static void addTask(DownloadInfo downloadInfo){
        DownloadTask downloadTask = new DownloadTask(downloadInfo);
        List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAllByWhere(DownloadInfo.class, "file_id = " + downloadInfo.getFile_id());
        downloadInfo.setStatus(Status.WATING.ordinal());
        mTasks.put(downloadInfo.getFile_id(), downloadTask);
        if (downloadInfos == null || downloadInfos.size() == 0){
            synchronized (WRITE_LOCK){
                DownloadService.DOWNLOAD_DB.save(downloadInfo);
            }
        }
        DownloadService.THREAD_POOL.execute(downloadTask);
    }

    public static void addTaskList(List<DownloadInfo> DownloadInfoList){
        for (int i = 0; i < DownloadInfoList.size(); i++) {
            addTask(DownloadInfoList.get(i));
        }
    }

    public static void resumeTask(DownloadInfo downloadInfo){
        DownloadTask downloadTask = mTasks.get(downloadInfo.getFile_id());
        if (downloadTask != null){
            downloadTask.resumDownloadTask();
        }else {
            addTask(downloadInfo);
        }
    }

    public static void stopTask(DownloadInfo downloadInfo){
        DownloadTask downloadTask = mTasks.get(downloadInfo.getFile_id());
        if(downloadTask == null){
            downloadInfo.setStatus(Status.PAUSE.ordinal());
            DownloadTask.updateDownloadInfo(downloadInfo);
        }else {
            downloadTask.stopDownloadTask();
        }
    }

    public static void stopAllTask(){
        List<DownloadInfo> downloadInfoList = getDownloadInfos();
        for (int i = 0; i < downloadInfoList.size(); i++) {
            DownloadTask downloadTask = mTasks.get(downloadInfoList.get(i).getFile_id());
            if (downloadTask != null){
                if (downloadTask.getDownloadInfo().getStatus() == Status.LOADING.ordinal()){
                    downloadTask.stopDownloadTask();
                    DownloadInfo downloadInfo = downloadTask.getDownloadInfo();
                    downloadInfo.setStatus(Status.PAUSE.ordinal());
                    downloadTask.updateDownloadInfo(downloadInfo);
                }
            }
        }
    }

    public static Map<Integer, DownloadTask> getTasks(){
        return mTasks;
    }

    public static List<DownloadInfo> getDownloadInfos(){
        List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAll(DownloadInfo.class);
        for (int i = 0; i < downloadInfos.size(); i++) {
            DownloadInfo di = downloadInfos.get(i);
            if (di.getDownloadType() == Constant.DOWNLOAD_TYPE_MULTI){
                List<SubDownloadInfo> subList = DownloadService.SUB_DOWNLOAD_DB.findAll(SubDownloadInfo.class);
                List<String> urlList = new ArrayList<String>();
                for (int j = 0; j < subList.size(); j++) {
                    urlList.add(subList.get(i).getUrl());
                }
                di.setUrlList(urlList);
            }
        }
        return downloadInfos;
    }

    public static void removeDownloadTask(int fileId){
        mTasks.remove(fileId);
    }
}
