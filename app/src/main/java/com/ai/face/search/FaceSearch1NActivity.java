package com.ai.face.search;

import static com.ai.face.FaceApplication.CACHE_SEARCH_FACE_DIR;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.*;
import static com.ai.face.faceSearch.search.SearchProcessTipsCode.THRESHOLD_ERROR;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;

import com.ai.face.base.view.CameraXFragment;
import com.ai.face.faceSearch.search.FaceSearchEngine;
import com.ai.face.faceSearch.search.SearchProcessBuilder;
import com.ai.face.faceSearch.search.SearchProcessCallBack;
import com.ai.face.faceSearch.utils.RectLabel;
import com.ai.face.utils.VoicePlayer;
import com.ai.face.verify.PasswordValidator_user;
import com.yifeng.face.R;
import com.yifeng.face.databinding.ActivityFaceSearchBinding;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Looper;
import android.widget.Toast;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;


public class FaceSearch1NActivity extends AppCompatActivity {
    private ActivityFaceSearchBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        // 保持屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (binding.mima.getVisibility() == View.VISIBLE) {

                    // 设置全屏
                    getWindow().setFlags(
                            WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN
                    );

                    binding.fragmentCamerax.setVisibility(View.GONE);
                    binding.tips.setVisibility(View.GONE);
                    binding.image.setVisibility(View.GONE);
                    binding.blackScreen.setVisibility(View.VISIBLE);
                    binding.blackScreen.setText(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date()));
                    binding.blackScreen.setTextSize(60);
                    binding.mima.setTextColor(getResources().getColor(R.color.colorPrimary));
                    handler.postDelayed(this,1000);
                }
                if (binding.mima.getVisibility() == View.GONE){
                    binding.fragmentCamerax.setVisibility(View.VISIBLE);
                    binding.tips.setVisibility(View.VISIBLE);
                    binding.image.setVisibility(View.VISIBLE);
                    binding.blackScreen.setVisibility(View.GONE);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            recreate();
                        }
                    }, 500);
                }
            }
        }, 10000);



        binding = ActivityFaceSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //初始化声音
        initMediaPlayer();

        binding.tips.setOnClickListener(v -> {
            final SharedPreferences sp = getSharedPreferences("yifeng",MODE_PRIVATE);
            if (Objects.equals(sp.getString("user", ""), "0")){
                startActivity(new Intent(this, FaceImageEditActivity.class));
            }

        });

        binding.mima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(FaceSearch1NActivity.this, PasswordValidator_user.class));
                finish();
            }
        });

        SharedPreferences sharedPref = getSharedPreferences("faceVerify", Context.MODE_PRIVATE);

        // 1. Camera 的初始化。第一个参数0/1 指定前后摄像头；
        int cameraLens = sharedPref.getInt("cameraFlag", sharedPref.getInt("cameraFlag", 1));

        // 第二个参数linearZoom [0.1f,1.0f] 指定焦距，默认0.1
        CameraXFragment cameraXFragment = CameraXFragment.newInstance(cameraLens,0.2f);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_camerax, cameraXFragment)
                .commit();

        cameraXFragment.setOnAnalyzerListener(imageProxy -> {
            //可以加个红外检测之类的，有人靠近再启动检索服务，不然机器老化快
            if (!isDestroyed() && !isFinishing()) {
                //第二个参数传0表示不裁剪
                //大于0 表示裁剪距中的正方形区域范围为人脸检测区，参数为正方形区域距离屏幕边缘的值
                FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
            }
        });


        // 2.各种参数的初始化设置
        SearchProcessBuilder faceProcessBuilder = new SearchProcessBuilder.Builder(getApplication())
                .setLifecycleOwner(this)
                .setThreshold(0.79f)            //阈值设置，范围限 [0.75 , 0.95] 识别可信度，也是识别灵敏度
                .setLicenceKey("yourLicense key")  //合作的VIP定制客户群体需要
                .setFaceLibFolder(CACHE_SEARCH_FACE_DIR)  //内部存储目录中保存N 个人脸图片库的目录
                .setImageFlipped(cameraLens == CameraSelector.LENS_FACING_FRONT) //手机的前置摄像头imageProxy 拿到的图可能左右翻转
                .setProcessCallBack(new SearchProcessCallBack() {
                    //人脸识别检索回调
                    @Override
                    public void onMostSimilar(String similar, Bitmap realTimeImg) {
                        //根据你的业务逻辑，各种提示 & 触发成功后面的操作
                        binding.searchTips1.setText(similar);
                        TextView mima = findViewById(R.id.mima);
                        mima.setVisibility(View.GONE);
                        //VoicePlayer.getInstance().addPayList(R.raw.success);
                        //VoicePlayer.getInstance().addPayList(R.raw.verify_success);
                        startPlay();

                        // 保存捕捉到的人脸图像
                        saveFaceImage(realTimeImg);

                        Glide.with(getBaseContext())
                                .load(CACHE_SEARCH_FACE_DIR + File.separatorChar + similar)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .transform(new RoundedCorners(11))
                                .into(binding.image);
                    }



                    // 保存捕捉到的人脸图像的方法
                    private void saveFaceImage(Bitmap faceBitmap) {
                        // 保存到本地
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                        String fileName = "face_capture_" + timeStamp + ".png";

                        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                        File imageFile = new File(storageDir, fileName);

                        try (FileOutputStream out = new FileOutputStream(imageFile)) {
                            faceBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                            // Toast.makeText(FaceSearch1NActivity.this, "人脸图像已保存：" + imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            return; // 如果保存失败，不进行上传
                        }

                        // 上传到FTP服务器
                        new UploadToFTPAsyncTask().execute(imageFile, "/app_face/record/");

                    }

                    class UploadToFTPAsyncTask extends AsyncTask<Object, Void, Boolean> {

                        @Override
                        protected Boolean doInBackground(Object... params) {
                            if (params.length >= 2) {
                                File localFile = (File) params[0];
                                String remoteDirectory = (String) params[1];

                                FTPClient ftpClient = new FTPClient();
                                String server = "124.71.2.21";
                                int port = 21;
                                String username = "yifeng";
                                String password = "cck134414";

                                try {
                                    ftpClient.connect(server, port);
                                    ftpClient.login(username, password);
                                    ftpClient.enterLocalPassiveMode();
                                    ftpClient.enterLocalActiveMode();
                                    ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                                    // 将工作目录更改为远程目录
                                    ftpClient.changeWorkingDirectory(remoteDirectory);

                                    try (InputStream inputStream = new FileInputStream(localFile)) {
                                        // 上传文件
                                        return ftpClient.storeFile(localFile.getName(), inputStream);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    // 处理异常
                                } finally {
                                    if (ftpClient.isConnected()) {
                                        try {
                                            ftpClient.logout();
                                            ftpClient.disconnect();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                            return false;
                        }

                        @Override
                        protected void onPostExecute(Boolean success) {
                            // 在这里处理上传结果
                            if (success) {
                                // 上传成功
                            } else {
                                // 上传失败
                            }
                        }
                    }


                    @Override
                    public void onProcessTips(int i) {
                        showPrecessTips(i);
                    }

                    //坐标框和对应的 搜索匹配到的图片标签
                    //人脸检测成功后画白框，此时还没有标签字段Label 字段为空
                    //人脸搜索匹配成功后白框变绿框，并标记出对应的Label
                    //部分设备会有左右图像翻转问题
                    @Override
                    public void onFaceMatched(List<RectLabel> rectLabels) {
//                        binding.graphicOverlay.drawRect(rectLabels, cameraXFragment);

//                        if(!rectLabels.isEmpty()) {
//                            binding.searchTips.setText("");
//                        }
                    }

                    @Override
                    public void onLog(String log) {
                        binding.tips.setText(log);
                    }

                }).create();


        //3.初始化引擎
        FaceSearchEngine.Companion.getInstance().initSearchParams(faceProcessBuilder);




        // 4.简单的单张图片搜索，不用摄像头的形式
        // 需要注释掉这行代码 FaceSearchEngine.Companion.getInstance().runSearch(imageProxy, 0);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                //这行代码演示 传入单张图片进行人脸搜索
//                FaceSearchEngine.Companion.getInstance().runSearch("your bitmap here");
            }
        },3000);



    }


    /**
     * 显示提示
     *
     * @param code
     */
    private void showPrecessTips(int code) {
        binding.image.setImageResource(R.mipmap.ic_launcher);
        switch (code) {
            default:
                binding.searchTips1.setText("提示码："+code);
                break;

            case THRESHOLD_ERROR :
                binding.searchTips1.setText("识别阈值Threshold范围为0.75-0.95");
                break;

            case MASK_DETECTION:
                binding.searchTips1.setText("请摘下口罩");
                break;

            case NO_LIVE_FACE:
                binding.searchTips1.setText("未检测到人脸");
                binding.mima.setVisibility(View.VISIBLE);
                binding.tips.setText("");

                break;

            case EMGINE_INITING:
                binding.searchTips1.setText("初始化中");
                break;

            case FACE_DIR_EMPTY:
                binding.searchTips1.setText("人脸库为空");
                break;

            case NO_MATCHED:
                //本次摄像头预览帧无匹配而已，会快速取下一帧进行分析检索
                binding.searchTips1.setText("无匹配");
                binding.tips.setText("暂无匹配，请先录入人脸");

                break;
        }
    }

    private MediaPlayer mediaPlayer;
    //初始化MediaPlayer
    private void initMediaPlayer(){
        mediaPlayer = MediaPlayer.create(this,R.raw.verify_success);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            //设置音频播放完毕后的回调函数
            @Override
            public void onCompletion(MediaPlayer mp) {
//                releaseMediaPlayer();
            }
        });
    }

    //开始播放音乐
    private void startPlay(){
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
    }

    //暂停播放音乐
    private void pausePlay(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    //释放音频资源
    private void releaseMediaPlayer(){
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }




}