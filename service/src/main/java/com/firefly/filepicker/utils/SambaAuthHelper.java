package com.firefly.filepicker.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.archos.filecorelibrary.filecorelibrary.jcifs.JcifsUtils;
import com.firefly.filepicker.roomdb.Credential;
import com.firefly.filepicker.roomdb.CredentialDao;
import com.firefly.filepicker.roomdb.FileRoomDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jcifs.CIFSContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.smb.SmbFile;

/**
 * Created by rany on 18-3-29.
 */

public class SambaAuthHelper {
    private HashMap<String, Credential> mCredentials;
    private static SambaAuthHelper sInstance;
    private CredentialDao mCredentialDao;

    public static SambaAuthHelper getInstance() {
        if (sInstance == null) {
            synchronized (SambaAuthHelper.class) {
                if (sInstance == null)
                    sInstance = new SambaAuthHelper();
            }
        }
        return sInstance;
    }

    private SambaAuthHelper() {
        mCredentials = new HashMap<>();
    }

    public void init(Context context) {
        mCredentialDao = FileRoomDatabase.getDatabase(context).getCredentialDao();
        Observable.create(emitter -> emitter.onNext("")).observeOn(Schedulers.newThread())
                .subscribe(o -> loadAllCredential());
    }

    public void loadAllCredential() {
        List<Credential> persistentCredentials = mCredentialDao.queryAllCredentials();
        mCredentials.clear();
        for (Credential cred : persistentCredentials) {
            cred.isTemporary=false;
            mCredentials.put(getCredentialKey(cred), cred);
        }
    }

    public void addCredential(Credential cred) {
        mCredentials.put(getCredentialKey(cred), cred);
    }

    public void saveCredential(Credential credential) {
        credential.isTemporary = false;
        mCredentials.put(getCredentialKey(credential), credential);
        mCredentialDao.insertCredential(credential);
    }

    //TODO
    public void deleteCIFSContext(SmbFile smbFile) {
        String server = smbFile.getServer();
        String share = smbFile.getShare();
        mCredentialDao.deleteCredential(server, share);
        mCredentials.remove(getCredentialKey(server,share));
    }

    public List<Credential> getAllPersistentCredentials() {
        List<Credential> persistentCredentials = new ArrayList<Credential>();
        for (Credential cred : mCredentials.values()) {
            if (!cred.isTemporary)
                persistentCredentials.add(cred);
        }
        return persistentCredentials;
    }

    public Credential getCredential(String uriString) {
        uriString = uriString.replaceFirst("smb://.*@", "smb://");

        if (mCredentials.containsKey(uriString)) {
            return mCredentials.get(uriString);
        }

        if (uriString.endsWith("/") && uriString.length() > 1 && mCredentials.containsKey(uriString.substring(0, uriString.length() - 1))) {
            return mCredentials.get(uriString.substring(0, uriString.length() - 1));
        }
        /*
            now we check if a parent has credentials, we will keep the longest one
         */
        Credential ret = null;
        for (String parent : mCredentials.keySet()) {
            if (uriString.startsWith(parent)) { // this is potentially the right one
                if (parent.endsWith("/") && (ret == null || ret.mServer.length() < parent.length())) { // this one is appropriate
                    ret = mCredentials.get(parent);
                } else {
                    //we have to check if the caracter after the string "parent" is a / or nothing
                    // to avoid these cases :
                    // credential for /this/is/a/path
                    // uriString : /this/is/a/pathbutdifferent
                    if (uriString.charAt(parent.length()) == '/' && (ret == null || ret.mServer.length() < parent.length())) {
                        ret = mCredentials.get(parent);
                    }
                }
            }
        }

        return ret;

    }

    /**
     * TODO guest账户处理
     *
     * @param url
     * @return
     */
    public CIFSContext getCIFSContext(String url) {
        CIFSContext cifsContext = null;
        Credential credential = getCredential(url);
        if (credential == null || (TextUtils.isEmpty(credential.getUsername()) && TextUtils.isEmpty(credential.getPassword()))) {
            cifsContext = JcifsUtils.getBaseContext(true).withAnonymousCredentials();
        } else {
            cifsContext = JcifsUtils.getBaseContext(true).withCredentials(new NtlmPasswordAuthenticator(credential.getDomain(), credential.getUsername(), credential.getPassword()));
        }
        return cifsContext;
    }

    public void saveCIFSContext(SmbFile smbFile, CIFSContext cifsContext) {
        Credential credential = getCredential(getCredentialKey(smbFile.getServer(),smbFile.getShare()));
        if (credential == null) {
            NtlmPasswordAuthenticator auth = (NtlmPasswordAuthenticator) cifsContext.getCredentials();
            credential = new Credential(smbFile.getServer(), smbFile.getShare(), auth.getUserDomain(), auth.getUsername(), auth.getPassword());
            saveCredential(credential);
        } else {
            if (credential.isTemporary)
                saveCredential(credential);
        }
    }

    public void addTempCIFSContext(SmbFile smbFile, CIFSContext cifsContext) {
        NtlmPasswordAuthenticator auth = (NtlmPasswordAuthenticator) cifsContext.getCredentials();
        Credential credential = new Credential(smbFile.getServer(), smbFile.getShare(), auth.getUserDomain(), auth.getUsername(), auth.getPassword());
        addCredential(credential);
    }

    public String getCredentialKey(Credential credential) {

        return "smb://" + credential.getServer() + "/" + (TextUtils.isEmpty(credential.getShare())?"":credential.getShare());
    }

    public String getCredentialKey(String server, String share) {
        return "smb://" + server + "/" +(TextUtils.isEmpty(share)?"":share);
    }

//    public static void save(Context context, Map<String, CIFSContext> maps) {
//        File filesDir = context.getFilesDir();
//
//        for (Map.Entry<String, CIFSContext> entry : maps.entrySet()) {
//            try {
//                File outFile = new File(filesDir, SAVE_DIR + entry.getKey());
//                if (!outFile.exists()) {
//                    outFile.getParentFile().mkdirs();
//                    outFile.createNewFile();
//                }
//                FileOutputStream outputStream = new FileOutputStream(outFile);
//                ObjectOutputStream oos = new ObjectOutputStream(outputStream);
//                oos.writeObject(entry.getValue());
//                oos.flush();
//                outputStream.close();
//                oos.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static Map<String, CIFSContext> readAll(Context context) {
//        Map<String, CIFSContext> maps = new HashMap<>();
//        File fileDir = new File(context.getFilesDir(), SAVE_DIR);
//        File[] files = fileDir.listFiles();
//
//        if (files == null) {
//            return maps;
//        }
//
//        for (File inFile : files) {
//            try {
//                FileInputStream inputStream = new FileInputStream(inFile);
//                ObjectInputStream ois = new ObjectInputStream(inputStream);
//                CIFSContext cifsContext =
//                        (CIFSContext) ois.readObject();
//                maps.put(inFile.getName(), cifsContext);
//                ois.close();
//                inputStream.close();
//            } catch (IOException | ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return maps;
//    }
//
//    public static CIFSContext read(Context context, String key) {
//        CIFSContext cifsContext = SingletonContext.getInstance().withAnonymousCredentials();
//        File inFile = new File(context.getFilesDir(), SAVE_DIR + key);
//
//        if (!inFile.exists()) {
//            return cifsContext;
//        }
//
//        try {
//            FileInputStream inputStream = new FileInputStream(inFile);
//            ObjectInputStream ois = new ObjectInputStream(inputStream);
//            cifsContext = (CIFSContext) ois.readObject();
//            ois.close();
//            inputStream.close();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return cifsContext;
//    }
//
//    public static String getSmbAuthKey(final SmbFile smbFile) {
//        try {
//            switch (SmbFileHelper.getType(smbFile)) {
//                case SmbFile.TYPE_SERVER:
//                    return HashHelper.md5(smbFile.getServer());
//                case SmbFile.TYPE_FILESYSTEM:
//                case SmbFile.TYPE_SHARE:
//                    return HashHelper.md5(smbFile.getServer() + "/" + smbFile.getShare());
//            }
//        } catch (SmbException e) {
//            e.printStackTrace();
//        }
//        return HashHelper.md5(smbFile.toString());
//    }
}
