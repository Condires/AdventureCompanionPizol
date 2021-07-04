package com.condires.adventure.companion.bluetooth;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.condires.adventure.companion.Data;
import com.condires.adventure.companion.R;
import com.condires.adventure.companion.audio.CompanionAudioService;
import com.condires.adventure.companion.logger.LogService;

import java.util.List;
/*
 * Diese Aktivität wird vom Controller benutzt um Funktionen au dem Companiln auszuführen.
 * Das sind befehle, die via SMS an den Controller geschickt werden.
 * Nach der Ausführung des Befehls wird die Aktivität geschlossen
 */
public class RemoteActivity extends AppCompatActivity {
    private static Data data;
    private static List mAnlagen;
    private static CompanionAudioService mCompanionAudioService;
    private String TAG = RemoteActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mCompanionAudioService = CompanionAudioService.getInstance();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            String cmd = (String) getIntent().getStringExtra("Command"); //Obtaining data
            String name  = (String) getIntent().getStringExtra("Name"); //Obtaining data
            if (data == null) {
                data = Data.getInstance();
            }

            switch (cmd) {
                case "LoadFromAirtable":
                    loadAnlagefromAirtable(name);
                    break;
                case "SaveToAirtable":
                    saveAnlageToAirtable(name);
                    break;
                case "LoadFromDB":
                    loadAnlageFromDb(name);
                    break;
                case "SaveToDB":
                    saveAnlageToDb(name);
                    break;
                case "FLIPVOL":
                    if (mCompanionAudioService.isSilent) {
                        mCompanionAudioService.setLoud();
                        LogService.getInstance(this).writeLog(TAG, 4, "Intent Volume Loud: " + CompanionAudioService.getInstance().getVolume());
                    } else {
                        mCompanionAudioService.setSilent();
                        LogService.getInstance(this).writeLog(TAG, 4, "Intent Volume Silent: " + CompanionAudioService.getInstance().getVolume());
                    }
                    break;

                default:
            }
            RemoteActivity.this.finish();
        }
    }

    public void loadAnlagefromAirtable(String name) {
        Toast.makeText(getApplicationContext(), " Load "+name +" from Airtable called", Toast.LENGTH_SHORT).show();
        data.loadAnlageFromAirtable(name);
        Toast.makeText(getApplicationContext(), " Load "+name +" from Airtable done", Toast.LENGTH_SHORT).show();

    }

    public void saveAnlageToAirtable(String name) {
        Toast.makeText(getApplicationContext(), " Save "+name +" to Airtable called", Toast.LENGTH_SHORT).show();
        data.saveAnlageToAirtable(name);
        Toast.makeText(getApplicationContext(), " Save "+name +" to Airtable done", Toast.LENGTH_SHORT).show();


    }

    public void loadAnlageFromDb(String name) {
        Toast.makeText(getApplicationContext(), " Load "+name +" to Database called", Toast.LENGTH_SHORT).show();
        data.loadAnlageFromDb(name);
        Toast.makeText(getApplicationContext(), " Load "+name +" to Database done", Toast.LENGTH_SHORT).show();

    }

    public void saveAnlageToDb(String name) {
        Toast.makeText(getApplicationContext(), " Save "+name +" from Database called", Toast.LENGTH_SHORT).show();
        data.saveAnlageToDb(name);
        Toast.makeText(getApplicationContext(), " Save "+name +" to Database done", Toast.LENGTH_SHORT).show();


    }
}
