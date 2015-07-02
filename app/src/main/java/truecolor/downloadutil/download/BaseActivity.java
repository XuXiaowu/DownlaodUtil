package truecolor.downloadutil.download;

import android.app.ActivityManager;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import java.util.List;

import truecolor.downloadutil.R;
import truecolor.downloadutil.service.DownloadService;

public class BaseActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        ActivityManager manager = (ActivityManager)this.getSystemService(ACTIVITY_SERVICE) ;
        List<ActivityManager.RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(Integer.MAX_VALUE) ;
        Intent intent=new Intent(this,DownloadService.class);
        intent.setAction(Constant.ACTION_STOP);
        this.startService(intent);
    }


}
