package com.ai.face.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yifeng.face.R;

import java.io.File;

import mehdi.sakout.aboutpage.AboutPage;
import mehdi.sakout.aboutpage.Element;

public class about extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        View aboutPage = new AboutPage(this)
                .isRTL(false)
                .setImage(R.mipmap.ic_launcher)//图片
                .setDescription("基于Android Studio\n开发的人脸门禁系统设计与实现\n\n\t\t\t\t\t传统的门禁系统以钥匙作为验证手段，便捷程度低，丢失钥匙之后会导致极大的安全问题。人脸是一种极易获得的生物特征，具有唯一性、稳定性的特点，并且使用时设备无需与人脸接触，因此可以作为新一代的门禁验证手段。近年来，随着Android移动设备性能的不断提升，使得在移动设备上进行人脸识别成为可能。本设计旨在设计并开发一个基于Android平台的人脸识别门禁系统，并解决在实际运用中可能遇到的光照变化，人脸姿态变化等情况。")//介绍
                .addItem(new Element().setTitle("Version 1.0").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(about.this, ScrollingActivity.class));
                        finish();
                    }
                }))
                .addItem(new Element().setTitle("我们的网站").setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent =new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("http://www.chuankuan.vip:666"));
                        PendingIntent pendingIntent = PendingIntent.getActivity(about.this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
                        String channelId = createNotificationChannel("0213", "逸风通知", NotificationManager.IMPORTANCE_HIGH);
                        NotificationCompat.Builder notification = new NotificationCompat.Builder(about.this, channelId)
                                .setContentTitle("温馨提示")
                                .setContentText("点击此处打开我们的网站")
                                .setContentIntent(pendingIntent)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setPriority(NotificationCompat.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .setSound(Uri.parse("/system/media/audio/notifications/Fresh.ogg"), AudioManager.STREAM_VOICE_CALL);
                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(about.this);
                        if (ActivityCompat.checkSelfPermission(about.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        notificationManager.notify(100, notification.build());
                    }

                    private String createNotificationChannel(String channelID, String channelNAME, int level) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                            NotificationChannel channel = new NotificationChannel(channelID, channelNAME, level);

                            manager.createNotificationChannel(channel);
                            return channelID;
                        } else {
                            return null;
                        }
                    }

                }))
                .addGroup("与我联系")
                .addEmail("caochuankuan@foxmail.com")//邮箱
                .addWebsite("http://chuankuan.vip:666")//网站
                .addPlayStore("com.yifeng.face")//应用商店
                .addGitHub("caochuankuan")//github
                .create();


        setContentView(aboutPage);
    }
}
