package truecolor.downloadutil.download;

import android.content.Context;

import truecolor.downloadutil.db.db.FinalDb;
import truecolor.downloadutil.model.DownloadInfo;

/**
 * Created by xiaowu on 15/5/29.
 */
public class DownloadInfoDB {

    public static void addDownloadInfo(Context context, DownloadInfo downloadInfo){
        FinalDb finalDb = FinalDb.create(context, Constant.DB_DOWNLOAD_INFO_NAME, true);
        finalDb.save(downloadInfo);
    }
}
