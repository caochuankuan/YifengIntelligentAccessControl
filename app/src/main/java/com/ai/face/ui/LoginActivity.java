package com.ai.face.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import com.yifeng.face.R;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText ed1,ed2;
    RadioGroup rg;
    RadioButton rb1,rb2,rb3;
    Spinner sp1;
    Button bt;
    String n,x,sign,user,pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 设置沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        ed1 = findViewById(R.id.xingming);
        ed2 = findViewById(R.id.xuehao);
        rg = findViewById(R.id.zhengzhig);
        sp1 = findViewById(R.id.yuanxi);
        bt = findViewById(R.id.login);

        final SharedPreferences sp11 = getSharedPreferences("yifeng",MODE_PRIVATE);
        final SharedPreferences sp = getSharedPreferences("users",MODE_PRIVATE);
        final SharedPreferences.Editor ed11 = sp11.edit();
        if (
                sp11.getString("xingming",n) != null
                && sp11.getString("xuehao",x) !=null
                && Objects.equals(sp11.getString("sign", sign), "1")
                && Objects.equals(sp11.getString("user", user), "0")
                && Objects.equals(sp.getString("password",pass),"0213")
                && Objects.equals(sp.getString("sign",""),"1")
        ) {
            Intent intent = new Intent(LoginActivity.this, com.ai.face.search.SearchNaviActivity.class);
            startActivity(intent);
            finish();
        }else if (
                sp11.getString("xingming",n) != null
                        && sp11.getString("xuehao",x) !=null
                        && Objects.equals(sp11.getString("sign", sign), "1")
                        && Objects.equals(sp11.getString("user", user), "0")
                        && Objects.equals(sp.getString("password",pass),"0213")
                        && Objects.equals(sp.getString("sign",""),"0")
        ){
            Intent intent = new Intent(LoginActivity.this, com.ai.face.verify.PasswordValidator.class);
            startActivity(intent);
            finish();
        } else if (sp11.getString("xingming",n) != null && sp11.getString("xuehao",x) !=null
                && Objects.equals(sp11.getString("sign", sign), "1")  && Objects.equals(sp11.getString("user", user), "1")){
            Intent it = new Intent(LoginActivity.this,com.ai.face.search.SearchNaviActivity1.class);
            startActivity(it);
            finish();
        }

        CheckBox rememberLoginCheckbox = findViewById(R.id.rememberLoginCheckbox);

        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean rememberLogin = rememberLoginCheckbox.isChecked();
                String st1 = ed1.getText().toString();
                String st2 = ed2.getText().toString();
                int rgc = rg.getCheckedRadioButtonId();
                rb1 = findViewById(rgc);
                String st3 = "";
                if (rb1 != null){
                    st3 = rb1.getText().toString();
                }
                String st4 = sp1.getSelectedItem().toString();

                if (st1.equals("")) {
                    Toast.makeText(LoginActivity.this, "请填写小区名再提交", Toast.LENGTH_SHORT).show();
                } else if (!st1.matches("^[\u4e00-\u9fa5]{2,10}$")) {
                    Toast.makeText(LoginActivity.this, "小区名必须2-10个中文汉字", Toast.LENGTH_SHORT).show();
                } else if (st2.equals("")) {
                    Toast.makeText(LoginActivity.this, "请输入编号再提交", Toast.LENGTH_SHORT).show();
                } else if (st2.length() != 6 || !st2.matches("\\d+")) {
                    Toast.makeText(LoginActivity.this, "编号必须是6位数字", Toast.LENGTH_SHORT).show();
                } else if (st3.equals("")) {
                    Toast.makeText(LoginActivity.this, "请选择身份再提交", Toast.LENGTH_SHORT).show();
                } else if (st4.equals("请选择维护者")) {
                    Toast.makeText(LoginActivity.this, "请选择维护人再提交", Toast.LENGTH_SHORT).show();
                } else {
                    if (st3.equals("管理员")){
                        ed11.putString("user","0");
                    }else {
                        ed11.putString("user","1");
                    }
                    if (!rememberLogin){
                        String aa = st3;
                        new AlertDialog.Builder(LoginActivity.this)
                                .setTitle("温馨提示")
                                .setMessage("是否记住信息")
                                .setNeutralButton("记住", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent it = new Intent(LoginActivity.this,com.ai.face.search.SearchNaviActivity1.class);
                                        Intent it1 = new Intent(LoginActivity.this, com.ai.face.verify.PasswordValidator.class);
                                        Bundle bd = new Bundle();
                                        bd.putCharSequence("xingming",st1);
                                        bd.putCharSequence("xuehao",st2);
                                        bd.putCharSequence("zhengzhi",aa);
                                        bd.putCharSequence("yuanxi",st4);
                                        it.putExtras(bd);
                                        ed11.putString("xingming",st1);
                                        ed11.putString("xuehao",st2);
                                        ed11.putString("yuanxi",st4);
                                        ed11.putString("zhengzhi",aa);
                                        ed11.putString("sign","1");
                                        ed11.commit();
                                        Toast.makeText(LoginActivity.this, "已添加自动登录信息", Toast.LENGTH_SHORT).show();
                                        if (aa.equals("管理员")){
                                            startActivity(it1);
                                        }else {
                                            startActivity(it);
                                        }
                                        finish();
                                    }
                                })
                                .setPositiveButton("不记住", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent it = new Intent(LoginActivity.this,com.ai.face.search.SearchNaviActivity1.class);
                                        Intent it1 = new Intent(LoginActivity.this, com.ai.face.verify.PasswordValidator.class);
                                        Bundle bd = new Bundle();
                                        bd.putCharSequence("xingming",st1);
                                        bd.putCharSequence("xuehao",st2);
                                        bd.putCharSequence("zhengzhi",aa);
                                        bd.putCharSequence("yuanxi",st4);
                                        it.putExtras(bd);
                                        ed11.putString("xingming",st1);
                                        ed11.putString("xuehao",st2);
                                        ed11.putString("yuanxi",st4);
                                        ed11.putString("zhengzhi",aa);
                                        ed11.putString("sign","0");
                                        ed11.commit();
                                        if (aa.equals("管理员")){
                                            startActivity(it1);
                                        }else {
                                            startActivity(it);
                                        }
                                        finish();
                                    }
                                }).show();
                    }else {
                        Intent it = new Intent(LoginActivity.this,com.ai.face.search.SearchNaviActivity1.class);
                        Intent it1 = new Intent(LoginActivity.this, com.ai.face.verify.PasswordValidator.class);
                        Bundle bd = new Bundle();
                        bd.putCharSequence("xingming",st1);
                        bd.putCharSequence("xuehao",st2);
                        bd.putCharSequence("zhengzhi",st3);
                        bd.putCharSequence("yuanxi",st4);
                        it.putExtras(bd);
                        ed11.putString("xingming",st1);
                        ed11.putString("xuehao",st2);
                        ed11.putString("yuanxi",st4);
                        ed11.putString("zhengzhi",st3);
                        ed11.putString("sign","1");
                        ed11.commit();
                        Toast.makeText(LoginActivity.this, "已添加自动登录信息", Toast.LENGTH_SHORT).show();
                        if (st3.equals("管理员")){
                            startActivity(it1);
                        }else {
                            startActivity(it);
                        }
                        finish();
                    }
                }
            }
        });
    }
}