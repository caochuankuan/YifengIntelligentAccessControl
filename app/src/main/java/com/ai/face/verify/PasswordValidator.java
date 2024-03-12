package com.ai.face.verify;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ai.face.search.FaceSearch1NActivity;
import com.yifeng.face.R;

public class PasswordValidator extends AppCompatActivity {

    private StringBuilder enteredPassword = new StringBuilder();
    String default_value = "";
    SharedPreferences sp;
    String correctPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_validator);

        //初始化声音
        initMediaPlayer();

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        // 在onCreate方法中获取SharedPreferences
        sp = getSharedPreferences("users", MODE_PRIVATE);
        correctPassword = sp.getString("password", default_value);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("sign","0");
        editor.apply();

        if (correctPassword.isEmpty()) {
            // 如果密码不存在，写入默认密码
            editor.putString("password", "0213"); // 这里是默认密码
            editor.apply();
            correctPassword = "0213"; // 将 correctPassword 设置为默认密码
        }

        GridLayout gridLayout = findViewById(R.id.gridLayout);

        TextView forget = findViewById(R.id.forget);
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(PasswordValidator.this).setTitle("温馨提示").setMessage("默认密码0213\n如已修改，请联系逸风智能门禁客服。\nQQ:2835082172@qq.com").show();
            }
        });

        // 设置按钮点击事件
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof Button) {
                final Button button = (Button) child;
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onButtonClick(button);
                    }
                });
            }
        }
    }

    private void onButtonClick(Button button) {
        String buttonText = button.getText().toString();

        // 更新已输入的数字显示
        TextView tvEnteredDigits = findViewById(R.id.tvEnteredDigits);

        if (buttonText.equals("清除")) {
            // 清除按钮，清空输入
            enteredPassword.setLength(0);
        } else if (buttonText.equals("删除")) {
            // 删除按钮，删除最后一个字符
            if (enteredPassword.length() > 0) {
                enteredPassword.deleteCharAt(enteredPassword.length() - 1);
            }
        } else {
            // 数字按钮，追加到输入密码
            enteredPassword.append(buttonText);
        }

        // 更新已输入的数字显示
        tvEnteredDigits.setText(enteredPassword.toString());

        // 判断密码是否输入完毕
        if (enteredPassword.length() == correctPassword.length()) {
            // 验证密码
            if (enteredPassword.toString().equals(sp.getString("password", correctPassword))) {
                tvEnteredDigits.setText("");
                // 密码正确
                Toast.makeText(this, "密码正确", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("sign","1");
                editor.apply();
                startPlay();
                startActivity(new Intent(PasswordValidator.this,com.ai.face.search.SearchNaviActivity.class));
                finish();
            } else {
                // 密码错误
                Toast.makeText(this, "密码错误，请重试", Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("sign","0");
                editor.apply();
                enteredPassword.setLength(0); // 清空输入
                tvEnteredDigits.setText("");
            }
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
