package dut.sem.zettayan.bigwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

public class MusicActivity extends AppCompatActivity {

    private ImageView iv_disk; //光盘（设置动画）
    private static SeekBar musicProgressBar; //进度条
    private static TextView currentTv; //当前音乐播放时长
    private static TextView totalTv; //当前音乐总时长
    private TextView tv_music_playing; //当前音乐名
    private Button btn_play, btn_pause, btn_continue, btn_exit; //四个控制按钮

    private ObjectAnimator animator;

    private MusicPlayerService.MusicControl control;

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            control = (MusicPlayerService.MusicControl) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        // 初始化
        init();
    }

    // 初始化方法
    public void init() {
        // 找到控件
        iv_disk = findViewById(R.id.iv_music);
        musicProgressBar = findViewById(R.id.sb);
        currentTv = findViewById(R.id.tv_progress);
        totalTv = findViewById(R.id.tv_total);
        tv_music_playing = findViewById(R.id.tv_music_playing);
        btn_play = findViewById(R.id.btn_play);
        btn_pause = findViewById(R.id.btn_pause);
        btn_continue = findViewById(R.id.btn_continue);
        btn_exit = findViewById(R.id.btn_exit);

        // 设置监听
        MyOnClickListener l = new MyOnClickListener();
        btn_play.setOnClickListener(l);
        btn_pause.setOnClickListener(l);
        btn_continue.setOnClickListener(l);
        btn_exit.setOnClickListener(l);

        // 设置转盘旋转
        animator = ObjectAnimator.ofFloat(iv_disk, "rotation", 0, 360.F);//对象是iv_disk，动作是rotation旋转，角度从0到360度，这里用的是浮点数，所以要加个F
        animator.setDuration(10000); //这是设置动画的时长，单位为毫秒，这里设置了10秒转一圈
        animator.setInterpolator(new LinearInterpolator()); //旋转时间函数为线性，意为匀速旋转
        animator.setRepeatCount(-1); //设置转动圈数，-1为一直转动

        // 接收从OperateActivity传递过来的音乐信息
        Intent getIntent = getIntent();
        String selectedMusic = getIntent.getStringExtra("selected_music");

        // 设置当前曲目名字
        tv_music_playing.setText(selectedMusic);

        // 绑定Service,并向传递需要播放的音乐的信息
        Intent intent = new Intent(getApplicationContext(), MusicPlayerService.class);
        intent.putExtra("selected_music", selectedMusic);
        bindService(intent, conn, BIND_AUTO_CREATE);

        // 设置进度条监听事件
        musicProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //当音乐停止后，停止光盘动画
                if (progress == seekBar.getMax()) {
                    animator.pause();
                }
                if (fromUser) { // 判断是不是用户拖动的
                    control.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                control.pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                control.resume();
            }
        });
    }

    private class MyOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case (R.id.btn_play):
                    // 播放音乐
                    control.play();
                    // 光盘开始转动
                    animator.start();
                    break;
                case (R.id.btn_pause):
                    //暂停音乐
                    control.pause();
                    // 光盘暂停
                    animator.pause();
                    break;
                case (R.id.btn_continue):
                    //继续播放
                    control.resume();
                    // 光盘继续转动
                    animator.resume();
                    break;
                case (R.id.btn_exit):
                    //退出应用
                    finish();
                    break;
            }
        }
    }

    // 当前Activity销毁时暂停播放并解绑服务
    @Override
    protected void onDestroy() {
        control.stop();
        unbindService(conn);
        super.onDestroy();
    }

    // 获取从MusicPlayerService传递过来的消息
    public static Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData(); //获取从子程发送过来的音乐播放进度
            int duration = bundle.getInt("duration"); //歌曲的总时长
            int currentPosition = bundle.getInt("currentPosition"); //歌曲当前进度
            musicProgressBar.setMax(duration); //设置Seekbar的最大值为歌曲总时长
            musicProgressBar.setProgress(currentPosition); //设置 Seekbar当前的进度位置
            String totalTime = msToMinSec(duration); //歌曲总时长
            String currentTime = msToMinSec(currentPosition); //歌曲当前播放时长
            totalTv.setText(totalTime);
            currentTv.setText(currentTime);
        }
    };

    // 时间转换方法
    public static String msToMinSec(int ms) {
        int sec = ms / 1000;
        int min = sec / 60;
        sec -= min * 60;
        return String.format("%02d:%02d", min, sec);
    }

}