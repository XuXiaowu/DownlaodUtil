package truecolor.threadpooldome.download;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.model.Status;
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
//        DownloadInfo resumeDown = mTasks.get(downloadInfo.getFile_id()).getDownloadInfo();
//        n
        DownloadTask downloadTask = mTasks.get(downloadInfo.getFile_id());
        downloadTask.resumDownloadTask();
//        DownloadService.THREAD_POOL.execute(downloadTask);
    }

    public static void stopTask(DownloadInfo downloadInfo){
        DownloadTask downloadTask = mTasks.get(downloadInfo.getFile_id());
        downloadTask.stopDownloadTask();
    }

    public static Map<Integer, DownloadTask> getTasks(){
        return mTasks;
    }

    public static List<DownloadInfo> getDownloadInfos(){
        List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAll(DownloadInfo.class);
        return downloadInfos;
    }
}
