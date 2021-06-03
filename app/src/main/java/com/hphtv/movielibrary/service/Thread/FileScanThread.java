package com.hphtv.movielibrary.service.Thread;

import android.util.Log;

import com.hphtv.movielibrary.data.ConstData;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.hphtv.movielibrary.roomdb.entity.MovieWrapper;
import com.hphtv.movielibrary.service.DeviceMonitorService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


/**
 * @author lxp
 * @date 18-12-17
 * 扫描文件设备线程
 */
public class FileScanThread extends Thread {
    public static final String TAG = FileScanThread.class.getSimpleName();
    private DeviceMonitorService mService;
    private String mPath;
    private Device mDevice;
    /**
     * 数据库dao类
     */
    private DeviceDao mDeviceDao;
    private ScanDirectoryDao mScanDirectoryDao;
    private VideoFileDao mVideoFileDao;
    /**
     * 扫描的目录队列
     */
    private LinkedList<ScanDirectory> mScanDirectories = new LinkedList<>();
    /**
     * 扫描目录队列最大长度
     */
    private static final int MAX_DIRS = 2000;
    private ArrayList<ScanDirectory> mTmpScanDirectories = new ArrayList<>();
    private ArrayList<VideoFile> mVideoFiles = new ArrayList<>();
    private ArrayList<MovieWrapper> mVideoFile__movies = new ArrayList<>();
    private boolean mIsOverMaxDirs = false;

    public FileScanThread(DeviceMonitorService service, Device device) {
        this.mService = service;
        this.mDevice = device;
        this.mPath = mDevice.localPath;
        this.mScanDirectories.add(new ScanDirectory(mPath, mDevice.id));
        this.mDeviceDao = MovieLibraryRoomDatabase.getDatabase(service).getDeviceDao();
        this.mScanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(service).getScanDirectoryDao();
        this.mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(service).getVideoFileDao();
    }

    @Override
    public void run() {
        super.run();
        int videoCount = 0;
        while (!mScanDirectories.isEmpty()) {
            boolean nomediaFlag = false;
            //文件信息超过100则先保存到数据库
            if (mVideoFiles.size() > 100) {
                saveVideoFileInfotoDB();
            }
            if (mScanDirectories.size() > MAX_DIRS) {
                //设置超过最大缓存标记
                mIsOverMaxDirs = true;
            } else if (mScanDirectories.size() < MAX_DIRS / 2 && mIsOverMaxDirs) {
                //取消超过最大缓存标记
                mIsOverMaxDirs = false;
                //从数据库中取回暂存目录数据
                loadScanDirectoriesFromDB();
            }
            //获取待扫描设备队列的一个设备。
            File dirFile = new File(mScanDirectories.remove().path);
//            Log.i(TAG, "scanDirectory->dirFile:" + dirFile);
            //初始化当前文件夹下的媒体文件数量

            if (dirFile != null && dirFile.exists()) {
                File[] subFiles = dirFile.listFiles();//获取子文件
                if (subFiles == null || subFiles.length == 0)
                    continue;
                for (File subFile : subFiles) {
                    if (!subFile.exists())//判断子文件是否存在
                        continue;
                    //文件夹加入待扫描队列
                    if (subFile.isDirectory()) {
                        if (mIsOverMaxDirs) {//当前扫描目录队列超过1000个则先缓存
                            mTmpScanDirectories.add(new ScanDirectory(subFile.getPath(), mDevice.id));
                            if (mTmpScanDirectories.size() >= MAX_DIRS / 2) {//暂存1000个
                                mScanDirectoryDao.insertScanDirectories(mTmpScanDirectories.toArray(new ScanDirectory[0]));
                            }
                        } else {
                            mScanDirectories.add(new ScanDirectory(subFile.getPath(), mDevice.id));
                        }
                    } else if (!nomediaFlag) {
                        //获取文件信息，传入文件信息暂存列表
                        if (isNomedia(subFile)) {
                            nomediaFlag = true;
                            continue;
                        }
                        VideoFile videoFile = buildVideoInfoFromFile(subFile, mDevice);
                        if (videoFile != null) {
                            ++videoCount;
                            mVideoFiles.add(videoFile);
                        }
                    }
                }
                mDevice.fileCount = (videoCount);
                mDeviceDao.updateDevice(mDevice);
            }
        }
        //队列结束保存文件信息入库
        saveVideoFileInfotoDB();
    }


    private boolean isNomedia(File file) {
        if (file.getName().equals(".nomedia"))
            return true;
        return false;
    }

    /**
     * 生成VdieoFile实体类
     *
     * @param file
     * @param device
     * @return
     */
    private VideoFile buildVideoInfoFromFile(File file, Device device) {
        String path = file.getPath();
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex < 0)
            return null;
        int startIndex = dotIndex + 1;
        if (startIndex >= path.length())
            return null;
        String tailEx = path.substring(startIndex).toLowerCase();
        LogUtil.v(tailEx + " = " + Arrays.binarySearch(ConstData.VIDEO_SUFFIX, tailEx));
        if (Arrays.binarySearch(ConstData.VIDEO_SUFFIX, tailEx) >= 0) {
            String fileName = file.getName();
            VideoFile videoFile = new VideoFile();
            videoFile.deviceId = (device.id);
            videoFile.filename = (fileName);
            videoFile.path = (file.getPath());
            return videoFile;
        }
        return null;
    }


    /**
     * 从数据库将数据读入扫描目录队列
     */
    private void loadScanDirectoriesFromDB() {
        Log.v(TAG, "loadScanDirectoriesFromDB===>");
        List<ScanDirectory> dbScanDirectories = mScanDirectoryDao.queryScanDirectories(mDevice.id, MAX_DIRS - mScanDirectories.size());
        if (dbScanDirectories != null && dbScanDirectories.size() > 0) {
            for (ScanDirectory itemDirectory : dbScanDirectories) {
                mScanDirectories.add(itemDirectory);
            }
            //删除数据库中对应的数据
            mScanDirectoryDao.deleteScanDirectories(dbScanDirectories);
        }
    }


    /**
     * 文件信息入库
     */
    private void saveVideoFileInfotoDB() {
        if (!mVideoFiles.isEmpty()) {
            String[] paths = new String[mVideoFiles.size()];
            int i = 0;
            for (VideoFile videoFile : mVideoFiles) {
                VideoFile tVideoFile = mVideoFileDao.queryByPath(videoFile.path);
                if (tVideoFile != null) {
                    videoFile.vid = tVideoFile.vid;
                    videoFile.isScanned=tVideoFile.isScanned;
                }
                int ret = mVideoFileDao.update(videoFile);
                if (ret == 0)
                    mVideoFileDao.insertOrIgnore(videoFile);
                paths[i++] = videoFile.path;
            }
            mVideoFileDao.deleteByDeviceId(mDevice.id, paths);
            mVideoFiles.clear();
        }

    }


}
