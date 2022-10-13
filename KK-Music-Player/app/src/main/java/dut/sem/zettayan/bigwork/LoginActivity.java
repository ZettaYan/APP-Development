package dut.sem.zettayan.bigwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences sp;

    private EditText et_userName;
    private EditText et_userPassword;
    private CheckBox cb_rememberPassword;
    private CheckBox cb_autologin;
    private Button bt_login;
    private Button bt_register;
    private TextView tv_prompt;

    private MyBroadcast myBroadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 获取首选项 SP
        // config是随便取的名字，Context.MODE_PRIVATE是最常规的模式（每次都更新）
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);

        // 初始化
        initView();
        // 回显数据：再次打开时候，从SP获取数据，进行画面的同步
        reshow();
    }

    // 初始化
    private void initView() {
        // 找到控件
        et_userName = findViewById(R.id.et_userName);
        et_userPassword = findViewById(R.id.et_userPassword);
        cb_rememberPassword = findViewById(R.id.cb_rememberPassword);
        cb_autologin = findViewById(R.id.cb_autoLogin);
        bt_register = findViewById(R.id.bt_register);
        bt_login = findViewById(R.id.bt_login);
        tv_prompt = findViewById(R.id.tv_prompt);

        // 设置监听
        MyOnClickListener l = new MyOnClickListener();
        bt_login.setOnClickListener(l);
        bt_register.setOnClickListener(l);

        // 注册广播
        myBroadcast = new MyBroadcast();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("dut.sem.zettayan.bigwork.broadcast");
        LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcast, intentFilter);
    }

    // 回显数据
    private void reshow() {
        boolean rememberPassword = sp.getBoolean("rememberPassword", false); // 如果获取是空，就会返回默认值
        boolean autologin = sp.getBoolean("autoLogin", false); // 如果获取是空，就会返回默认值
        // 如果之前勾选了"记住密码"
        if (rememberPassword) {
            // 获取SP里面的name和password，并保存到EditText里面
            String name = sp.getString("name", "");
            String password = sp.getString("password", "");
            et_userName.setText(name);
            et_userPassword.setText(password);
            cb_rememberPassword.setChecked(true); // "记住密码"打上勾
        }
        // 如果之前勾选了"自动登录"
        if (autologin){
            cb_autologin.setChecked(true); // "自动登录"打上勾
            // 模拟自动登录
            Toast.makeText(this, "自动登录了", Toast.LENGTH_SHORT).show();
            // 自动转跳到第二个界面(OperateActivity)
            openOperateActivityByAuto();
        }
    }

    // 按钮点击监听器
    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                // 注册按钮
                case (R.id.bt_register):
                    // 注册操作
                    String name = et_userName.getText().toString().trim();
                    String password = et_userPassword.getText().toString().trim();
                    if (TextUtils.isEmpty(name) || TextUtils.isEmpty(password)){
                        Toast.makeText(LoginActivity.this, "用户名或密码为空", Toast.LENGTH_SHORT).show();
                    } else {
                        // 检查"记住密码"是否打勾
                        if (cb_rememberPassword.isChecked()){
                            // "用户名"和"密码"都需要保存，同时"记住密码"的状态也要保存
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", name);
                            editor.putString("password", password);
                            editor.putBoolean("rememberPassword", true);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("name", name);
                            editor.putString("password", password);
                            editor.putBoolean("rememberPassword", false);
                            editor.apply();
                        }
                        // 检查"自动登录"是否打勾
                        if (cb_autologin.isChecked()){
                            // "自动登录"的状态要保存
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("autoLogin", true);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("autoLogin", false);
                            editor.apply();
                        }
                        // 提示用户注册成功
                        Toast.makeText(LoginActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                // 登录按钮
                case (R.id.bt_login):
                    // 登录操作
                    String loginName = et_userName.getText().toString().trim();
                    String loginPassword = et_userPassword.getText().toString().trim();
                    if (loginName.equals(sp.getString("name", null)) && loginPassword.equals(sp.getString("password", null))){
                        // 提示用户登录成功
                        Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                        // 检查"记住密码"是否打勾
                        if (cb_rememberPassword.isChecked()){
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("rememberPassword", true);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("rememberPassword", false);
                            editor.apply();
                        }
                        // 检查"自动登录"是否打勾
                        if (cb_autologin.isChecked()){
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("autoLogin", true);
                            editor.apply();
                        } else {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putBoolean("autoLogin", false);
                            editor.apply();
                        }
                        // 转跳到第二个界面(OperateActivity)
                        openOperateActivityByLogin();
                    } else {
                        // 提示用户用户名或密码不正确
                        Toast.makeText(LoginActivity.this, "用户名或密码不正确", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }

    // 手动"登录"情况下的界面跳转及参数传递
    private void openOperateActivityByLogin() {
        // 从sp中调取用户信息数据,并打包放入Bundle数据类型中
        Bundle bd = new Bundle();
        bd.putString("name", et_userName.getText().toString().trim());
        bd.putString("password", et_userPassword.getText().toString().trim());
        bd.putBoolean("rememberPassword", cb_rememberPassword.isChecked());
        bd.putBoolean("autoLogin", cb_autologin.isChecked());
        // 定义一个意图,将Bundle类型中的数据传给这个意图,并实现页面的跳转
        Intent intent = new Intent();
        intent.putExtras(bd);
        intent.setClass(LoginActivity.this, OperateActivity.class);
        startActivity(intent);
    }

    // "自动登录"情况下的界面跳转及参数传递
    private void openOperateActivityByAuto() {
        // 从sp中调取用户信息数据,并打包放入Bundle数据类型中
        Bundle bd = new Bundle();
        bd.putString("name", sp.getString("name", ""));
        bd.putString("password", sp.getString("password", ""));
        bd.putBoolean("rememberPassword", sp.getBoolean("rememberPassword", false));
        bd.putBoolean("autoLogin", sp.getBoolean("autoLogin", false));
        // 定义一个意图,将Bundle类型中的数据传给这个意图,并实现页面的跳转
        Intent intent = new Intent();
        intent.putExtras(bd);
        intent.setClass(LoginActivity.this, OperateActivity.class);
        startActivity(intent);
    }


    // 注销登录状态的广播接收
    private class MyBroadcast extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case ("dut.sem.zettayan.bigwork.broadcast"):
                    tv_prompt.setText("已注销登录，请重新登录");
                    break;
            }
        }
    }

    // Activity销毁时取消注册广播
    @Override
    protected void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myBroadcast);
    }
}