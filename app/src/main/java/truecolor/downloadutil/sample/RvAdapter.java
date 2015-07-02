package truecolor.downloadutil.sample;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import truecolor.downloadutil.R;
import truecolor.downloadutil.download.DownloadManager;
import truecolor.downloadutil.model.DownloadInfo;
import truecolor.downloadutil.model.Status;

/**
 * Created by xiaowu on 15/6/7.
 */
public class RvAdapter extends RecyclerView.Adapter<RvAdapter.ViewHolder>{

    private List<DownloadInfo> mDataList;

    public RvAdapter(List<DownloadInfo> mDataList){
        this.mDataList = mDataList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mFileNameView;
        private TextView mStatusView;
        private Button mControlBtn;
        private ProgressBar mProgressBar;

        public ViewHolder(View itemView){
            super(itemView);
            mFileNameView = (TextView) itemView.findViewById(R.id.m_file_name_view);
            mStatusView = (TextView) itemView.findViewById(R.id.m_status_view);
            mControlBtn = (Button) itemView.findViewById(R.id.m_control_btn);
            mProgressBar = (ProgressBar) itemView.findViewById(R.id.m_progress_view);
        }

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    @Override
    public void onBindViewHolder(RvAdapter.ViewHolder holder, int position) {
        DownloadInfo downloadInfo = mDataList.get(position);
        holder.mFileNameView.setText(downloadInfo.getFileName());
        holder.mStatusView.setText(Status.valueOf(downloadInfo.getStatus()) + " ("
                +getProgress(downloadInfo.getFileLength(), downloadInfo.getProgress()) + "%)");
        holder.mProgressBar.setProgress(getProgress(downloadInfo.getFileLength(), downloadInfo.getProgress()));
        setControlBtn(downloadInfo, holder.mControlBtn);

    }

    @Override
    public RvAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.download_list_item_view, null);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    private int getProgress(long fileLength,long progress ){
        if(progress > 0){
            return (int) (progress * 100 / fileLength);
        }else {
            return 0;
        }
    }

    public void updateDatas(List<DownloadInfo> mDataList){
        this.mDataList = mDataList;
    }

    private void setControlBtn(DownloadInfo downloadInfo, Button mControlBtn){
        mControlBtn.setTag(downloadInfo);
        mControlBtn.setOnClickListener(mControlBtnOnClickListener);
        switch (downloadInfo.getStatus()){
            case 0:
                mControlBtn.setText("等待中");
                break;
            case 1:
                mControlBtn.setText("暂停");
                break;
            case 2:
                mControlBtn.setText("暂停");
                break;
            case 3:
                mControlBtn.setText("已完成");
                break;
            case 4:
                mControlBtn.setText("继续");
                break;
            case 5:
                mControlBtn.setText("重试");
                break;
        }
    }

    private View.OnClickListener mControlBtnOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DownloadInfo downloadInfo = (DownloadInfo) v.getTag();
            switch (downloadInfo.getStatus()){
                case 0:
                    DownloadManager.stopTask(downloadInfo);
                    break;
                case 1:
                    DownloadManager.stopTask(downloadInfo);
                    break;
                case 2:
                    DownloadManager.stopTask(downloadInfo);
                    break;
                case 3:
                    break;
                case 4:
                    DownloadManager.resumeTask(downloadInfo);
                    break;
                case 5:
                    DownloadManager.resumeTask(downloadInfo);
                    break;
            }
        }
    };

}
