package com.firefly.filepicker.utils;

import android.content.Context;
import android.os.ConditionVariable;
import android.os.Looper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import jcifs.CIFSContext;
import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

/**
 * Created by rany on 18-3-29.
 */

public class SambaAuthHelper {
    private static String SAVE_DIR = "samba/";
    public static final CIFSContext GUEST = SingletonContext.getInstance().withGuestCrendentials();

    public static void save(Context context, Map<String, CIFSContext> maps) {
        File filesDir = context.getFilesDir();

        for (Map.Entry<String, CIFSContext> entry : maps.entrySet()) {
            try {
                File outFile = new File(filesDir, SAVE_DIR + entry.getKey());
                if (!outFile.exists()) {
                    outFile.getParentFile().mkdirs();
                    outFile.createNewFile();
                }
                FileOutputStream outputStream = new FileOutputStream(outFile);
                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
                oos.writeObject(entry.getValue());
                oos.flush();
                outputStream.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Map<String, CIFSContext> readAll(Context context) {
        Map<String, CIFSContext> maps = new HashMap<>();
        File fileDir = new File(context.getFilesDir(), SAVE_DIR);
        File[] files = fileDir.listFiles();

        if (files == null) {
            return maps;
        }

        for (File inFile : files) {
            try {
                FileInputStream inputStream = new FileInputStream(inFile);
                ObjectInputStream ois = new ObjectInputStream(inputStream);
                CIFSContext cifsContext =
                        (CIFSContext) ois.readObject();
                maps.put(inFile.getName(), cifsContext);
                ois.close();
                inputStream.close();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return maps;
    }

    public static CIFSContext read(Context context, String key) {
        CIFSContext cifsContext = SingletonContext.getInstance().withAnonymousCredentials();
        File inFile = new File(context.getFilesDir(), SAVE_DIR + key);

        if (!inFile.exists()) {
            return cifsContext;
        }

        try {
            FileInputStream inputStream = new FileInputStream(inFile);
            ObjectInputStream ois = new ObjectInputStream(inputStream);
            cifsContext = (CIFSContext) ois.readObject();
            ois.close();
            inputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return cifsContext;
    }

    public static String getSmbAuthKey(final SmbFile smbFile) {
        try {
            switch (SmbFileHelper.getType(smbFile)) {
                case SmbFile.TYPE_SERVER:
                    return HashHelper.md5(smbFile.getServer());
                case SmbFile.TYPE_FILESYSTEM:
                case SmbFile.TYPE_SHARE:
                    return HashHelper.md5(smbFile.getServer() + "/" + smbFile.getShare());
            }
        } catch (SmbException e) {
            e.printStackTrace();
        }
        return HashHelper.md5(smbFile.toString());
    }
}
