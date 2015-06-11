package truecolor.threadpooldome.test;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import truecolor.threadpooldome.R;
import truecolor.threadpooldome.download.DownloadManager;
import truecolor.threadpooldome.download.DownloadTask;
import truecolor.threadpooldome.model.DownloadInfo;
import truecolor.threadpooldome.model.Status;

public class DownloadListActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private List<DownloadInfo> mDataList;
    private RvAdapter mRvAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_list);

        mRecyclerView = (RecyclerView) findViewById(R.id.m_recycler_view);
        mDataList = new ArrayList<DownloadInfo>();
        setData();
        mRvAdapter = new RvAdapter(mDataList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mRvAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mThread.start();

    }

    private void setData(){
        mDataList = DownloadManager.getDownloadInfos();
    }

    private Thread mThread = new Thread() {

        @Override
        public void run() {
            while (!isAllDownloadTaskFinish()) {
                try {
                    sleep(1000);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mRvAdapter.notifyDataSetChanged();
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e("InterruptedException", e.getMessage());
                }
            }
        }
    };

    private boolean isAllDownloadTaskFinish(){
        setData();
        mRvAdapter.updateDatas(mDataList);
        for (int i = 0; i < mDataList.size(); i++) {
            if (mDataList.get(i).getStatus() != Status.SUCCESS.ordinal()){
                return false;
            }
        }
        return true;
    }


}
