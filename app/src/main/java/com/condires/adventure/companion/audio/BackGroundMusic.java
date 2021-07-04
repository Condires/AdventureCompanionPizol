package com.condires.adventure.companion.audio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


public class BackGroundMusic extends Service {


    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    Context context;
    int volume;
    private String TAG = this.getClass().getSimpleName();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Bundle extras = intent.getExtras();
        context=getBaseContext();//Get the context here
        int mediaId = 0;
        if (extras != null) {
           mediaId = (int) intent.getSerializableExtra("MediaId"); //Obtaining data
        }
        mediaPlayer = MediaPlayer.create(this, mediaId);
        Log.d(TAG, "onStartCommand: ");
        sendGPSDataToActivity("Hello world");
        mediaPlayer.start();


        return super.onStartCommand(intent, flags, startId);
    }



    @Override
    public boolean stopService(Intent name) {

        return super.stopService(name);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;

    }

    private void sendGPSDataToActivity(String newData){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("LocationTrackerActivity");
        broadcastIntent.putExtra("LocationTrackerUI", newData);
        sendBroadcast(broadcastIntent);

    }
}