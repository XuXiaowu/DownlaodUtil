package truecolor.threadpooldome.download;

import android.content.Context;

import truecolor.threadpooldome.db.db.FinalDb;
import truecolor.threadpooldome.model.DownloadInfo;

/**
 * Created by xiaowu on 15/5/29.
 */
public class DownloadInfoDB {

    public static void addDownloadInfo(Context context, DownloadInfo downloadInfo){
        FinalDb finalDb = FinalDb.create(context, Constant.DB_NAME, true);
        finalDb.save(downloadInfo);
    }
}
