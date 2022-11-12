package com.hphtv.movielibrary.service.Thread;

import android.content.Context;

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
import com.station.kit.util.LogUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


/**
 * @author lxp
 * @date 18-12-17
 * 扫描文件设备线程
 */
public class LocalFileScanHelper {
    public static final String TAG = LocalFileScanHelper.class.getSimpleName();
    private static final int MAX_DIRS = 1000;//扫描目录队列最大长度

    public static void searchAllConnectedLocalShortcuts(Context context) {
        ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(context).getShortcutDao();
        List<Shortcut> shortcutList = shortcutDao.queryAllConnectedLocalShortcuts();
        long curTime = System.currentTimeMillis();
        scanShortcuts(context, shortcutList);
        LogUtil.v(TAG, "search All Local takes " + (System.currentTimeMillis() - curTime) + "ms");
    }

    public static void searchShortcut(Context context, Shortcut shortcut) {
        List<Shortcut> shortcutList = new ArrayList<>();
        shortcutList.add(shortcut);
        scanShortcuts(context, shortcutList);
    }

    public static List<Shortcut> scanDevice(Context context, String devicePath) {
        ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(context).getShortcutDao();
        List<Shortcut> shortcutList = shortcutDao.queryLocalShortcuts(devicePath);
        scanShortcuts(context, shortcutList);
        return shortcutList;
    }

    /**
     * 搜索索引下的所有文件，保存到数据库
     *
     * @param context
     * @param shortcutList
     */
    private static void scanShortcuts(Context context, List<Shortcut> shortcutList) {

        DeviceDao deviceDao = MovieLibraryRoomDatabase.getDatabase(context).getDeviceDao();
        for (Shortcut shortcut : shortcutList) {
            Device device = deviceDao.querybyMountPath(shortcut.devicePath);
            if (device != null) {
                runScanProcess(context,shortcut);
            }
        }
    }


    private static void runScanProcess(Context context, Shortcut shortcut) {
        long addTime = System.currentTimeMillis();

        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
        ScanDirectoryDao scanDirectoryDao = MovieLibraryRoomDatabase.getDatabase(context).getScanDirectoryDao();

        boolean isOverMaxDirs = false;//待扫描队列超过最大缓存标记
        LinkedList<ScanDirectory> scanDirectoryList = new LinkedList<>();
        ArrayList<VideoFile> allVideoFileList = new ArrayList<>();

        ScanDirectory parentDirectory = new ScanDirectory(shortcut.uri, shortcut.devicePath);
        scanDirectoryList.add(parentDirectory);
        //一.搜索文件
        while (!scanDirectoryList.isEmpty()) {
            //* 文件信息超过100则先保存到数据库
            if (allVideoFileList.size() > 100) {
                saveVideoFileInfoToDB(allVideoFileList, videoFileDao);
            }
            //待扫描设置超过最大缓存标记
            if (scanDirectoryList.size() > MAX_DIRS) {
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
                if (subFiles == null || subFiles.length == 0) continue;
                for (File subFile : subFiles) {
                    if (!subFile.exists())//判断子文件是否存在
                        continue;
                    //文件夹加入待扫描队列
                    if (subFile.isDirectory()) {
                        ScanDirectory childDirectory = new ScanDirectory(subFile.getPath(), scanDirectory.devicePath);
                        if (isOverMaxDirs) {//当前扫描目录队列超过2000个则先缓存
                            scanDirectoryDao.insertScanDirectories(childDirectory);
                        } else {
                            scanDirectoryList.add(childDirectory);
                        }
                    } else {
                        if (!subFile.getName().startsWith(".")) {
                            //获取文件信息，传入文件信息暂存列表
                            VideoFile videoFile = buildVideoInfoFromFile(subFile, parentDirectory, addTime);
                            if (videoFile != null) {
                                allVideoFileList.add(videoFile);
                            }
                        }
                    }
                }

            }
        }

        //二、保存文件列表
        saveVideoFileInfoToDB(allVideoFileList, videoFileDao);
        //三、删除多余文件和对应关系表的行
        clearRedundantFile(context, parentDirectory.path, addTime);
        //四、更新文件数量
        updateShortcutFileCount(context,shortcut);
    }


    private boolean isNoMedia(File file) {
        if (file.getName().equals(".nomedia")) return true;
        return false;
    }

    /**
     * 生成VdieoFile实体类
     *
     * @param file
     * @return
     */
    private static VideoFile buildVideoInfoFromFile(File file, ScanDirectory parentDir, long addTime) {
        String path = file.getPath();
        int dotIndex = path.lastIndexOf(".");
        if (dotIndex < 0) return null;
        int startIndex = dotIndex + 1;
        if (startIndex >= path.length()) return null;
        String tailEx = path.substring(startIndex).toLowerCase();
        if (Arrays.binarySearch(Constants.VIDEO_SUFFIX, tailEx) >= 0) {
            String fileName = file.getName();
            VideoFile videoFile = new VideoFile();
            videoFile.filename = (fileName);
            videoFile.path = (file.getPath());
            videoFile.devicePath = parentDir.devicePath;
            videoFile.dirPath = parentDir.path;
            videoFile.addTime = addTime;
            return videoFile;
        }
        return null;
    }

    /**
     * 文件信息入库
     */
    private static void saveVideoFileInfoToDB(ArrayList<VideoFile> videoFileList, VideoFileDao videoFileDao) {
        if (!videoFileList.isEmpty()) {
            for (VideoFile videoFile : videoFileList) {
                VideoFile tVideoFile = videoFileDao.queryByPath(videoFile.path);
                if (tVideoFile != null) {
                    tVideoFile.addTime = videoFile.addTime;
                    if (!Objects.equals(videoFile.dirPath, tVideoFile.dirPath)) {
                        if (videoFile.dirPath.contains(tVideoFile.dirPath)) {
                            tVideoFile.dirPath = videoFile.dirPath;
                        }
                    }
                    videoFileDao.update(tVideoFile);
                } else {
                    videoFileDao.insertOrIgnore(videoFile);
                }
            }
            videoFileList.clear();
        }
    }

    /**
     * 清除没有更新add_time的文件
     * （先检查设备是否连接，防止删除离线设备文件）
     *
     * @param context
     * @param dir_path
     * @param add_time
     */
    private static void clearRedundantFile(Context context, String dir_path, long add_time) {
        VideoFileDao videoFileDao = MovieLibraryRoomDatabase.getDatabase(context).getVideoFileDao();
        List<VideoFile> redundantFiles = videoFileDao.queryRedundantFile(dir_path, add_time);
        for (VideoFile v : redundantFiles) {
            videoFileDao.removeRelation(v.path);
            videoFileDao.delete(v);
        }
    }

    private static void updateShortcutFileCount(Context context, Shortcut shortcut) {
        ShortcutDao shortcutDao = MovieLibraryRoomDatabase.getDatabase(context).getShortcutDao();
        int fileCount = shortcutDao.queryTotalFiles(shortcut.uri);
        int matchedCount = shortcutDao.queryMatchedFiles(shortcut.uri);
        shortcut.fileCount = fileCount;
        shortcut.posterCount = matchedCount;
        shortcutDao.updateShortcut(shortcut);
    }


}
