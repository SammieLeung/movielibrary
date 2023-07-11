package com.firefly.filepicker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private ContentObserver setupCompleteObserver;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();


        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            if (Settings.Global.getInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 0) == 1 &&
                    Settings.Secure.getInt(context.getContentResolver(), "user_setup_complete", 0) == 1) {
                Intent service = new Intent(context, DLNAService.class);
                context.startService(service);
            } else {
                startListening(context, () -> {
                    Log.d("filepicker.BootReceiver","start DLNAService");
                    Intent service = new Intent(context, DLNAService.class);
                    context.startService(service);
                    stopListening(context);
                });
            }

        }
    }

    public void startListening(Context context, SetupWizardCallback callback) {
        // 创建一个 Handler 对象来处理监听事件
        Handler handler = new Handler(context.getMainLooper());

        // 监听 USER_SETUP_COMPLETE 设置变化
        Uri userSetupCompleteUri = Settings.Secure.getUriFor("user_setup_complete");
        setupCompleteObserver = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                boolean isUserSetupComplete = isUserSetupComplete(context);
                if (isUserSetupComplete) {
                    callback.run();
                }
            }
        };
        context.getContentResolver().registerContentObserver(userSetupCompleteUri, false, setupCompleteObserver);
    }

    public void stopListening(Context context) {
        // 停止监听
        if (setupCompleteObserver != null) {
            context.getContentResolver().unregisterContentObserver(setupCompleteObserver);
        }
    }

    private boolean isUserSetupComplete(Context context) {
        int userSetupComplete = Settings.Secure.getInt(context.getContentResolver(),
                "user_setup_complete", 0);
        return userSetupComplete == 1;
    }


    interface SetupWizardCallback {
        public void run();
    }
}
