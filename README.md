# DownlaodUtil_sample
支持多线程断点继传下载的工具库
==============================

可以支持对单个文件或是文件列表的断点继传、后台下载，对应用中的下载提供一个公共的解决方案

--------

使用方法
--------

```Java

  //首先开启下载的服务
  DownloadService.startService(MainActivity.this);
  //AndroidManifest.xml中service的配置
  <service android:name=".service.DownloadService" />

   /**
    * 创建一个DownloadInfo对象
    * url 文件地址
    * fileName 下载后给文件的命名
    * fileId 文件的Id 可自己定义 不要重复
    * mLoadDataListener 下载任务各种状态的回调
    */
    String url = "http://www.muzhiwan.com/index.php?action=common&opt=downloadstat&vid=112707";
    //单个文件URL直接用Sting
    DownloadInfo downloadInfo = new DownloadInfo(url, "mzw1.apk", 10001, mLoadDataListener); 
    List<String> urls = new ArrayList();
    urls.add("http://www.muzhiwan.com/index.php?action=common&opt=downloadstat&vid=112707");
    urls.add("http://www.muzhiwan.com/index.php?action=common&opt=downloadstat&vid=112707");
    urls.add("http://www.muzhiwan.com/index.php?action=common&opt=downloadstat&vid=112707");
    //文件列表urls用List<String>
    DownloadInfo downloadInfoFieList = new DownloadInfo(urls, "mzw1.apk", 10001, mLoadDataListener); 
    
    //添加一个新下载任务
    DownloadManager.addTask(downloadInfo);
    //暂停一个下载中的任务
    DownloadManager.stopTask(downloadInfo);
    //暂停所有下载中的任务
    DownloadManager.stopAllTask();
    //恢复一个下载中的任务
    DownloadManager.resumeTask(downloadInfo);
    
    //退出应用停止下载服务
    Intent intent=new Intent(this,DownloadService.class);
    intent.setAction(Constant.ACTION_STOP);
    this.startService(intent);
```

    
    
