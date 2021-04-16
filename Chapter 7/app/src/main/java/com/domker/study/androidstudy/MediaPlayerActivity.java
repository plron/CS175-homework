package com.domker.study.androidstudy;

import android.annotation.SuppressLint;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.SeekBar;

import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MediaPlayerActivity extends AppCompatActivity {
    private SurfaceView surfaceView;
    private MediaPlayer player;
    private SurfaceHolder holder;
    private SeekBar seekbar;
    private Timer mTimer;
    private TimerTask mTimerTask;
    private boolean isChanging = false;
    private boolean iffirst = false;
    private TextView  video_now_time, video_total_time;
    public static final int UPDATE_UI = 1;

    // 设置定时器
    private Timer timer = null;
    private final static int WHAT = 0;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case WHAT:
                    if (player != null) {
                        int currentPlayer = player.getCurrentPosition();
                        if (currentPlayer > 0) {
                            player.getCurrentPosition();
                            video_now_time.setText(formatTime(currentPlayer));


                        } else {
                            video_now_time.setText("00:00");
                            seekbar.setProgress(0);
                        }
                    }

                    break;

                default:
                    break;
            }
        };
    };
    private String formatTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
        return formatter.format(new Date(time));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("MediaPlayer");

        setContentView(R.layout.layout_media_player);
        surfaceView = findViewById(R.id.surfaceView);
        seekbar = findViewById(R.id.seekBar);
        seekbar.setOnSeekBarChangeListener(new MySeekbar());
        video_now_time = findViewById(R.id.textView_playtime);
        video_total_time = findViewById(R.id.textView_duration);


        player = new MediaPlayer();
        try {
            player.setDataSource(getResources().openRawResourceFd(R.raw.big_buck_bunny));
            holder = surfaceView.getHolder();
            holder.setFormat(PixelFormat.TRANSPARENT);
            holder.addCallback(new PlayerCallBack());
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 自动播放
                    player.start();
                    player.setLooping(true);
                }
            });
            player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
                @Override
                public void onBufferingUpdate(MediaPlayer mp, int percent) {
                    System.out.println(percent);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        findViewById(R.id.buttonPlay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.start();
            }
        });

        findViewById(R.id.buttonPause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player.pause();
            }
        });

        findViewById(R.id.buttonReplay).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //player.prepare();
                player.seekTo(0);
                player.start();
            }
        });
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                // 初始化前准备功能，表示准备完成，设置总的时长，使用时间格式化工具

                video_total_time.setText(formatTime(player.getDuration()));
                // 初始化定时器
                timer = new Timer();
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        handler.sendEmptyMessage(WHAT);
                    }
                }, 0, 1000);
            }
        });


        seekbar.setMax(player.getDuration());
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isChanging == true) {
                    return;
                }
                seekbar.setProgress(player.getCurrentPosition());
            }
        };
        mTimer.schedule(mTimerTask, 0, 10);
        iffirst = true;
    }

    //进度条处理
    class MySeekbar implements OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekbar.getProgress());
            isChanging = false;
        }

    }



    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.stop();
            player.release();
        }
    }


        private class PlayerCallBack implements SurfaceHolder.Callback {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        }
    }
