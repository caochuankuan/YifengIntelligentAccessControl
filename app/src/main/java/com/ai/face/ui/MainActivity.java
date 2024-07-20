package com.ai.face.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.text.LineBreaker;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.yifeng.face.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String n, x ,sign;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        SharedPreferences sp = getSharedPreferences("yifeng", MODE_PRIVATE);
        SharedPreferences sp1 = getSharedPreferences("users", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        if (
                sp.getString("xingming",n) != null
                        && sp.getString("xuehao",x) !=null
                        && Objects.equals(sp.getString("sign", sign), "1")
                        && Objects.equals(sp.getString("user", ""), "1")
        ) {
            TextView user =findViewById(R.id.user);
            user.setText("欢迎回来，"+sp.getString("zhengzhi",n));
        }
        if (
                sp.getString("xingming",n) != null
                        && sp.getString("xuehao",x) !=null
                        && Objects.equals(sp.getString("sign", sign), "1")
                        && Objects.equals(sp.getString("user", ""), "0")
                        && Objects.equals(sp1.getString("password",""),"0213")
                        && Objects.equals(sp1.getString("sign",""),"1")
        ) {
            TextView user =findViewById(R.id.user);
            user.setText("欢迎回来，"+sp.getString("zhengzhi",n));
        }

        Button bt = findViewById(R.id.center);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
            }
        });


        // 在onCreate方法中获取SharedPreferences
        SharedPreferences sp2 = getSharedPreferences("privacy", MODE_PRIVATE);
        String privacy_sign = sp2.getString("privacy_sign", "");
        SharedPreferences.Editor editor = sp2.edit();

        if (privacy_sign.isEmpty()) {
            // 如果密码不存在，写入默认密码
            editor.putString("privacy_sign", "0"); // 这里是默认密码
            editor.apply();
            privacy_sign = "0"; // 将 correctPassword 设置为默认密码
        }

        privacy(privacy_sign);



        TextView privacy1 = findViewById(R.id.privacy);
        String finalPrivacy_sign = "0";
        privacy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privacy(finalPrivacy_sign);
            }
        });


    }

    private void privacy(String privacy_sign){
        if (privacy_sign.equals("0")){

            // 检查网络连接状态
            if (!isNetworkAvailable(this)) {
                showToast("无网络连接，请检查网络后重试", "温馨提示：无网络，请联网再试。","");
                return;
            }

            OkHttpClient client = new OkHttpClient();
            String url = "http://chuankuan.com.cn/app.html";

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // 处理请求失败的情况
                    showToast("网络请求失败","","");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        try {
                            final String responseData = response.body().string();

                            // 使用Jsoup解析HTML
                            Document document = Jsoup.parse(responseData);

                            // 获取title标签中的文本内容
                            String title = document.title();
                            Elements links1 = document.select("body");
                            for (Element link : links1) {
                                final String linkText = link.text();

                                // 使用Handler确保在主线程中运行UI更新操作
                                new Handler(Looper.getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showToast(linkText,title, responseData);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 处理请求失败的情况
                        showToast("服务器返回错误：" + response.code(),"","");
                    }
                }
            });

        }
    }

    private boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false; // 无法获取上下文，无法检查网络
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET));
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkInfo networkInfo = connectivityManager.getNetworkInfo(network);
                    if (networkInfo != null && networkInfo.isConnected()) {
                        return true;
                    }
                }
            } else {
                // 对于 API 级别 21 及以下
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }

        return false;
    }


    private void showToast(String message,String title,String all) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

//        // 创建一个具有适当格式的 TextView
//        TextView textView = new TextView(this);
//
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
////            // 使用 HtmlCompat.fromHtml 解析 HTML 格式的文本（适用于 Android N 及以上版本）
////            textView.setText(HtmlCompat.fromHtml(message, HtmlCompat.FROM_HTML_MODE_LEGACY));
////        } else {
////            // 使用 Html.fromHtml 解析 HTML 格式的文本（适用于 Android N 以下版本）
////            textView.setText(Html.fromHtml(message));
////        }
//        message = message.replaceAll("aa","\n");
//        message = ToDBC(message);
//        textView.setText(message);
//
//        textView.setPadding(60, 20, 60, 20); // 根据需要调整内边距
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            textView.setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM);//指定自动调整大小的类型。
//        }
//        textView.setTextSize(16); // 设置文本大小
//        textView.setTextColor(Color.BLACK); // 设置文本颜色
//
//        // 将 TextView 放入 ScrollView
//        ScrollView scrollView = new ScrollView(this);
//        scrollView.addView(textView);

        // 创建一个 WebView
        WebView webView = new WebView(this);

        // 配置 WebView 设置
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true); // 如果需要，启用 JavaScript

        // 设置 WebViewClient 处理 WebView 内的重定向
        webView.setWebViewClient(new WebViewClient());


        // 在 WebView 中加载 HTML 内容
        webView.loadDataWithBaseURL(null, all, "text/html", "utf-8", null);

        // 将 WebView 放入 ScrollView（可选）
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(webView);



        // 设置自定义视图
        builder.setView(scrollView);

        if (isNetworkAvailable(this)){
            builder.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 用户同意隐私政策的处理逻辑
                    // 在这里保存用户同意状态，以便在其他地方检查用户是否同意了隐私政策
                    TextView privacy = findViewById(R.id.privacy);
                    privacy.setText("(已同意)隐私政策");
                    SharedPreferences sp3 = getSharedPreferences("privacy",MODE_PRIVATE);
                    sp3.edit().putString("privacy_sign","1").apply();
                }
            });
            builder.setNegativeButton("拒绝", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // 用户拒绝隐私政策的处理逻辑
                    // 在这里采取相应的操作，如关闭应用或提醒用户必须同意隐私政策才能使用应用等
                    SharedPreferences sp3 = getSharedPreferences("privacy",MODE_PRIVATE);
                    sp3.edit().putString("privacy_sign","0").apply();
                    TextView privacy = findViewById(R.id.privacy);
                    privacy.setText("隐私政策");
                    finish();
                }
            });
        }

        // 显示对话框
        builder.create().show();
    }


    /**
     * 半角转换为全角
     *
     * @param input
     * @return
     */
    public static String ToDBC(String input) {
        char[] c = input.toCharArray();
        for (int i = 0; i < c.length; i++) {
            if (c[i] == 12288) {
                c[i] = (char) 32;
                continue;
            }
            if (c[i] > 65280 && c[i] < 65375)
                c[i] = (char) (c[i] - 65248);
        }
        return new String(c);
    }

}