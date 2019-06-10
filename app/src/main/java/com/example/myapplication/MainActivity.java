package com.example.myapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView playBtn;
    ImageView pauseBtn;
    ImageView stopBtn;
    ProgressBar progressBar;
    TextView titleView;

    String filePath;
    boolean runThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playBtn = (ImageView) findViewById(R.id.lab1_play);
        pauseBtn = (ImageView) findViewById(R.id.lab1_pause);
        stopBtn = (ImageView) findViewById(R.id.lab1_stop);
        progressBar = (ProgressBar) findViewById(R.id.lab1_progress);
        titleView = (TextView) findViewById(R.id.lab1_title);

        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);

        titleView.setText("music.mp3");
        pauseBtn.setEnabled(false);
        stopBtn.setEnabled(false);

        filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Music/music.mp3";


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        //Activity의 Receiver를 시스템에 등록
        registerReceiver(receiver, new IntentFilter("com.example.PLAY_TO_ACTIVITY"));
        Intent intent = new Intent(this, PlayService.class);
        intent.putExtra("filePath", filePath);
        startService(intent);

    }


    class ProgressThread extends Thread {
        private  ProgressThread instance;
        @Override
        public void run() {
            while (runThread) {
                progressBar.incrementProgressBy(1000);
                SystemClock.sleep(1000);
                if (progressBar.getProgress() == progressBar.getMax()) {
                    runThread = false;
                }
            }
        }
        public synchronized ProgressThread getInstance(){
            if(instance == null){
                instance = new ProgressThread();
            }
            return instance;
        }

    }

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mode = intent.getStringExtra("mode");
            if (mode != null) {
                if (mode.equals("start")) {
                    //최초 Service가 구동된 상태라면 duration 설정만 획득

                    int duration = intent.getIntExtra("duration", 0);
                    int current = intent.getIntExtra("current", 0);

                    if (current == 0) {
                        ProgressThread thread = new ProgressThread();
                        thread.start();
                    } else {
                        ProgressThread thread = new ProgressThread();
                        thread.start();
                    }

                    progressBar.setMax(duration);
                    progressBar.setProgress(current);
                } else if (mode.equals("stop")) {
                    //Service 쪽에서 음악 플레이가 종료된 상황이라면
                    runThread = false;
                    //화면 button 제어
                    playBtn.setEnabled(true);
                    playBtn.setVisibility(View.VISIBLE);
                    pauseBtn.setEnabled(false);
                    pauseBtn.setVisibility(View.INVISIBLE);
                    stopBtn.setEnabled(false);

                } else if (mode.equals("restart")) {
                    //activity가 다시 시작되었다면
                    //Service 쪽에서 음악을 play화고 있는 도중일 수 있어서
                    //현재 play 위치를 획득해야 progressBar의 값으로 설정할 수 있어서
                    int duration = intent.getIntExtra("duration", 0);
                    int current = intent.getIntExtra("current", 0);
                    progressBar.setMax(duration);
                    progressBar.setProgress(current);
                    //ProgressBar의 값을 증가시키는  Thread
                    runThread = true;
                    ProgressThread thread = new ProgressThread();
                    thread.start();
                    //화면 button 제어
                    playBtn.setEnabled(false);
                    stopBtn.setEnabled(true);
                } else if (mode.equals("pause")) {
                    int duration = intent.getIntExtra("duration", 0);
                    int current = intent.getIntExtra("current", 0);
                    progressBar.setMax(duration);
                    progressBar.setProgress(current);
                    //ProgressBar의 값을 증가시키는  Thread
                    runThread = false;
               /*     ProgressThread thread = new ProgressThread();
                    thread.start();*/
                    //화면 button 제어
                    playBtn.setEnabled(true);
                    stopBtn.setEnabled(false);
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public void onClick(View v) {
        if (v == playBtn) {
            Intent intent = new Intent("com.example.PLAY_TO_SERVICE");
            intent.putExtra("mode", "start");
            sendBroadcast(intent);
            runThread = true;

            /*ProgressThread thread = new ProgressThread();
            thread.start();*/
            playBtn.setEnabled(false);
            playBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setEnabled(true);
            pauseBtn.setVisibility(View.VISIBLE);
            stopBtn.setEnabled(true);
        } else if (v == stopBtn) {
            Intent intent = new Intent("com.example.PLAY_TO_SERVICE");
            intent.putExtra("mode", "stop");
            sendBroadcast(intent);
            runThread = false;
            progressBar.setProgress(0);

            playBtn.setEnabled(true);
            playBtn.setVisibility(View.VISIBLE);
            pauseBtn.setEnabled(false);
            pauseBtn.setVisibility(View.INVISIBLE);
            stopBtn.setEnabled(false);

        } else if (v == pauseBtn) {
            Intent intent = new Intent("com.example.PLAY_TO_SERVICE");
            intent.putExtra("mode", "pause");
            sendBroadcast(intent);
            //  runThread=false;
            // progressBar.setProgress(0);

            playBtn.setVisibility(View.VISIBLE);
            playBtn.setEnabled(true);

            pauseBtn.setVisibility(View.INVISIBLE);
            pauseBtn.setEnabled(false);

            stopBtn.setEnabled(true);
        }

    }
}

