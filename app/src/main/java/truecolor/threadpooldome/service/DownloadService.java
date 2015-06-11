package truecolor.threadpooldome.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import truecolor.threadpooldome.db.db.FinalDb;
import truecolor.threadpooldome.download.Constant;
import truecolor.threadpooldome.download.DownloadInfoDB;
import truecolor.threadpooldome.download.DownloadTask;
import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.model.Status;

/**
 * Created by xiaowu on 15/5/27.
 */
public class DownloadService extends Service{

    private static final int THREAD_SIZE = 2;
    public static ExecutorService THREAD_POOL = null;
    public static FinalDb DOWNLOAD_DB;
    public static Map<Integer, DownloadTask> TASKS = new LinkedHashMap<Integer, DownloadTask>();

    public static DownloadInfo DOWNLOAD_INFO;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            if (intent.getAction().equals(Constant.ACTION_START)){
                createDb();
                THREAD_POOL = Executors.newFixedThreadPool(THREAD_SIZE);
//                if (TASKS.get(DOWNLOAD_INFO.getFile_id()) == null){
//                    DownloadTask downloadTask = new DownloadTask(DOWNLOAD_INFO);
//                    TASKS.put(DOWNLOAD_INFO.getFile_id(), downloadTask);
//                    List<DownloadInfo> downloadInfos = DownloadService.DOWNLOAD_DB.findAllByWhere(DownloadInfo.class, "file_id = " + DOWNLOAD_INFO.getFile_id());
//                    if (downloadInfos == null || downloadInfos.size() == 0){
//                        DOWNLOAD_INFO.setStatus(Status.WATING.ordinal());
//                        DOWNLOAD_DB.save(DOWNLOAD_INFO);
//                    }
//                    THREAD_POOL.execute(downloadTask);
//                    Thread.State status = downloadTask.getState();
//                    Log.e("",status + "------");
//                }

            }
            if (intent.getAction().equals(Constant.ACTION_STOP)){
//                DownloadInfo downloadInfo = (DownloadInfo) intent.getSerializableExtra(Constant.DONWLOADINFO_INTENT);
                DownloadTask downloadTask = TASKS.get(DOWNLOAD_INFO.getFile_id());
                if (downloadTask != null){
                    downloadTask.isTaskPause = true;
                }
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        THREAD_POOL.shutdownNow();
        stopSelf();
    }

    private void createDb(){
        if (DOWNLOAD_DB == null){
            DOWNLOAD_DB = FinalDb.create(this, Constant.DB_NAME, true);
        }
    }

    public static void setDownloadIfo(DownloadInfo downloadIfo){
        DOWNLOAD_INFO = downloadIfo;
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;

        ActivityManager activityManager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList
                = activityManager.getRunningServices(Integer.MAX_VALUE);

        if (serviceList == null || serviceList.size() == 0) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(DownloadService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    public static void startService(Context context) {
        if(! isServiceRunning(context)){
            Intent intent=new Intent(context,DownloadService.class);
            intent.setAction(Constant.ACTION_START);
            context.startService(intent);
        }
    }
}
