/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.condires.adventure.companion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewAnimator;

import com.condires.adventure.companion.alarm.AppChecker;
import com.condires.adventure.companion.audio.CompanionAudioService;
import com.condires.adventure.companion.bluetooth.BluetoothLogger;
import com.condires.adventure.companion.gpstracker.GPSTrackService;
import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.logger.TopExceptionHandler;
import com.condires.adventure.companion.logwrapper.Log;
import com.condires.adventure.companion.logwrapper.LogFragment;
import com.condires.adventure.companion.logwrapper.LogWrapper;
import com.condires.adventure.companion.logwrapper.MessageOnlyLogFilter;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Radius;
import com.condires.adventure.companion.model.Story;
import com.condires.adventure.companion.model.Weg;
import com.condires.adventure.companion.recorder.AdventureRecorderActivity;
import com.condires.adventure.companion.setting.ACSettings;
import com.condires.adventure.companion.setting.SettingsPrefActivity;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Allows playback of a single MP3 file via the UI. It contains a {@link MediaPlayerHolder}
 * which implements the {@link PlayerAdapter} interface that the activity uses to control
 * audio playback.
 */
public final class MainActivity extends AppCompatActivity implements OnItemSelectedListener {
    // TODO: der Start Button wird automatisch gedrückt, Story ändern heisst zurück und wieder Start drücken
    // beim Start soll ein Status angezeigt werden, der sagt, wenn das Gerät bereit ist, bzw GPS Empfang hat
    // TODO: bildschirm anschalten wenn wir im Produktiven Mode sind
    // TODO einen Button Entwicklermode machen
    // TODO stoppen wenn die Gondel auf dem Weg stehen bleibt

    private String TAG = MainActivity.class.getSimpleName();

    //public static final int MEDIA_RES_ID = R.raw.musik_talstation;
    static final int START_TRACKER_REQUEST = 1;

    private static boolean firstStart = true;
    // Whether the Log Fragment is currently shown
    private boolean  mLogShown;

    private TextView mTextDebug;
    private SeekBar  mSeekbarAudio;
    private static CompanionAudioService mCompanionAudioService;
    private ScrollView mScrollContainer;
    //private PlayerAdapter mPlayerAdapter;
    //private LocationTracker mLocationTracker;
    private boolean mUserIsSeeking = false;
    private Story   story;
    private static Context myContext = null;
    public static Data     data;
    public static Anlage   mAnlage;
    public static int      mAnlageIndex = 0;    // welche mAnlage soll benutzt werden
    ArrayAdapter<Anlage> mAnlageListAdapter;
    public String   testOn = "0";   // test aus
    public boolean darkScreen = true;

    private BroadcastReceiver mRadiusReceiver;
    //private BroadcastReceiver mFlicReceiver;

    protected PowerManager.WakeLock mWakeLock;
    public static MainActivity instance;



    public static Context getContext() {
        return myContext;
    }
    public static CompanionAudioService getAudioService() {
        return mCompanionAudioService;
    }
    public static MainActivity getInstance() {return instance;}
    public static int getCurrentAnlageIndex() { return mAnlageIndex; }
    Intent mLocationTrackerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler(this));

        myContext = this;
        instance = this;
        // die Anlagenamen werden geladen entweder von der db oder vom code

        if (data == null) {
            data = new Data(myContext);
            Log.d(TAG,"Data ist geladen");
        }
        // Audioservice muss gesetzt sein, bevor der Logger instanziert wird, weil der Logger den Audioservice nutzen kann
        mCompanionAudioService = CompanionAudioService.getInstance(this);
        BluetoothLogger bluetoothLogger = BluetoothLogger.getInstance(this);
        //bluetoothLogger.setActivity(this);
        bluetoothLogger.start();

        // mAnlage muss gesetzt sein

        int size = data.getAnlagen().size();
        if (size > 1) {
            mAnlageIndex = 1;
        } else {
            mAnlageIndex = 0;
        }

        mAnlage = data.getAnlagen().get(mAnlageIndex);

        //LoadFromAirtable();

        //mAnlage = data.getAnlagen().get(mAnlageIndex);

        mLocationTrackerIntent = new Intent(MainActivity.this, LocationTrackerActivity.class);


        //startControllerApp();

        setContentView(R.layout.activity_main);
        initializeUI();
        //initializeSeekbar();
        //initializePlaybackController();
        registerRadiusReceiver();
        //registerFLICReceiver();


        // Spinner element
        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(this);

        // Spinner Drop down elements
        //List<String> categories = new ArrayList<String>();
        //categories.add("Mounteens 1");
        //categories.add("Mounteens 2");
        //categories.add("Heidi");
        // TODO: Anlage Drop Down anzeigen

        // Creating adapter for spinner
        ArrayAdapter<Story> dataAdapter = new ArrayAdapter<Story>(this,
                android.R.layout.simple_spinner_item, mAnlage.getStories());

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        // anlagespinner
        Spinner aspinner = (Spinner) findViewById(R.id.anlagespinner);
        // Spinner click listener
        aspinner.setOnItemSelectedListener(this);
        // Creating adapter for spinner
        mAnlageListAdapter = new ArrayAdapter<Anlage>(this,
                android.R.layout.simple_spinner_item, data.getAnlagen());
        // Drop down layout style - list view with radio button
        mAnlageListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        aspinner.setAdapter(mAnlageListAdapter);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "myapp:mywakelocktag");
        this.mWakeLock.acquire();

        sendStackTracetoAirtable();

        boolean startOther = true;
        // nur beim ersten mal
        if (startOther) {
            if (savedInstanceState == null) {
                if (AppChecker.isBoosterRunning(this)) {
                    LogService.getInstance(this).writeLog(TAG, 4, "Volume Booster is running");
                } else {
                    LogService.getInstance(this).writeLog(TAG, 4, "Volume Booster not running");
                }

                if (AppChecker.isADCRunning(this)) {
                    LogService.getInstance(this).writeLog(TAG, 4, "AD Controller is running");
                } else {
                    LogService.getInstance(this).writeLog(TAG, 4, "AD Controller not running");
                }

                if (AppChecker.isFlicRunning(this)) {
                    LogService.getInstance(this).writeLog(TAG, 4, "Flic is running");
                } else {
                    LogService.getInstance(this).writeLog(TAG, 4, "Flic not running");
                    AppChecker.startFlic(this);
                }
            }
        }




        Log.d(TAG, "onCreate: finished");
        //automatischer Start, der aktuellen Anlage

        /* geht nicht, Back chrashed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
                startLocationTracker();
            }
        }, 2000);
        */
        //

    }

    private void sendStackTracetoAirtable() {
        FileInputStream file = null;
        boolean fileFound = true;
        try {
            file = MainActivity.this.openFileInput("stack.trace");
        } catch (FileNotFoundException e) {
            fileFound = false;
            //e.printStackTrace();
        }
        if (fileFound) {
            String line;
            String trace = "";
            LogService.getInstance(this).writeLog(TAG, 4, "Log StackTrace");

            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(file));
                while ((line = reader.readLine()) != null) {
                    trace += line + "\n";
                }
            } catch (FileNotFoundException fnfe) {
                LogService.getInstance(this).writeLog(TAG, 4, "Log StackTrace file not found");
            } catch (IOException ioe) {
                LogService.getInstance(this).writeLog(TAG, 4, "Log StackTrace Io Exception");
            }
            LogService.getInstance(this).writeStackTrace(trace);
            LogService.getInstance(this).setAirtableLogStackTraceNull();
            try {
                MainActivity.this.deleteFile("stack.trace");
            } catch (Exception e) {
                LogService.getInstance(this).writeLog(TAG, 4, "Log StackTrace cant delete file");
            }
        }
    }

    private void LoadFromAirtable() {
        if (data != null) {
            String name = mAnlage.getName();
            data.loadSettingFromAirtable(name);
            Toast.makeText(MainActivity.getContext(), "ACSettings "+name+" von airtable geladen", Toast.LENGTH_LONG).show();
            LogService.getInstance(this).writeLog(TAG, 4, "Start Settings: " + name + " geladen");

            data.loadAnlageFromAirtable(name);
            LogService.getInstance(this).writeLog(TAG, 4, "Start Anlage: " + name + " von Airtable geladen");
            data.saveAnlageToDb(name);
            LogService.getInstance(this).writeLog(TAG, 4, "Start Anlage: " + name + " in db gespeichert");
            writeLog(4, "Reload Model ausgeführt");
        }
    }

    /*
     * startet die Controller app und übergibt die erlaubten SMS Caller Nummern
     */
    public void startControllerApp(){
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);//Action_Main heißt Hauptklasse
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        launchIntent.setClassName("com.conteus.adventure.companion.controller", "com.conteus.adventure.companion.controller.MainActivity");//genau spezifizieren, was aufgerufen werden soll, sonst kommt auswahl
        if (launchIntent != null) {
            ACSettings setting = ACSettings.getInstance();
            launchIntent.putExtra("keyCaller", setting.getKeyCaller());
            launchIntent.putExtra("key_notfall_nummer", setting.getKeyNotfallNummer());
            startActivity(launchIntent);
            writeLog(4, "start Controller ausgeführt");
        } else {
            Toast.makeText(getApplicationContext(), " Companion Main kann Controller nicht starten", Toast.LENGTH_LONG).show();
        }
    }
    /*
     * startet die Controller app und übergibt die erlaubten SMS Caller Nummern
     */
    public void startControllerAppToStartMe(){
        Intent launchIntent = new Intent(Intent.ACTION_MAIN);//Action_Main heißt Hauptklasse
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        launchIntent.setClassName("com.conteus.adventure.companion.controller", "com.conteus.adventure.companion.controller.MainActivity");//genau spezifizieren, was aufgerufen werden soll, sonst kommt auswahl
        if (launchIntent != null) {
            ACSettings setting = ACSettings.getInstance();
            launchIntent.putExtra("keyCaller", setting.getKeyCaller());
            launchIntent.putExtra("key_notfall_nummer", setting.getKeyNotfallNummer());
            launchIntent.putExtra("startMe", "true");
            startActivity(launchIntent);
        } else {
            Toast.makeText(getApplicationContext(), " Companion Main kann Controller nicht starten", Toast.LENGTH_LONG).show();
        }
    }
    /*
     * Wir warten hier auf Einstellungen, welche wie via Broadcast von der Controller app bekommen
     * in diesem Fall einen neuen Radius
     * */
    private void registerRadiusReceiver() {
        mRadiusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String msg = (String) intent.getStringExtra("Radius");
                Log.d(TAG, "registerRadiusReceiver: " + msg);
                //TODO: die radius Info auslesen und im Modell updaten
                // das Ende des Weges ist weg,getRadiusNachOrt
                // der Anfang des Weges ist ort.getRadius
                // anlagename:ort:anradius
                // anlagename:ort:abnachOrt:radius
                // Pizolbahn Wangs:Wangs unten:ab:190:20 setzt den Radius des Ortes Wangs unten auf 20m
                // <anlage>
                // es gibt Weg und Ort
                Radius radius = msgToRadius(msg);
                if (radius != null) {
                    Anlage anlage = Data.getInstance().getAnlageByName(radius.getAnlage());
                    processRadiusChanges(anlage, radius);
                }

            }
        };
        registerReceiver(mRadiusReceiver, new IntentFilter("com.conteus.adventure.companion.controller.radius"));
    }

    // erzeugt aus eimem Radius String ein Radius Objekt
    private Radius msgToRadius (String msg) {
        //TODO NumberFormatException ausschliessen bei falschen Meldungen
        // R:Pizolbahn Wangs:Wangs unten:190:an:20
        Radius r = new Radius();
        // R:
        int doppel = msg.indexOf(":");
        if (msg.length() > doppel) {
            String start = msg.substring(0, doppel);
            // Anlagename
            msg = msg.substring(doppel+1);
            // Pizolbahn Wangs:Wangs unten:190an:20
            doppel = msg.indexOf(":");
            if (msg.length() > doppel) {
                String anlageName = msg.substring(0,doppel);
                r.setAnlage(anlageName);
                // ort
                msg = msg.substring(doppel+1);
                doppel = msg.indexOf(":");
                // Wangs unten:190:an:20
                if (msg.length() > doppel) {
                    String ortName = msg.substring(0,doppel);
                    r.setOrtname(ortName);
                    msg = msg.substring(doppel+1);
                    doppel = msg.indexOf(":");
                    // 190:an:20
                    if (msg.length() > doppel) {
                        String richtung = msg.substring(0, doppel);
                        try {
                            int direction = Integer.valueOf(richtung);
                            r.setDirection(direction);
                        } catch (NumberFormatException e) {
                            Log.d(TAG, "Richtung: "+richtung+" ist keine Zahl");
                        }
                         msg = msg.substring(doppel + 1);
                        // an/ab
                        doppel = msg.indexOf(":");
                        if (msg.length() > doppel) {
                            String anAb = msg.substring(0, doppel);
                            r.setAnAb(anAb);
                            String radius = msg.substring(doppel + 1);
                            try {
                                int rad = Integer.valueOf(radius);
                                if (rad > 0) {
                                    r.setRadius(rad);
                                    return r;
                                }
                            } catch (NumberFormatException e) {
                                Log.d(TAG, "Radius: "+radius+" ist keine Zahl");
                            }
                        }
                    }
                    // radius
                }
            }
        }
        return null;
    }
    private void processRadiusChanges(Anlage anlage, Radius radius ) {
        //Anlage anlage = null;
        // der radius hat einen Wer > 0
        if (radius.getRadius() > 0) {
            // wir haben wirklich eine Anlage
            if (anlage != null) {
                String anAb = radius.getAnAb();
                // der Abstand bei der Ankunft wird gesetzt
                if (anAb != null && "an".equals(radius.getAnAb())) {
                    Ort ort = anlage.getOrtByName(radius.getOrtname());
                    Weg weg = anlage.findWegEndingAt(ort, radius.getDirection());
                    if (weg == null) {
                        weg = anlage.findWegStartingAt(ort, radius.getDirection());
                    }
                    if (weg != null) {
                        weg.setRadiusNachOrt(radius.getRadius());
                    }
                } else {
                    String ortname = radius.getOrtname();
                    if (ortname != null && ortname.length() > 0) {
                        Ort ort = anlage.getOrtByName(ortname);
                        if (ort != null) {
                            ort.setRadius((radius.getRadius()));
                        }

                    }
                }
            }
        }

    }

    private void writeLog(int type, String msg) {
        LogService.getInstance(this).writeLog(TAG, type, msg);
    }


    /*
    private void registerFLICReceiver() {
        mFlicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String msg = (String) intent.getStringExtra("Command");
                Log.d(TAG, "registerFLICReceiver: ");
                executeSMSCommand("FLIP");

            }
        };
        registerReceiver(mFlicReceiver, new IntentFilter("com.condires.adventure.companion.FLIC"));
        LogService.getInstance(this).writeLog(TAG, 4, "FLIC Receiver registered for action:com.condires.adventure.companion.FLIC");
    }
    */

    public void processSMS(final String smsBody, String smsSender) {
        //arrayAdapter.insert(smsBody, 0);
        //arrayAdapter.notifyDataSetChanged();

        // die Preferenzen werden von Companion beim Starten gesetzt
        //String SenderPhoneNumber = "+41795432109";
        //String SMSStartsWith = "Wangs:";

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
        String SenderPhoneNumber = prefs.getString("key_caller", "+41795432109");
        String SMSStartsWith = prefs.getString("key_sms_signature", "Wangs:");
        if (SenderPhoneNumber.contains(smsSender) && smsBody.startsWith(SMSStartsWith)) {
            String cmd = smsBody.substring(SMSStartsWith.length());
            executeSMSCommand(cmd);
        } else {
            writeLog(4, "sms von: "+smsSender+" Befehl nicht verarbeitet, msg: "+smsBody);
        }

    }

    public void executeSMSCommand(String msg) {
        String cmd;
        if (msg.startsWith("R:")) {
            cmd = "R";
        } else if (msg.startsWith("+")) {
            cmd = "+";
        } else if (msg.startsWith("-")) {
            cmd = "-";
        } else if (msg.startsWith("LOAD:")) {
            cmd = "LOAD";
         } else {
            cmd = msg;
        }

        switch (cmd) {

            case "R":
                Radius radius = msgToRadius(msg);
                if (radius != null) {
                    Anlage anlage = Data.getInstance().getAnlageByName(radius.getAnlage());
                    if (anlage.getOrte() == null || anlage.getOrte().size() == 0) {
                        anlage = Data.getInstance().anlageLaden(anlage);
                    }
                    processRadiusChanges(anlage, radius);
                }
                break;
            case "GPS+":
                writeLog(4, "SMS START GPS Tracking ausführen");
                //start LocationTracker
                Intent cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "STARTTRACKING");
                sendBroadcast(cmdIntent);
                break;
            case "GPS-":
                writeLog(4, "SMS STOP GPS Tracking ausführen");
                //start LocationTracker
                cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "STOPTRACKING");
                sendBroadcast(cmdIntent);
                break;
            case "DB+":
                writeLog(4, "SMS START DB Tracking ausführen");
                //start LocationTracker
                cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "STARTDB");
                sendBroadcast(cmdIntent);
                break;
            case "DB-":
                writeLog(4, "SMS STOP DB Tracking ausführen");
                //start LocationTracker
                cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "STOPDB");
                sendBroadcast(cmdIntent);
                break;
            case "CRASH":
                throw new RuntimeException("Crash!");
                //break;

            case "RESTART":
                writeLog(4, "SMS RESTART ausführen");
                //start LocationTracker
                cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "RESTART");
                sendBroadcast(cmdIntent);
                break;
            case "START":
                //start LocationTracker
                writeLog(4, "SMS START ausführen");
                startLocationTracker();
                break;
            case "STOP":
                // stop LocationTracker
                writeLog(4, "SMS STOP ausführen");
                cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                cmdIntent.putExtra("cmd", "STOP");
                sendBroadcast(cmdIntent);

                /*
                Intent intent = new Intent(Intent.ACTION_SEND);  //Action_Main heißt Hauptklasse
                Toast.makeText(getApplicationContext(), " Stop called", Toast.LENGTH_SHORT).show();
                intent.setComponent(new ComponentName("com.condires.adventure.companion", "com.condires.adventure.companion.MainActivity"));
                intent.putExtra("Stop", true);
                startActivity(intent);
                */
                break;
            case "C":
                //startControllerApp();
                AppChecker.openApp(this,"com.conteus.adventure.companion.controller");

                LogService.getInstance(this).writeLnLog("Controller App gestartet");
                break;
            case "+":
                mCompanionAudioService.changeVolume(msg.length());
                LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume to: " + CompanionAudioService.getInstance().getVolume());
                break;
            case "-":
                mCompanionAudioService.changeVolume(-1 * msg.length());
                LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume to: " + CompanionAudioService.getInstance().getVolume());

                break;
            case "SILENT":
                mCompanionAudioService.setSilent();
                LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume Silent: " + CompanionAudioService.getInstance().getVolume());
                break;
            case "FLIP":
                if (mCompanionAudioService.isSilent) {
                    mCompanionAudioService.setLoud();
                    LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume Loud: " + CompanionAudioService.getInstance().getVolume());
                } else {
                    mCompanionAudioService.setSilent();
                    LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume Silent: " + CompanionAudioService.getInstance().getVolume());
                }
                break;
            case "LOUD":
                mCompanionAudioService.setLoud();
                LogService.getInstance(this).writeLog(TAG, 4, "SMS Volume Loud: " + CompanionAudioService.getInstance().getVolume());
                break;
            case "A":
                LogService.getInstance(this).writeLnLog("forced by sms");
                break;
            case "LOAD":
                // Load Anlage from Airtable und save to db
                // geht nur mit WLAN
                if (data != null) {
                    String name = msg.substring(5);  // LOAD abschneiden
                    if ("SETTINGS".equals(name)) {
                        data.loadSettingFromAirtable(mAnlage.getName());
                        Toast.makeText(MainActivity.getContext(), "ACSettings "+name+" von airtable geladen", Toast.LENGTH_LONG).show();
                        LogService.getInstance(this).writeLog(TAG, 4, "SMS ASettings: " + mAnlage.getName() + " geladen");

                    } else {
                        data.loadAnlageFromAirtable(name);
                        LogService.getInstance(this).writeLog(TAG, 4, "SMS Anlage: " + name + " von Airtable geladen");
                        data.saveAnlageToDb(name);
                        LogService.getInstance(this).writeLog(TAG, 4, "SMS Anlage: " + name + " in db gespeichert");
                        cmdIntent = new Intent("com.condires.adventure.companion.LocationTrackerActivity.stop");
                        cmdIntent.putExtra("cmd", "RELOAD");
                        sendBroadcast(cmdIntent);
                        writeLog(4, "Reload Model ausgeführt");
                    }
                }
                break;
            case "BOOST":  // prüfen ob der Volume Booster läuft und starten wenn nicht
                if (AppChecker.isBoosterRunning(this)) {
                    LogService.getInstance(this).writeLog(TAG, 4, "Volume Booster is running");
                } else {
                    // start Volume Booster
                    AppChecker.startBooster(this);
                }
                break;
            case "MODEL":
                String json = mAnlage.getAsJson() + ACSettings.getInstance().getAsJson();
                LogService.getInstance(this).writeLog(TAG, 5, json);
                break;

        }
    }

    /*
     * der Adapter mit dem Stories und Anlagen ausgewählt werden
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        Object o = parent.getItemAtPosition(position);
        if (o instanceof Story) {
            Story item = (Story) parent.getItemAtPosition(position);
            story = item;
            // Showing selected spinner item
            Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
            // wenn eine Anlage gewählt wird, muss sie geladen werden
            // initial besteht das mAnlage Objekt nur aus dem Namen der dbid
        } else   if (o instanceof Anlage) {
            Anlage anl = (Anlage) parent.getItemAtPosition(position);
            mAnlageIndex  = position;
            // die Anlage wird er beim effektiven Starten geladen
            // und falsl nicht, kann sie der LocationTracker nachladen
            //anlageLaden(position);
            Toast.makeText(parent.getContext(), "Selected: " + anl, Toast.LENGTH_LONG).show();
        }


    }
    /*
     * stellt sicher das die Anlage die gespielt werden soll, auch wirklich geladen ist
     */
    public void anlageLaden(int index) {
        mAnlage = Data.getInstance().getAnlage(index);
        mAnlageIndex = data.getAnlageIndex(mAnlage.getName());
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }
    @Override
    protected void onStart() {
        super.onStart();
        initializeLogging();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        //mPlayerAdapter.loadMedia(MEDIA_RES_ID);
        //Log.d(TAG, "onStart: create MediaPlayer");
        List anlagen = Data.getInstance().getAnlagen();
        mAnlageListAdapter.notifyDataSetChanged();
        //mAnlageListAdapter.clear();
        //mAnlageListAdapter.addAll(anlagen);
        if (firstStart) {
            firstStart = false;
            startLocationTracker();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        /*
         * Step 4: Ensure to unregister the receiver when the activity is destroyed so that
         * you don't face any memory leak issues in the app
         */
        if(mRadiusReceiver != null) {
            try {
                unregisterReceiver(mRadiusReceiver);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        /*
        if(mFlicReceiver != null) {
            try {
                unregisterReceiver(mFlicReceiver);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        */

        /*
        if (isChangingConfigurations() && mPlayerAdapter.isPlaying()) {
            Log.d(TAG, "onStop: don't release MediaPlayer as screen is rotating & playing");
        } else {
            mPlayerAdapter.release();
            Log.d(TAG, "onStop: release MediaPlayer");
        }
        */
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mWakeLock.release();
    }

    private void initializeUI() {
        mTextDebug = (TextView) findViewById(R.id.text_debug);
        Button mTestOnButton = (Button) findViewById(R.id.button_test_on);
        Button mPauseButton = (Button) findViewById(R.id.button_test_off);
        Button mTrackButton = (Button) findViewById(R.id.button_GPSTrack);
        Button mResetButton = (Button) findViewById(R.id.button_record);
        Button mStartButton = (Button) findViewById(R.id.button_start);
        Button mAnlageButton = (Button) findViewById(R.id.button_db);
        Button mSettingsButton = (Button) findViewById(R.id.button_setting);
        Button mModelButton = (Button) findViewById(R.id.model);
        //mSeekbarAudio = (SeekBar) findViewById(R.id.seekbar_audio);
        mScrollContainer = (ScrollView) findViewById(R.id.scroll_container);


        final Intent mAdventureRecorderIntent = new Intent(MainActivity.this, AdventureRecorderActivity.class);
        final Intent mAnlageListIntent = new Intent(MainActivity.this, AnlageListActivity.class);
        final Intent mAirtableEditIntent = new Intent(MainActivity.this, AirtableEditActivity.class);
        final Intent mAnlageBrowseActivity = new Intent(MainActivity.this, AnlageBrowseActivity.class);


        final RadioButton mRadioScreenDark = (RadioButton) findViewById(R.id.radioButton_screen_dark);

        mRadioScreenDark.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                mRadioScreenDark.setChecked(!mRadioScreenDark.isChecked());
                darkScreen = mRadioScreenDark.isChecked();
                Toast.makeText(MainActivity.this,mRadioScreenDark.getText(),Toast.LENGTH_SHORT).show();

            }

        });

        ;
        mTestOnButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testOn = "1";
                    }
                });
        mTrackButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        GPSTrackService.getInstance(MainActivity.this).setTracking(true);
                    }
                });
        mPauseButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        testOn="0";
                    }
                });
        mResetButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(mAdventureRecorderIntent);
                    }
                });
        mStartButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startLocationTracker();
                    }
                });
        mAnlageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       // mLocationTrackerIntent.putExtra("Story", story);
                        //mLocationTrackerIntent.putExtra("Story", story);
                        startActivity(mAnlageListIntent);
                    }
                });
        mSettingsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(MainActivity.this, SettingsPrefActivity.class));
                    }
                });
        mModelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // mLocationTrackerIntent.putExtra("Story", story);
                        //mLocationTrackerIntent.putExtra("Story", story);
                        mAnlageBrowseActivity.putExtra("AnlageIndex", mAnlageIndex);
                        //anlageLaden(mAnlageIndex);
                        startActivity(mAnlageBrowseActivity);
                    }
                });
    }

    private void startLocationTracker() {
        mLocationTrackerIntent.putExtra("Story", story);
        //mLocationTrackerIntent.putExtra("Anlage", mAnlage);
        mLocationTrackerIntent.putExtra("TestOn", testOn);
        mLocationTrackerIntent.putExtra("DarkScreen", darkScreen);
        mLocationTrackerIntent.putExtra("AnlageIndex", mAnlageIndex);
        anlageLaden(mAnlageIndex);
        //startActivity(mLocationTrackerIntent);
        startActivityForResult(mLocationTrackerIntent, START_TRACKER_REQUEST);
    }

    /*
     * V1.10: wenn der Tracker sich selbst neu starten wir, schickt er RESTART zurück
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == START_TRACKER_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // wenn im Result RESTART steht, muss der Tracker geleich wieder gestartet werden
                String returnString = data.getStringExtra("action");
                if ("RESTART".equals(returnString)) {
                    startLocationTracker();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);

        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setDisplayedChild(1);
                } else {
                    output.setDisplayedChild(0);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /** Create a chain of targets that will receive log data */

    public void initializeLogging() {
        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");
    }

    /*
    private void initializePlaybackController() {
        MediaPlayerHolder mMediaPlayerHolder = new MediaPlayerHolder(this);
        Log.d(TAG, "initializePlaybackController: created MediaPlayerHolder");
        mMediaPlayerHolder.setPlaybackInfoListener(new PlaybackListener());
        mPlayerAdapter = mMediaPlayerHolder;
        Log.d(TAG, "initializePlaybackController: MediaPlayerHolder progress callback set");
        //mLocationTracker = new LocationTrackerActivity(this);


    }
    */

    private void initializeSeekbar() {
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
                        //mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }

    public void sendCrashSMS(String report) {
        LogService.getInstance(this).writeLog(TAG,4, "send Crash SMS");
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage("++41795432109", null, "crashed"+ report, null, null);
    }


    public class PlaybackListener extends PlaybackInfoListener {

        @Override
        public void onDurationChanged(int duration) {
            mSeekbarAudio.setMax(duration);
            Log.d(TAG, String.format("setPlaybackDuration: setMax(%d)", duration));
        }

        @Override
        public void onPositionChanged(int position) {
            if (!mUserIsSeeking) {
                mSeekbarAudio.setProgress(position, true);
                Log.d(TAG, String.format("setPlaybackPosition: setProgress(%d)", position));
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
        }
    }


}