package dut.sem.zettayan.bigwork;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class OperateActivity extends AppCompatActivity {
    private final String BROADCAST_ID="dut.sem.zettayan.bigwork.broadcast";
    private final String CHANNEL_ID="ChannelID";
    private final int notificationId=1;

    SharedPreferences sp;
    private TextView userNameShow;
    private TextView userPasswordShow;
    private TextView rememberPassword;
    private TextView autoLogin;
    private Button bt_logout;
    private Button bt_playMusic;
    private Button bt_find_singer;
    private Spinner sp_songs;
    private TextView tv_singer;
    private final SingerExpert singerExpert = new SingerExpert();
    private Button bt_broadcast;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate);

        // 获取首选项 SP
        // config是随便取的名字，Context.MODE_PRIVATE是最常规的模式（每次都更新）
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);

        // 初始化
        initView();
    }

    private void initView() {
        // 找到控件
        userNameShow = findViewById(R.id.tv_userNameShow);
        userPasswordShow = findViewById(R.id.tv_userPasswordShow);
        rememberPassword = findViewById(R.id.tv_rememberPassword);
        autoLogin = findViewById(R.id.tv_autoLogin);
        bt_logout = findViewById(R.id.bt_logout);
        bt_playMusic = findViewById(R.id.bt_playMusic);
        bt_find_singer = findViewById(R.id.bt_find_singer);
        sp_songs = findViewById(R.id.sp_songs);
        tv_singer = findViewById(R.id.tv_singer);
        bt_broadcast = findViewById(R.id.bt_logout_account);

        // 设置监听事件
        MyOnClickListener l = new MyOnClickListener();
        bt_logout.setOnClickListener(l);
        bt_playMusic.setOnClickListener(l);
        bt_find_singer.setOnClickListener(l);
        bt_broadcast.setOnClickListener(l);

        // 接受登录界面传递过来的参数并显示在界面上
        receiveInfo();

        // 创建通知通道
        createNotificationChannel();
    }

    // 配置监听器监听按钮的点击事件
    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v){
            switch (v.getId()){
                case (R.id.bt_logout):
                    // 退出界面
                    logout();
                    // 创建并发送通知
                    sendNotice();
                    break;
                case (R.id.bt_playMusic):
                    // 跳转到第3个界面(MusicActivity)
                    openMusicActivity();
                    break;
                case (R.id.bt_find_singer):
                    String song = sp_songs.getSelectedItem().toString();
                    String singer = singerExpert.getSinger(song);
                    tv_singer.setText(singer);
                    break;
                case (R.id.bt_logout_account):
                    // 注销登录状态
                    logoutAccount();
                    break;
            }
        }
    }

    // 接收登录界面传递过来的信息并显示在屏幕上
    private void receiveInfo(){
        Intent intent = getIntent();
        Bundle bd = intent.getExtras();
        userNameShow.setText(bd.getString("name", ""));
        userPasswordShow.setText(bd.getString("password", ""));
        if (bd.getBoolean("rememberPassword", false)){
            rememberPassword.setText("是");
        } else {
            rememberPassword.setText("否");
        }
        if (bd.getBoolean("autoLogin", false)){
            autoLogin.setText("是");
        } else {
            autoLogin.setText("否");
        }
    }

    // 退出登录(结束此Activity)
    private void logout() {
        // 如果之前勾选了"自动登录"，则取消自动登录的设置,防止返回后有跳转回此页面
        boolean autologin = sp.getBoolean("autoLogin", false);
        if (autologin){
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("autoLogin", false);
            editor.apply();
        }
        // 结束此Activity
        finish();
    }

    // 跳转到第3个界面(MusicActivity)
    private void openMusicActivity() {
        Intent intent = new Intent();
        String selectedMusic = sp_songs.getSelectedItem().toString();
        intent.putExtra("selected_music", selectedMusic); // 向MusicActivity传递需要播放的音乐
        intent.setClass(this, MusicActivity.class);
        startActivity(intent);
    }

    // 注销登录状态
    private void logoutAccount() {
        // 发送注销登录状态的广播
        Intent intent = new Intent(BROADCAST_ID);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        // 提示已经注销登录状态
        Toast.makeText(this, "已注销", Toast.LENGTH_SHORT).show();

        // 如果之前勾选了"自动登录"，则取消自动登录的设置,防止返回后有跳转回此页面
        boolean autologin = sp.getBoolean("autoLogin", false);
        if (autologin){
            SharedPreferences.Editor editor = sp.edit();
            editor.putBoolean("autoLogin", false);
            editor.apply();
        }

        // 将页面中的用户信息置空
        userNameShow.setText("");
        userPasswordShow.setText("");
        rememberPassword.setText("");
        autoLogin.setText("");
    }

    // 通知通道
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // 创建并发送通知
    private void sendNotice() {
        Intent intent = new Intent(this, LoginActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("您已退出KK音乐")
                .setContentText("点击此返回登录页面")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
    }

    // 查询歌曲对应的歌手
    private static class SingerExpert {
        public String getSinger(String song){
            String result;
            switch (song){
                case "青花瓷":
                    result = "周杰伦";
                    break;
                case "江南":
                    result = "林俊杰";
                    break;
                case "泡沫":
                    result = "邓紫棋";
                    break;
                default:
                    result = "查不到这首歌的歌手";
            }
            return result;
        }
    }

}