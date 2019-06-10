package com.example.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;

public class PlayService extends Service implements MediaPlayer.OnCompletionListener {
    MediaPlayer player;
    String filePath;


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String mode = intent.getStringExtra("mode");
            if (mode != null) {
                if (mode.equals("start")) {
                    try {
                        if (player != null && !player.isPlaying()) {
                            player.start();

                            Intent aIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
                            aIntent.putExtra("mode", "restart");
                            aIntent.putExtra("duration", player.getDuration());
                            aIntent.putExtra("current", player.getCurrentPosition());
                            sendBroadcast(aIntent);
                            /*player.release();
                            player=null;*/
                        } else {
                            //음악 paly를 위한 MediaPlayer 준비
                            player = new MediaPlayer();
                            player.setDataSource(filePath);
                            player.prepare();
                            player.start();
                            //Activity에 duration 전달
                            Intent aIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
                            aIntent.putExtra("mode", "start");
                            aIntent.putExtra("duration", player.getDuration());
                            sendBroadcast(aIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (mode.equals("stop")) {
                    if (player != null && player.isPlaying()) {
                        player.stop();
                        player.release();
                        player = null;
                    }
                } else if (mode.equals("pause")){
                    if(player != null && player.isPlaying()){
                        player.pause();
                        Intent aIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
                        aIntent.putExtra("mode", "pause");
                        aIntent.putExtra("duration", player.getDuration());
                        aIntent.putExtra("current", player.getCurrentPosition());
                        sendBroadcast(aIntent);
                    }
                }
            }
        }
    };

    @Override
    public void onCompletion(MediaPlayer mp) {
        Intent intent = new Intent("com.example.PLAY_TO_ACTIVITY");
        intent.putExtra("mode", "stop");
        sendBroadcast(intent);
        //Service 자신을 종료시킨다.
        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(receiver, new IntentFilter("com.example.PLAY_TO_SERVICE"));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        filePath = intent.getStringExtra("filePath");
        //이미 Service가 이전에 구동되어 음악이 플에이되고 있는 상황
        if (player != null) {
            Intent aIntent = new Intent("com.example.PLAY_TO_ACTIVITY");
            aIntent.putExtra("mode", "restart");
            aIntent.putExtra("duration", player.getDuration());
            aIntent.putExtra("current", player.getCurrentPosition());
            sendBroadcast(aIntent);
        }
        return super.onStartCommand(intent, flags, startId);

    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
