package com.hphtv.movielibrary.service.Thread;

import android.content.Context;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;
import com.station.kit.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * @author lxp
 * @date 18-12-17
 * 扫描文件设备线程
 */
public class FileScanThread implements Callable<Boolean> {
    public static final String TAG = FileScanThread.class.getSimpleName();
    private boolean isContainsSubfolder = true;
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
    private List<String> mTmpVideoFilePaths = new ArrayList<>();

    private boolean mIsOverMaxDirs = false;

    public FileScanThread(Context context, Device device) {
        this.mDevice = device;
        this.mPath = mDevice.path;
//        this.mScanDirectories.add(new ScanDirectory(mPath, mDevice.id));
        this.mDeviceDao = MovieLibraryRoomDatabase.getDatabase(context).getDeviceDao();
        this.mScanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(context).getScanDirectoryDao();
        this.mVideoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
    }

    @Override
    public Boolean call() throws Exception {
        mScanDirectories.addAll(mScanDirectoryDao.queryScanDirByDevicePath(mDevice.path));
//        if (mScanDirectories.size() == 0 && mPath.equals(StorageHelper.getFlashStoragePath(MovieApplication.getInstance()))) {
//            ScanDirectory scanDirectory = new ScanDirectory(new File(mPath, "/Station/Download").toString(), mDevice.id);
//            ScanDirectory scanDirectory2 = new ScanDirectory(new File(mPath, "/Download").toString(), mDevice.id);
//            ScanDirectory scanDirectory3 = new ScanDirectory(new File(mPath, "/Movies").toString(), mDevice.id);
//
//            scanDirectory.isUserAdd=false;
//            scanDirectory2.isUserAdd=false;
//            scanDirectory3.isUserAdd=false;
//
//            mScanDirectoryDao.insertScanDirectories(scanDirectory,scanDirectory2,scanDirectory3);
//
//            mScanDirectories.add(scanDirectory);
//            mScanDirectories.add(scanDirectory2);
//            mScanDirectories.add(scanDirectory3);
//        }
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
            ScanDirectory scanDirectory=mScanDirectories.remove();
            File dirFile = new File(scanDirectory.path);
//            Log.i(TAG, "scanDirectory->dirFile:" + dirFile);
            //初始化当前文件夹下的媒体文件数量

            if (dirFile != null && dirFile.exists()) {
                File[] subFiles = dirFile.listFiles();//获取子文件
                if (subFiles == null || subFiles.length == 0)
                    continue;
//                for (File subFile : subFiles) {
//                    if (subFile.exists() && subFile.isFile() && isNomedia(subFile)) {
//                        nomediaFlag = true;
//                        break;
//                    }
//                }
//
//                if(nomediaFlag){
//                    if(!isContainsSubfolder)
//                    {
//                        for (File subFile : subFiles) {
//                            if (!subFile.exists())//判断子文件是否存在
//                                continue;
//                            //文件夹加入待扫描队列
//                            if (subFile.isDirectory()) {
//                                if (mIsOverMaxDirs) {//当前扫描目录队列超过1000个则先缓存
//                                    mTmpScanDirectories.add(new ScanDirectory(subFile.getPath(), mDevice.id));
//                                    if (mTmpScanDirectories.size() >= MAX_DIRS / 2) {//暂存1000个
//                                        mScanDirectoryDao.insertScanDirectories(mTmpScanDirectories.toArray(new ScanDirectory[0]));
//                                    }
//                                } else {
//                                    mScanDirectories.add(new ScanDirectory(subFile.getPath(), mDevice.id));
//                                }
//                            }
//                        }
//                    }
//                }else{
                for (File subFile : subFiles) {
                    if (!subFile.exists())//判断子文件是否存在
                        continue;
                    //文件夹加入待扫描队列
                    if (subFile.isDirectory()) {
                        if (mIsOverMaxDirs) {//当前扫描目录队列超过1000个则先缓存
                            ScanDirectory tmpScanDirectory=new ScanDirectory(subFile.getPath(), mDevice.path);
                            tmpScanDirectory.parentPath=scanDirectory.parentPath;
                            mTmpScanDirectories.add(tmpScanDirectory);
                            if (mTmpScanDirectories.size() >= MAX_DIRS / 2) {//暂存1000个
                                mScanDirectoryDao.insertScanDirectories(mTmpScanDirectories.toArray(new ScanDirectory[0]));
                            }
                        } else {
                            ScanDirectory tmpScanDirectory=new ScanDirectory(subFile.getPath(), mDevice.path);
                            tmpScanDirectory.parentPath=scanDirectory.parentPath;
                            mScanDirectories.add(tmpScanDirectory);
                        }
                    } else {
                        //获取文件信息，传入文件信息暂存列表
                        VideoFile videoFile = buildVideoInfoFromFile(subFile, mDevice,scanDirectory);
                        if (videoFile != null) {
                            ++videoCount;
                            mVideoFiles.add(videoFile);
                            mTmpVideoFilePaths.add(videoFile.path);
                        }
                    }
                }
                mDevice.fileCount = (videoCount);
                mDeviceDao.updateDevice(mDevice);
//                }

            }
        }
        //队列结束保存文件信息入库
        saveVideoFileInfotoDB();
        LogUtil.v("fileScanThread " + mDevice.name);
        return true;
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
    private VideoFile buildVideoInfoFromFile(File file, Device device,ScanDirectory scanDirectory) {
        String path = file.getPath();
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex < 0)
            return null;
        int startIndex = dotIndex + 1;
        if (startIndex >= path.length())
            return null;
        String tailEx = path.substring(startIndex).toLowerCase();
        if (Arrays.binarySearch(Constants.VIDEO_SUFFIX, tailEx) >= 0) {
            String fileName = file.getName();
            VideoFile videoFile = new VideoFile();
            videoFile.deviceId = (device.id);
            videoFile.filename = (fileName);
            videoFile.path = (file.getPath());
            videoFile.dirPath=scanDirectory.parentPath;
            return videoFile;
        }
        return null;
    }


    /**
     * 从数据库将数据读入扫描目录队列
     */
    private void loadScanDirectoriesFromDB() {
        List<ScanDirectory> dbScanDirectories = mScanDirectoryDao.queryTmpScanDirectories(mDevice.path, MAX_DIRS - mScanDirectories.size());
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
            for (VideoFile videoFile : mVideoFiles) {
                VideoFile tVideoFile = mVideoFileDao.queryByPath(videoFile.path);
                if (tVideoFile != null) {
                    videoFile.vid = tVideoFile.vid;
                    videoFile.isScanned = tVideoFile.isScanned;
                    videoFile.keyword = tVideoFile.keyword;
                    videoFile.addTime = System.currentTimeMillis();
                    videoFile.lastPlayTime=tVideoFile.lastPlayTime;
                    mVideoFileDao.update(videoFile);
                } else {
                    videoFile.addTime = System.currentTimeMillis();
                    mVideoFileDao.insertOrIgnore(videoFile);
                }

            }
            mVideoFiles.clear();
        }
    }

}
