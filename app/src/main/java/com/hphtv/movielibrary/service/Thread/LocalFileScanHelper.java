package com.hphtv.movielibrary.service.Thread;

import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.hphtv.movielibrary.data.Constants;
import com.hphtv.movielibrary.roomdb.MovieLibraryRoomDatabase;
import com.hphtv.movielibrary.roomdb.dao.DeviceDao;
import com.hphtv.movielibrary.roomdb.dao.ScanDirectoryDao;
import com.hphtv.movielibrary.roomdb.dao.ShortcutDao;
import com.hphtv.movielibrary.roomdb.dao.VideoFileDao;
import com.hphtv.movielibrary.roomdb.entity.Device;
import com.hphtv.movielibrary.roomdb.entity.ScanDirectory;
import com.hphtv.movielibrary.roomdb.entity.Shortcut;
import com.hphtv.movielibrary.roomdb.entity.VideoFile;

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
public class LocalFileScanHelper {
    public static final String TAG = LocalFileScanHelper.class.getSimpleName();
    private static final int MAX_DIRS = 2000;//扫描目录队列最大长度

    public static void scanAllLocalShortcuts(Context context) {
        ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(context).getShortcutDao();
        List<Shortcut> shortcutList = shortcutDao.queryAllLocalShortcuts();
        scanShortcuts(context,shortcutList);
        Intent intent = new Intent();
        intent.setAction(Constants.BroadCastMsg.POSTER_PAIRING);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void scanShortcut(Context context,Shortcut shortcut){
        List<Shortcut> shortcutList=new ArrayList<>();
        shortcutList.add(shortcut);
        scanShortcuts(context,shortcutList);
    }

    /**
     * 搜索索引下的所有文件，保存到数据库
     * @param context
     * @param shortcutList
     */
    private static void scanShortcuts(Context context,List<Shortcut> shortcutList) {
        LinkedList<ScanDirectory> scanDirectoryList = new LinkedList<>();
        LinkedList<ScanDirectory> tmpScanDirectoryList = new LinkedList<>();

        ArrayList<VideoFile> allVideoFileList = new ArrayList<>();

        DeviceDao deviceDao = MovieLibraryRoomDatabase.getDatabase(context).getDeviceDao();
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
        ScanDirectoryDao scanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(context).getScanDirectoryDao();
        ShortcutDao shortcutDao=MovieLibraryRoomDatabase.getDatabase(context).getShortcutDao();

        //1 - 获取所有本地文件夹
        for (Shortcut shortcut : shortcutList) {
            Device device = deviceDao.querybyMountPath(shortcut.devicePath);
            if (device != null) {
                ScanDirectory scanDirectory = new ScanDirectory(shortcut.uri, shortcut.devicePath);
                scanDirectoryList.add(scanDirectory);
            }
        }

        boolean isOverMaxDirs = false;//待扫描队列超过最大缓存标记
        while (!scanDirectoryList.isEmpty()) {
            //文件信息超过100则先保存到数据库
            if (allVideoFileList.size() > 100) {
                saveVideoFileInfotoDB(allVideoFileList, videoFileDao);
            }
            if (scanDirectoryList.size() > MAX_DIRS) {
                //设置超过最大缓存标记
                isOverMaxDirs = true;
            } else if (scanDirectoryList.size() < MAX_DIRS / 2 && isOverMaxDirs) {
                //取消超过最大缓存标记
                isOverMaxDirs = false;
                //从数据库中取回暂存目录数据
                List<ScanDirectory> dbScanDirectories = scanDirectoryDao.queryTmpScanDirectories(MAX_DIRS - scanDirectoryList.size());
                if (dbScanDirectories != null && dbScanDirectories.size() > 0) {
                    for (ScanDirectory itemDirectory : dbScanDirectories) {
                        scanDirectoryList.add(itemDirectory);
                    }
                    //删除数据库中对应的数据
                    scanDirectoryDao.deleteScanDirectories(dbScanDirectories);
                }
            }
            //获取待扫描设备队列的一个设备。
            ScanDirectory scanDirectory = scanDirectoryList.remove();
            File dirFile = new File(scanDirectory.path);
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
                        if (isOverMaxDirs) {//当前扫描目录队列超过1000个则先缓存
                            ScanDirectory tmpScanDirectory = new ScanDirectory(subFile.getPath(), scanDirectory.devicePath);
                            tmpScanDirectory.parentPath = scanDirectory.parentPath;
                            tmpScanDirectoryList.add(tmpScanDirectory);
                            if (tmpScanDirectoryList.size() >= MAX_DIRS / 2) {//暂存1000个
                                scanDirectoryDao.insertScanDirectories(tmpScanDirectoryList.toArray(new ScanDirectory[0]));
                            }
                        } else {
                            ScanDirectory tmpScanDirectory = new ScanDirectory(subFile.getPath(), scanDirectory.devicePath);
                            tmpScanDirectory.parentPath = scanDirectory.parentPath;
                            scanDirectoryList.add(tmpScanDirectory);
                        }
                    } else {
                        if(!subFile.getName().startsWith(".")) {
                            //获取文件信息，传入文件信息暂存列表
                            VideoFile videoFile = buildVideoInfoFromFile(subFile, scanDirectory);
                            if (videoFile != null) {
                                allVideoFileList.add(videoFile);
                            }
                        }
                    }
                }

            }
        }
        //队列结束保存文件信息入库
        saveVideoFileInfotoDB(allVideoFileList, videoFileDao);
        updateShortcutFileCount(shortcutList,shortcutDao);
//        clearRedundantVideoFiles();
//        Intent intent = new Intent();
//        intent.setAction(Constants.BroadCastMsg.POSTER_PAIRING);
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
     * @return
     */
    private static VideoFile buildVideoInfoFromFile(File file, ScanDirectory scanDirectory) {
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
            videoFile.filename = (fileName);
            videoFile.path = (file.getPath());
            videoFile.devicePath = scanDirectory.devicePath;
            videoFile.dirPath = scanDirectory.parentPath;
            return videoFile;
        }
        return null;
    }

    /**
     * 文件信息入库
     */
    private static void saveVideoFileInfotoDB(ArrayList<VideoFile> videoFileList, VideoFileDao videoFileDao) {
        if (!videoFileList.isEmpty()) {
            for (VideoFile videoFile : videoFileList) {
                VideoFile tVideoFile = videoFileDao.queryByPath(videoFile.path);
                if (tVideoFile != null) {
                    videoFile.vid = tVideoFile.vid;
                    videoFile.isScanned = tVideoFile.isScanned;
                    videoFile.keyword = tVideoFile.keyword;
                    videoFile.addTime = System.currentTimeMillis();
                    videoFile.lastPlayTime = tVideoFile.lastPlayTime;
                    videoFileDao.update(videoFile);
                } else {
                    videoFile.addTime = System.currentTimeMillis();
                    long id = videoFileDao.insertOrIgnore(videoFile);
                    videoFile.vid = id;
                }
            }
            videoFileList.clear();
        }
    }

    private static void updateShortcutFileCount(List<Shortcut> shortcutList,ShortcutDao shortcutDao){
        for(Shortcut shortcut:shortcutList) {
            int fileCount = shortcutDao.queryTotalFiles(shortcut.uri);
            int matchedCount = shortcutDao.queryMatchedFiles(shortcut.uri);
            shortcut.fileCount = fileCount;
            shortcut.posterCount = matchedCount;
            shortcutDao.updateShortcut(shortcut);
        }
    }


}
