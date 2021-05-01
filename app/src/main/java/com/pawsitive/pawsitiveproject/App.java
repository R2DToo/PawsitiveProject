package com.pawsitive.pawsitiveproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import net.gotev.uploadservice.UploadServiceConfig;

/**
 * App class used to setup the notification channel for uploading images
 */
public class App extends Application {
    private final String notificationID = "PawsitiveChannel";

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    notificationID,
                    "PawsitiveApp Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        UploadServiceConfig.initialize(this, notificationID, BuildConfig.DEBUG);
    }
}
