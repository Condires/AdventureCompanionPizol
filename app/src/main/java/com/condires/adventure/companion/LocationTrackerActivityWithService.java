package com.condires.adventure.companion;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.condires.adventure.companion.audio.BackGroundMusic;
import com.condires.adventure.companion.model.Story;


public class LocationTrackerActivityWithService extends AppCompatActivity  {

    private String TAG = this.getClass().getSimpleName();
    private final int LOCATION_PERMISSION = 1001;

    private TextView locationTv;
    // TODO: umstellen auf ausgewählte Anlage
    private Story story = null;
    private String storyName = "";

    private SeekBar mSeekbarAudio;
    private boolean mUserIsSeeking = false;

    private BroadcastReceiver broadcastReceiver;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_tracker);
        locationTv = findViewById(R.id.location);
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            story = (Story)getIntent().getSerializableExtra("Story"); //Obtaining data
        }


        storyName = story.getName();


        // Daten vom Background Service empfangen
        registerReceiver();

        initializeSeekbar();

        checkPermission();

    }

    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , LOCATION_PERMISSION);
            return;
        }
        Intent mLocationTrackerIntent = new Intent(LocationTrackerActivityWithService.this, BackGroundMusic.class);
        mLocationTrackerIntent.putExtra("StoryId", 0);
        startService(new Intent(LocationTrackerActivityWithService.this, BackGroundMusic.class));


    }

    private void displayLocation(String uiContent) {
        locationTv.setText(uiContent);
    }


    /*
     * Step 2: Register the broadcast Receiver in the activity
     * Hier sollten wir Daten für UI- Updates empfangen
     * */
    private void registerReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String uiData = intent.getStringExtra("LocationTrackerUI");
                Log.d(TAG, "onReceive: "+ uiData);
                /*
                 * Step 3: We can update the UI of the activity here
                 * */
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("LocationTrackerActivity"));
    }



    private void initializeSeekbar() {
        mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);

        mSeekbarAudio.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        //TODO: müsste via Broadcast Message gemacht werden
                        //mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }


    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            mSeekbarAudio.setMax(duration);
            //ps.setEreignisEnde(System.currentTimeMillis() + duration);
            //Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                mSeekbarAudio.setProgress(position, true);
                //Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
            }
        }

        @Override
        public void onStateChanged(@State int state) {
            String stateToString = PlaybackInfoListener.convertStateToString(state);
            onLogUpdated(String.format("onStateChanged(%s)", stateToString));
        }

        @Override
        public void onPlaybackCompleted() {
        }

        @Override
        public void onLogUpdated(String message) {
            /*
            if (mTextDebug != null) {
                mTextDebug.append(message);
                mTextDebug.append("\n");
                // Moves the scrollContainer focus to the end.
                mScrollContainer.post(
                        new Runnable() {
                            @Override
                            public void run() {
                                mScrollContainer.fullScroll(ScrollView.FOCUS_DOWN);
                            }
                        });
            }
            */
        }
    }





   @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        //mPlayerAdapter.release();
        stopService(new Intent(LocationTrackerActivityWithService.this, BackGroundMusic.class));

    }
    @Override
    protected void onStop() {
        super.onStop();

        //stopService(new Intent(LocationTrackerActivity.this, BackGroundMusic.class));


        /*
         * Step 4: Ensure to unregister the receiver when the activity is destroyed so that
         * you don't face any memory leak issues in the app
         */
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

}
