package com.firefly.filepicker.utils;

import android.os.ConditionVariable;
import android.os.Looper;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;

public class SmbFileHelper {
    private static boolean isMainThread() {
        return Thread.currentThread().getId()
                == Looper.getMainLooper().getThread().getId();
    }

    private static void runNewThread(Runnable runnable) {
        new Thread(runnable).start();
    }

    public static boolean isFile(final SmbFile smbFile) throws SmbException {
        if (isMainThread()) {
            final boolean[] result = new boolean[1];
            final ConditionVariable conditionVariable = new ConditionVariable();

            runNewThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result[0] = smbFile.isFile();
                    } catch (SmbException e) {
                        e.printStackTrace();
                        result[0] = false;
                    }
                    conditionVariable.open();
                }
            });
            conditionVariable.block();
            return result[0];
        } else {
            return smbFile.isFile();
        }
    }

    public static int getType(final SmbFile smbFile) throws SmbException {
        if (isMainThread()) {
            final ConditionVariable conditionVariable = new ConditionVariable();
            final int[] key = {-1};

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        key[0] = smbFile.getType();
                    } catch (SmbException e) {
                        e.printStackTrace();
                    }
                    conditionVariable.open();
                }
            }).start();
            conditionVariable.block();
            if (key[0] == -1) {
                throw new SmbException(SmbException.NT_STATUS_UNSUCCESSFUL, true);
            }

            return key[0];
        } else {
            return smbFile.getType();
        }
    }

    public static String getName(final SmbFile smbFile) {
        if (isMainThread()) {
            final ConditionVariable conditionVariable = new ConditionVariable();
            final String[] key = { null };

            new Thread(new Runnable() {
                @Override
                public void run() {
                    key[0] = smbFile.getName();
                    conditionVariable.open();
                }
            }).start();
            conditionVariable.block();
            if (key[0] == null) {
                return "";
            }

            return key[0];
        } else {
            return smbFile.getName();
        }
    }
}
