package truecolor.downloadutil.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import truecolor.downloadutil.download.Constant;
import truecolor.downloadutil.download.DownloadManager;
import truecolor.downloadutil.download.DownloadTask;
import truecolor.downloadutil.model.DownloadInfo;
import truecolor.downloadutil.db.db.FinalDb;

/**
 * Created by xiaowu on 15/5/27.
 */
public class DownloadService extends Service{

    private static final int THREAD_SIZE = 2;
    public static ExecutorService THREAD_POOL = null;
    public static FinalDb DOWNLOAD_DB = null;
    public static FinalDb SUB_DOWNLOAD_DB = null;
    public static Map<Integer, DownloadTask> TASKS = new LinkedHashMap<Integer, DownloadTask>();

    public static DownloadInfo DOWNLOAD_INFO;



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null){
            if (intent.getAction().equals(Constant.ACTION_START)){
                createDb();
                //创建一个可重用固定线程数的线程池
                THREAD_POOL = Executors.newFixedThreadPool(THREAD_SIZE);
            }
            if (intent.getAction().equals(Constant.ACTION_STOP)){
                stopSelf();
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
        DownloadManager.stopAllTask();
    }

    private void createDb(){
        if (DOWNLOAD_DB == null){
            DOWNLOAD_DB = FinalDb.create(this, Constant.DB_DOWNLOAD_INFO_NAME, true);
        }
        if (SUB_DOWNLOAD_DB == null){
            SUB_DOWNLOAD_DB = FinalDb.create(this, Constant.DB_SUB_DOWNLOAD_INFO_NAME, true);
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
