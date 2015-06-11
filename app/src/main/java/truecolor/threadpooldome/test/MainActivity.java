package truecolor.threadpooldome.test;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import truecolor.threadpooldome.R;
import truecolor.threadpooldome.download.Constant;
import truecolor.threadpooldome.download.DownloadManager;
import truecolor.threadpooldome.download.DownloadTask;
import truecolor.threadpooldome.download.DownloadTaskListener;
import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.service.DownloadService;


public class MainActivity extends ActionBarActivity {

    private TextView mTextView;
    private TextView mTextView2;
    private TextView mTextView3;

    private Button mTestRunBtn;
    private Button mTestRunBtn2;
    private Button mTestRunBtn3;

    private Button mTestPauseBtn;
    private Button mTestPauseBtn2;
    private Button mTestPauseBtn3;

    private ProgressBar mPbView;
    private ProgressBar mPbView2;
    private ProgressBar mPbView3;

    private Button mShutDownNowBtn;
    private Button mShutdownBtn;
    private Button mAddDownloadTaskBtn;
    private Button mAddDownloadTaskListBtn;

    private DownloadInfo downloadInfo1;
    private DownloadInfo downloadInfo2;
    private DownloadInfo downloadInfo3;


    private ExecutorService mThreadPool;
    private String url = "http://www.muzhiwan.com/index.php?action=common&opt=downloadstat&vid=112312";
    private final String SAVE_PATH = "/sdcard/DownloadDome/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.m_tv);
        mTextView2 = (TextView) findViewById(R.id.m_tv2);
        mTextView3 = (TextView) findViewById(R.id.m_tv3);

        mTestRunBtn = (Button) findViewById(R.id.btn_test_run_thread_pool);
        mTestRunBtn2 = (Button) findViewById(R.id.btn_test_run_thread_pool2);
        mTestRunBtn3 = (Button) findViewById(R.id.btn_test_run_thread_pool3);

        mTestPauseBtn = (Button) findViewById(R.id.btn_test_pause_thread_pool);
        mTestPauseBtn2 = (Button) findViewById(R.id.btn_test_pause_thread_pool2);
        mTestPauseBtn3 = (Button) findViewById(R.id.btn_test_pause_thread_pool3);

        mPbView = (ProgressBar) findViewById(R.id.m_pb1);
        mPbView2 = (ProgressBar) findViewById(R.id.m_pb2);
        mPbView3 = (ProgressBar) findViewById(R.id.m_pb3);

        mShutdownBtn = (Button) findViewById(R.id.btn_shutdown_thread_pool);
        mShutDownNowBtn = (Button) findViewById(R.id.btn_shutdown_now_thread_pool);
        mAddDownloadTaskBtn = (Button) findViewById(R.id.btn_add_download_task);
        mAddDownloadTaskListBtn = (Button) findViewById(R.id.btn_add_download_task_list);

        mTestRunBtn.setOnClickListener(mTestRunBtnOnClickListener);
        mTestRunBtn2.setOnClickListener(mTestRunBtnOnClickListener2);
        mTestRunBtn3.setOnClickListener(mTestRunBtnOnClickListener3);

        mTestPauseBtn.setOnClickListener(mTestPauseBtnOnClickListener);
        mTestPauseBtn2.setOnClickListener(mTestPauseBtnOnClickListener2);
        mTestPauseBtn3.setOnClickListener(mTestPauseBtnOnClickListener3);

        mShutdownBtn.setOnClickListener(mShutdownBtnOnClickListener);
        mShutDownNowBtn.setOnClickListener(mShutdownNowBtnOnClickListener);
        mAddDownloadTaskBtn.setOnClickListener(mAddDownloadTaskBtnOnClickListener);
        mAddDownloadTaskListBtn.setOnClickListener(mAddDownloadTaskListBtnOnClickListener);

        mThreadPool = Executors.newFixedThreadPool(1);

        downloadInfo1 = new DownloadInfo(url, "mzw1.apk", 10001, mLoadDataListener);
        downloadInfo2 = new DownloadInfo(url, "mzw2.apk", 10002, mLoadDataListener2);
        downloadInfo3 = new DownloadInfo(url, "mzw3.apk", 10003, mLoadDataListener3);

        DownloadService.startService(MainActivity.this);
    }

    private DownloadTaskListener mLoadDataListener = new DownloadTaskListener() {

        @Override
        public void onLoading(final long total, final long current) {
            mTextView.setText("Loading:" + total + ":" +current);
            mTextView.setTextColor(getResources().getColor(R.color.holo_blue_light));
            mPbView.setProgress((int) (current * 100 / total));
        }

        @Override
        public void onPause() {
            mTextView.setText("Pause-----");
            mTextView.setTextColor(getResources().getColor(R.color.holo_purple));
        }

        @Override
        public void onStarted() {
            mTextView.setText("Started-----");
            mTextView.setTextColor(getResources().getColor(R.color.holo_orange_light));
        }

        @Override
        public void onFailed(Exception e, final String msg) {
            mTextView.setText("Failed-----" + msg);
            mTextView.setTextColor(getResources().getColor(R.color.holo_red_light));
        }

        @Override
        public void onSuccess() {
            mTextView.setText("SUCCESS-----");
            mTextView.setTextColor(getResources().getColor(R.color.holo_green_light));

        }
    };

    private DownloadTaskListener mLoadDataListener2 = new DownloadTaskListener() {

        @Override
        public void onLoading(final long total, final long current) {
            mTextView2.setText("Loading:" + total + ":" +current);
            mTextView2.setTextColor(getResources().getColor(R.color.holo_blue_light));
            mPbView2.setProgress((int) (current * 100 / total));
        }

        @Override
        public void onPause() {
            mTextView2.setText("Pause-----");
            mTextView2.setTextColor(getResources().getColor(R.color.holo_purple));
        }

        @Override
        public void onStarted() {
            mTextView2.setText("Started-----");
            mTextView2.setTextColor(getResources().getColor(R.color.holo_orange_light));
        }

        @Override
        public void onFailed(Exception e, final String msg) {
            mTextView2.setText("Failed-----" + msg);
            mTextView2.setTextColor(getResources().getColor(R.color.holo_red_light));
        }

        @Override
        public void onSuccess() {
            mTextView2.setText("SUCCESS-----");
            mTextView2.setTextColor(getResources().getColor(R.color.holo_green_light));

        }
    };

    private DownloadTaskListener mLoadDataListener3 = new DownloadTaskListener() {

        @Override
        public void onLoading(final long total, final long current) {
            mTextView3.setText("Loading:" + total + ":" +current);
            mTextView3.setTextColor(getResources().getColor(R.color.holo_blue_light));
            mPbView3.setProgress((int) (current * 100 / total));
        }

        @Override
        public void onPause() {
            mTextView3.setText("Pause-----");
            mTextView3.setTextColor(getResources().getColor(R.color.holo_purple));
        }

        @Override
        public void onStarted() {
            mTextView3.setText("Started-----");
            mTextView3.setTextColor(getResources().getColor(R.color.holo_orange_light));
        }

        @Override
        public void onFailed(Exception e, final String msg) {
            mTextView3.setText("Failed-----" + msg);
            mTextView3.setTextColor(getResources().getColor(R.color.holo_red_light));
        }

        @Override
        public void onSuccess() {
            mTextView3.setText("SUCCESS-----");
            mTextView3.setTextColor(getResources().getColor(R.color.holo_green_light));

        }
    };


    private void testThreadPool(){
        //创建一个可重用固定线程数的线程池
        ExecutorService pool = Executors.newFixedThreadPool(1);
        //创建实现了Runnable接口对象，Thread对象当然也实现了Runnable接口
        Thread t1 = new MyThread();
        Thread t2 = new MyThread2();
        Thread t3 = new MyThread3();
//        Thread t4 = new MyThread();
//        Thread t5 = new MyThread();
        //将线程放入池中进行执行
        mThreadPool.execute(t1);
        mThreadPool.execute(t2);
        mThreadPool.execute(t3);
//        mThreadPool.execute(t4);
//        mThreadPool.execute(t5);
        //关闭线程池
//        mThreadPool.shutdown();
    }

    class MyThread extends Thread{
        @Override
        public void run() {
            httpDownload(url, SAVE_PATH + "mzw.apk", mLoadDataListener);
        }
    }

    class MyThread2 extends Thread{
        @Override
        public void run() {
            httpDownload(url, SAVE_PATH + "mzw2.apk", mLoadDataListener2);
        }
    }

    class MyThread3 extends Thread{
        @Override
        public void run() {
            httpDownload(url, SAVE_PATH + "mzw3.apk", mLoadDataListener3);
        }
    }

    private View.OnClickListener mTestRunBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_START);
//            DownloadService.setDownloadIfo(downloadInfo1);
//            MainActivity.this.startService(intent);

            DownloadManager.addTask(downloadInfo1);

        }
    };

    private View.OnClickListener mTestPauseBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_STOP);
//            DownloadService.setDownloadIfo(downloadInfo1);
//            MainActivity.this.startService(intent);
            DownloadManager.stopTask(downloadInfo1);
        }
    };

    private View.OnClickListener mTestRunBtnOnClickListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_START);
//            DownloadService.setDownloadIfo(downloadInfo2);
//            MainActivity.this.startService(intent);
            DownloadManager.addTask(downloadInfo2);
        }
    };

    private View.OnClickListener mTestPauseBtnOnClickListener2 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_STOP);
//            DownloadService.setDownloadIfo(downloadInfo2);
//            MainActivity.this.startService(intent);
            DownloadManager.stopTask(downloadInfo2);
        }
    };

    private View.OnClickListener mTestRunBtnOnClickListener3 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_START);
//            DownloadService.setDownloadIfo(downloadInfo3);
//            MainActivity.this.startService(intent);
            DownloadManager.addTask(downloadInfo3);
        }
    };

    private View.OnClickListener mTestPauseBtnOnClickListener3 = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            Intent intent=new Intent(MainActivity.this,DownloadService.class);
//            intent.setAction(Constant.ACTION_STOP);
//            DownloadService.setDownloadIfo(downloadInfo3);
//            MainActivity.this.startService(intent);
            DownloadManager.stopTask(downloadInfo3);
        }
    };

    private View.OnClickListener mShutdownBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
//            mThreadPool.shutdown();
            Intent intent = new Intent(MainActivity.this, DownloadListActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener mShutdownNowBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DownloadService.THREAD_POOL.shutdownNow();
        }
    };

    private View.OnClickListener mAddDownloadTaskBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            List<DownloadInfo> downloadInfoList = DownloadManager.getDownloadInfos();
            String fileName = "mzwFile" + (downloadInfoList.size() + 1) + ".apk";
            int fileId = 10000 + downloadInfoList.size() + 1;
            DownloadTaskListener downloadTaskListener = new DownloadTaskListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onLoading(long total, long current) {

                }

                @Override
                public void onPause() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailed(Exception e, String msg) {

                }
            };
            DownloadInfo downloadInfo = new DownloadInfo(url, fileName, fileId, downloadTaskListener);
            DownloadManager.addTask(downloadInfo);
        }
    };

    private View.OnClickListener mAddDownloadTaskListBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DownloadTaskListener downloadTaskListener = new DownloadTaskListener() {
                @Override
                public void onStarted() {

                }

                @Override
                public void onLoading(long total, long current) {

                }

                @Override
                public void onPause() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailed(Exception e, String msg) {

                }
            };
            int downloadTaskQueuSize = DownloadManager.getDownloadInfos().size();
            List<DownloadInfo> downloadInfoList = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                String fileName = "mzwFile" + (downloadTaskQueuSize + i + 1) + ".apk";
                int fileId = 10000 + (downloadTaskQueuSize + i + 1);
                DownloadInfo downloadInfo = new DownloadInfo(url, fileName, fileId, downloadTaskListener);
                downloadInfoList.add(downloadInfo);
            }
            DownloadManager.addTaskList(downloadInfoList);
        }
    };

    public static boolean httpDownload(String httpUrl, String saveFile, DownloadTaskListener loadDataListener){
        // 下载网络文件
        int bytesum = 0;
        int byteread = 0;

        URL url = null;

        try {
            url = new URL(httpUrl);
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return false;
        }

        try {
            File targetFile = new File(saveFile);

            if (!targetFile.exists()) {
                File dir = targetFile.getParentFile();
                if (dir.exists() || dir.mkdirs()) {
                    targetFile.createNewFile();
                }
            }

            URLConnection conn = url.openConnection();
            int total = conn.getContentLength();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(saveFile);
            loadDataListener.onStarted();

            byte[] buffer = new byte[1204];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                System.out.println(bytesum);
                loadDataListener.onLoading(total, bytesum);
                fs.write(buffer, 0, byteread);
            }
            loadDataListener.onSuccess();
            return true;
        } catch (FileNotFoundException e) {

            e.printStackTrace();
            loadDataListener.onFailed(e, e.getMessage());
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            loadDataListener.onFailed(e, e.getMessage());
            return false;
        }
    }

}
