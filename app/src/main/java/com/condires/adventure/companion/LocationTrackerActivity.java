package com.condires.adventure.companion;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.condires.adventure.companion.alarm.AlarmService;
import com.condires.adventure.companion.audio.CompanionAudioService;
import com.condires.adventure.companion.audio.Recorder;
import com.condires.adventure.companion.gpstracker.GPSTrackService;
import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.logwrapper.Log;
import com.condires.adventure.companion.model.Aktion;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ereignis;
import com.condires.adventure.companion.model.IrgendWo;
import com.condires.adventure.companion.model.LocationSim;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.PlayerStatus;
import com.condires.adventure.companion.model.Story;
import com.condires.adventure.companion.model.Weg;
import com.condires.adventure.companion.setting.ACSettings;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LocationTrackerActivity extends AppCompatActivity {
    // TODO: SMS einbauen: Loglevel mer sms steuern, kompaktes log via sms bestellen
    // TODO umbenennen in Player statt Tracker
    // TODO: jede Logmeldung bekommt eine ID, eine Liste von IDs reicht um den Verlauf zu sehen
    // Done Hintergrund Musik eingebaut
    // TODO Icon und Hintergrundbild einbauen
    // Done Batteriestatus und ob geladen wird
    // Done Lautstärke
    // TODO Zeit
    // Done Connected oder nicht
    // sim Karte ja oder nein, damit ich sms empfangen kann: ja
    // beim chat, grösserer Bildschirm
    // Logmeldungen via Chat schicken
    // TODO stillstand von kein GPS Empfang unterscheiden, wenn keine neuen Locations kommen, stehen wir oder haben keinen EMpfang

    private final String TAG = this.getClass().getSimpleName();


    //private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    //private Location        location;
    private LocationRequest mLocationRequest;
    private List<Location>  mLocationFifo = new ArrayList<Location>();   // die letzten 4 Positionen
    private FusedLocationProviderClient mFusedLocationClient;
    private final int       LOCATION_PERMISSION = 1001;
    private final int       REQUEST_CHECK_SETTINGS = 10001;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds

    //private final static int SMS_PERMISSION_CODE = 10;

    //public static final int MEDIA_RES_ID = R.raw.jazz_in_paris;
    private MediaPlayer     mediaPlayer;
    // Done: Background Music aus den Actions holen, bzw aus dem Ort oder Weg
    int                     mBackgroundMediaId = R.raw.on_my_way_with_lyrics;
    MediaPlayer             mBackgroundMediaPlayer;
    MediaPlayer             currentPlayer;  // wer hat gespielt, als der Notstop aktiviert wurde?

    private TextView        locationTv;
    // Done: umstellen auf ausgewählte Anlage
    private Data            data = Data.getInstance();
    private Anlage          anlage;
    private List<Ereignis>  journey;
    private Story           story = null;

    private String          storyName; // welche Geschichte wird erzählt.
    //private Ort             aktuellerOrt;


    private int     count = 0;
    private String  testOn = "0";
    boolean mIsTrackSim = false;  // weiter unten beim testen setzen
    private boolean darkScreen = true;
    private boolean stopActivity = false;
    int             mAnlageIndex;

    private long lastRestart;    // wann wurde ein Restart gemacht?
    private long lastVolumeLog;  // wann wurde das letzte mal gemeldet, dass das Volume falsch ist
    private int  countWrongDirection = 0;   // wie oft wurde hintereinander eine falsche Richtung erkannt

    private boolean wifiTest = false;  // in der simulation wifi ein/aus oder immer ein
    private boolean stopTest = false;  // in der Simulation ein Stop getested werden
    private boolean restartGPSTest = false;  // true heisst es wird ein Restart GPS gemacht, sobald WLAN da ist
    //private boolean mGPSTracking = false;    // damit kann das GPS Tracking ein und ausgeschaltet werden
    private GPSTrackService mGPSTrackService;
    private boolean mDBTracking = false;    // damit kann das Dezibel Tracking ein und ausgeschaltet werden
    private PlayerStatus ps = new PlayerStatus();


    // Status
    Intent            mBatteryStatus = null;
    BroadcastReceiver mAkkuReceiver;
    long mLastAkkuCheck;                // wir checken nur alle 5 Minuten
    List<String> mStatusElemente;       // alle Elemente die pro Runde rapportiert werden
    Map<String, String> mStatusNamen;   // die Namen der StatusElemente
    //StringBuilder mStatusMsg;           // die Statusmeldung, die auf dem Log ausgegeben wird
    List<String> mStatusList = null;    // statt einen Stringbulider zu verwenden, eine Liste nehmen
    int mStartLoopIndex = 0;            // bei welcem Element starten wir wenn wir unten angekommen sind

    // SMS Steuerung
    BroadcastReceiver mMessageCmdReceiver;    // nimmt Befehle von MainActivity entgegen
    private BroadcastReceiver mFlicReceiver;  // erkennt den Command vom Flic-Button

    //private BroadcastReceiver mRadiusReceiver;

    // SMS Kommunikation
    // der Player soll selbst keine sms verarbeiten, das geht via Controller
    //private SmsBroadcastReceiver mSmsBroadcastReceiver;

    // network detection
    private ConnectivityManager manager;
    WifiManager          mWifiManager;
    private int          mPreferredNetworkID;
    private boolean      mRestartNachWlan = false;    // bei falschem Ort gemäss MAC wird ein Restart bei WLAN verlust erzwungen


    private LogService   mLogService;
    private AlarmService mAlarmService;

    private String       mDeviceName;  // der name des Gerätes

    private static Context myContext = null;

    // Dezibel Meter
    private Recorder mRecorder;         //ah o
    /*
    private Handler  mHandler = new Handler();
    private Runnable mUpdateTimer = new Runnable() {        //q
        @Override
        public void run() {
            float f2 = mRecorder.SoundDB();
            long msSinceStart = ps.getNow()- ps.getCurrentEreignis().getStart();
            writeLog(6, msSinceStart+","+f2);
            mHandler.postDelayed(mUpdateTimer, 300L);
        }
    };
    */
    /*
     * schreibt einen Dezibel Wert ins DB Log
     */
    private void writeDB() {
        float f2 = mRecorder.SoundDB();
        long msSinceStart = ps.getNow()- ps.getCurrentEreignis().getStart();
        writeLog(6, msSinceStart+","+f2);
    }

    // wird benötigt um eine Bewegung zu simuleren
    Handler mSimulationHandler = new Handler();
    int i;
    long    stoptime = 0;
    int     iNotstop = 50;  // bei i = 50 halten wir an
    int     stopDauer = ACSettings.getInstance().getTimeToNotstop()*1000;  // Millisekunden
    boolean ersteRunde = true;   // notstop nur in der ersten Runde
    int     delay = 1000; // Millisekunden
    float   direction = LocationSim.getDirectionForeward();

    LocationSim lastLoc;
    LocationSim loc;

    // Simulation eines echten Tracks
    Runnable mPeriodicUpdateTrack = new Runnable() {
        /*
         * wir gehen den Weg einmal vorwärts durch und dann rückwärts
         */
        public void run() {
            lastLoc = loc;  // merken wo ich war, damit ich Distanz berechnen kann
            loc = LocationSim.getLocation(i++);
            if (mIsTrackSim) {
                if (i >= loc.size()) {
                    i = 0;
                }
                if (i==0) {
                    long now1 = System.currentTimeMillis();
                    ps.setNow(now1);
                }

                if (lastLoc != null) {
                    if (lastLoc.getWifiLevel() == 0 && loc.getWifiLevel() > 0) {
                        ps.setWlanStatus("WIFI");
                        ps.setWifiSSID("Simulator");
                        ps.setWifiLevel(loc.getWifiLevel());
                        ps.setMACAddress(loc.getMACAdress());
                        writeLog(4, "connected to MAC:" + ps.getMACAddress() + ":" + ps.getWifiSSID());
                        handleNetworkConnected();
                    } else if (lastLoc.getWifiLevel() > 0 && loc.getWifiLevel() == 0) {
                        handleConnectionLost();
                    }
                }
            }
            /*
            if (lastLoc != null) {
                delay = loc.getDelayms() - lastLoc.getDelayms();
            }
            */
            // Simulation läuft doppelt so schnell wie original
            delay = loc.getDelayms();
            if (delay > 10000) delay = 10000;
            mSimulationHandler.postDelayed(mPeriodicUpdateTrack, delay); //milliseconds);
            double lat = loc.getLatitude();
            double lng = loc.getLongitude();

            ps.setLatitude(lat);
            ps.setLongitude(lng);
            ps.setSpeed(loc.getSpeed());
            ps.setAccuracy(loc.getAccuracy());
            ps.setAltitude(loc.getAltitude());
            ps.setDirection(loc.getDirection());
            displayLocation(ps);
            // TODO: wifi sollte auch simuliert werden.
        }
    };


    // Simulation eines berechneten Tracks
    Runnable mPeriodicUpdate = new Runnable() {
        /*
         * wir gehen den Weg einmal vorwärts durch und dann rückwärts
         */
        public void run() {
            // scheduled another events to be in 10 seconds later
            // Done Richtung nicht fix, sondern variable Hin und Zurück
            // der speed kommt aus dem simulator

            mSimulationHandler.postDelayed(mPeriodicUpdate, delay); //milliseconds);
            lastLoc = loc;  // merken wo ich war, damit ich Distanz berechnen kann
            loc = LocationSim.getLocation(i);

            if (i == 0) {
                loc.setAccuracy(6);
                // Speed berechnen
                direction =   LocationSim.getDirectionForeward();
                writeLog(3, "Ich simuliere den Start und fahre vorwärts");
                //loc.setDirection(190);  // Zurück 74, 240 vor
            } else if (i == loc.size()) {
                direction = LocationSim.getDirectionBackward();
                writeLog(3, "Ich simuliere das Ende des Weges und wir fahren wieder zurück");
                //loc.setDirection(15);
            }
            if (i != 0) {
                loc.setDirection(direction);
                if (loc.getDirection() == LocationSim.getDirectionForeward()) {
                    Ort ort = new Ort(lastLoc.getLatitude(), lastLoc.getLongitude());
                    float dist = ort.calculateDistance(loc.getLatitude(), loc.getLongitude());
                    loc.setSpeed(dist / delay * 1000);  // *1000 weil delay ms
                } else {

                }
            }


            if (wifiTest) {
                IrgendWo ort = ps.getOrt();
                boolean w = mWifiManager.isWifiEnabled();
                if (ort instanceof Weg) {
                    // auf dem Weg haben wir kein WLAN
                    // könnten wir auch auf Distanz prüfen, zb 20m vorher haben wir WLAN
                    if (w) {
                        mWifiManager.setWifiEnabled(false); // true or false to activate/deactivate wifi
                    }
                } else {
                    if (!w) {
                        mWifiManager.setWifiEnabled(true); // true or false to activate/deactivate wifi
                    }
                }
            }

            ps.setLatitude(loc.getLatitude());
            ps.setLongitude(loc.getLongitude());
            ps.setSpeed(loc.getSpeed());
            ps.setAccuracy(loc.getAccuracy());
            ps.setAltitude(loc.getAltitude());
            ps.setDirection(loc.getDirection());
            displayLocation(ps);
            if (mIsTrackSim) {
                i++;
            } else {
                if (loc.getDirection() == LocationSim.getDirectionForeward()) {
                    if (ersteRunde) {
                        if (i == iNotstop && stopTest) {
                            if (stoptime == 0) {
                                stoptime = System.currentTimeMillis();
                            } else {
                                long dauer = System.currentTimeMillis() - stoptime;
                                if (dauer > (stopDauer + 10000)) {
                                    stoptime = 0;
                                    i++;
                                }
                            }
                        } else {
                            i++;
                        }
                    } else {
                        i++;
                    }
                } else {
                    i--;
                }
            }
        }
    };

    /*
    private boolean checkWLANSimulator() {
        if (testOn.equals("1")) {
            IrgendWo ort = ps.getOrt();
            if (ort instanceof Weg) {
                // auf dem Weg haben wir kein WLAN
                // könnten wir auch auf Distanz prüfen, zb 20m vorher haben wir WLAN
                return false;
            }
        }
        return true;  // wenn kein Simulator läuft it true
    }
    */

    private void registerCmdReceiver() {
        mMessageCmdReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                String cmd  = intent.getStringExtra("cmd");
                if ("STARTTRACKING".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" START GPS Tracking Command bekommen");
                    mGPSTrackService.setTracking(true);
                    ps.setGPSTracking(true);
                } else if ("RELOAD".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" Anlage Reload Command bekommen");
                    anlage = data.getAnlage(mAnlageIndex);
                } else if ("STOPTRACKING".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" STOP GPS Tracking Command bekommen");
                    // der zweite Stop Befehl setzt das Log zurück, dadruch wird nicht immer geschrieben
                    if (ps.isGPSTracking() == false) {
                        mLogService.setAirtableGPSTrackNull();
                    }
                    ps.setGPSTracking(false);
                    mGPSTrackService.setTracking(false);
                } else if ("STARTDB".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" START DB Tracking Command bekommen");
                    mDBTracking = true;
                } else if ("STOPDB".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" STOP DB Tracking Command bekommen");
                    // der zweite Stop Befehl setzt das Log zurück, dadruch wird nicht immer geschrieben
                    if (mDBTracking == false) {
                        mLogService.setAirtableLogDBNull();
                    }
                    mDBTracking = false;


                } else if ("RESTART".equals(cmd)) {
                    writeLog( 4,ps.getNowTime()+" RESTART Command bekommen");
                    Toast.makeText(LocationTrackerActivity.this, "Restart GPS Command", Toast.LENGTH_LONG).show();
                    restartGPS();
                } else {
                    writeLog( 4,ps.getNowTime()+" STOP Command bekommen");
                    // TODO sicherstellen, dass LogRest rausgeschrieben wird
                    finish();
                }
            }

        };
        registerReceiver(mMessageCmdReceiver, new IntentFilter("com.condires.adventure.companion.LocationTrackerActivity.stop"));
    }


    // Connection Detection
    private final ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            super.onAvailable(network);

            // this ternary operation is not quite true, because non-metered doesn't yet mean, that it's wifi
            // nevertheless, for simplicity let's assume that's true
            // schicke alle Alarme raus, wenn airtable da ist
            // im Testmode hat das reale WLAN keinen Einnfluss
            if (!testOn.equals("1")) {
                ps.setWlanStatus("connected to " + (manager.isActiveNetworkMetered() ? "LTE" : "WIFI"));
                String macadresse = "";
                if (mWifiManager != null) {
                    WifiInfo info = mWifiManager.getConnectionInfo();
                    ps.setWifiSSID(info.getSSID());
                    int level = WifiManager.calculateSignalLevel(info.getRssi(), 5);
                    ps.setWifiLevel(level);
                    macadresse = info.getBSSID();
                    ps.setMACAddress(macadresse);
                }
                writeLog(4, "connected to " + (manager.isActiveNetworkMetered() ? "LTE" : "WIFI") + " MAC:" + macadresse + ":" + ps.getWifiSSID());
                handleNetworkConnected();
            }
        }

        @Override
        public void onLost(Network network) {
            super.onLost(network);
            if (!testOn.equals("1")) {
                handleConnectionLost();
            }
        }
    };

    private void handleConnectionLost() {
        long now = System.currentTimeMillis();
        ps.setWlanStatus("WLAN weg");
        ps.setWifiLevel(0);
        if (ps.isGPSTracking()) {
            mLogService.writeGPSLocation(ps.getLatitude(), ps.getLongitude(), ps.getSpeed(), ps.getDirection(), ps.getWifiLevel(), 0, ps.getMACAddress(), ps.getOrt(), ps.getAccuracy());
        }
        Ort ortNachMac = anlage.getOrtByMAC(ps.getMACAddress());
        if (ACSettings.getInstance().getIsWLANMaster() == 1 && ortNachMac != null) { // WLAN entscheidet wo wir sind
            writeLog(4, "Ort "+ortNachMac+"verlassen aufgrund WLAN"+ps.getMACAddress());
            ortVerlassen(ps, now);
        }
        // neu mit der id des Orrtes statt mit der MacAdessen, weil ein Ort mehrere MAC Adressen haben kann
        if (ortNachMac != null) {
            String ortId = ortNachMac.getId();
            writeStatus( ortId+"LOST", "");
        }
        //writeStatus( ps.getMACAddress()+"LOST", "");
        ps.setLastMACAddress(ps.getMACAddress());  // merken um einen reconnect zu erkennen
        ps.setMACAddress("");

        writeLog(4, "WLAN ist weg");
        // wenn wir wlan hatten und der Ort falsch war
        if (mRestartNachWlan) {
            writeLog(4, "Ort gemäss MAC stimmt nicht");
            restartGPS();
            mRestartNachWlan = false;
        }
    }

    private void handleNetworkConnected() {
        long now = System.currentTimeMillis();
        Ort ortNachMac = anlage.getOrtByMAC(ps.getMACAddress());
        if (ortNachMac != null) {
            String ortId = ortNachMac.getId();
            writeStatus( ortId+"CONNECT", ps.getWifiLevel()+"");
            writeLog(4, "MAC Ort:" + ps.getMACAddress() + " =" + ortNachMac.getName());
        }
        if (ps.isGPSTracking()) {
            mLogService.writeGPSLocation(ps.getLatitude(), ps.getLongitude(), ps.getSpeed(), ps.getDirection(), ps.getWifiLevel(), 0, ps.getMACAddress(), ps.getOrt(), ps.getAccuracy());
        }
        // reconnect auf WLAN erkennen
        // kann ein anderer Router sein am selben Ort
        if (!(ps.getMACAddress()+"").equals(ps.getLastMACAddress())) {
            // V20.5 kann auch am selben Ort mit unterschiedlicher MAC Adresse sein.
            Ort lastOrtNachMac = anlage.getOrtByMAC(ps.getLastMACAddress());
            if ((ortNachMac != null && lastOrtNachMac != null) && ortNachMac.name.equals(lastOrtNachMac)) {
                writeLog(4, "other MAC:"+ps.getMACAddress());
            } else {
                mAlarmService.writeAlarme();
                ortNachMac = anlage.getOrtByMAC(ps.getMACAddress());
                if (ACSettings.getInstance().getIsWLANMaster() == 1 && ortNachMac != null) { // WLAN entscheidet wo wir sind
                    writeLog(4, ps.getNowTime() + " Ort:" + ortNachMac + " angekommen aufgrund WLAN");
                    // wir sind auf dem falschen Weg unterwegs
                    if (ps.getZielOrt() != ortNachMac) {
                        if (ps.getZielOrt() != null) {
                            writeLog(4, ps.getNowTime() + " Fehler Zielort:" + ps.getZielOrt().getName() + "<>" + ortNachMac.getName());
                            ps.setZielOrt(ortNachMac);
                            // letzterWeg wenn er vom Typ Weg ist muss auf den richtigen Weg gesetzt werden, bei Mitte schwer
                        }

                    }
                    // TODO 1.48 wenn wir am richtigen Ort sind, nichts machen. Das ist, wenn wir das WLAN am Ort verlieren und wieder bekommen
                    // dann dürfen wir nicht nochmals ankommen
                    IrgendWo aktuellerOrt = ps.getOrt();
                    boolean schonDa = false;
                    if (aktuellerOrt instanceof Ort) {
                        if (aktuellerOrt == ortNachMac) {
                            schonDa = true;
                            writeLog(4, ps.getNowTime() + "war schon da:" + ortNachMac.getName());
                        } else {

                        }
                    }
                    if (!schonDa) {
                        ortAngekommen(ps, now);
                    }
                } else {
                    // TODO: wenn wir keinen Ort gemäss MAC Adresse finden
                    // prüfen, ob wir in der Nähe eines Ortes sind, sonst GPS neustart
                    if (ps.getOrt() != null) {         // Player glaubt er ist an einem Ort/oder auf einem Weg
                        if (ps.getJourney() != null) {  // er ist schon auf einer Reise
                            if (!isNearOrt(ps)) {      // der Ort ist kein Ort, bzw er
                                // wenn WLAN kommt, müssen wir an einem Ort sein sonst ist GPS falsch
                                writeLog(4, "GPS Fehler: WLAN da: muss ein Ort sein, ist Weg:" + ps.getOrt());
                                restartGPS();
                            } else {
                                // V 1.31
                                // wir sind an einem Ort, wir haben GPS, sind wir am richtigen Ort?
                                // Achtung lastRestart wurde soeben auf now gesetzt
                                Ort ortGemMac = null;
                                if (anlage != null) {
                                    ortGemMac = anlage.getOrtByMAC(ps.getMACAddress());
                                    writeLog(4, "Ort gem. MAC:" + ps.getMACAddress() + " =" + ortGemMac);
                                }
                                // wo glaubt der Player zu sein?
                                if (ortGemMac != null) {   // V1.32
                                    IrgendWo ort = ps.getOrt();
                                    if (ort != null && ort instanceof Ort) {
                                        if (ort == ortGemMac) {
                                            // die Welt ist in Ordnung, wir sind dort wo wir glauben
                                            mRestartNachWlan = false;
                                        } else {
                                            // wir sind nicht dort wo wir glauben
                                            // eigentlich Restart machen wenn wir wlan verlieren
                                            lastRestart = lastRestart - (ACSettings.getInstance().getRestartDelaySec() * 1000);
                                            mRestartNachWlan = true;
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        } else {
            writeLog(4, "reconnect:"+ps.getMACAddress());
        }

        if (ps.isGPSTracking()) {
            IrgendWo standort = ps.getOrt();
            if (standort != null && standort instanceof Ort) {
                Ort hier = (Ort) standort;
                Ort logOrt = mGPSTrackService.getLetzterLogOrt();
                if (logOrt == null || (logOrt != null && logOrt != hier)) {
                    writeLog(4, "WLAN da schreiben GPSTrack:" + hier);
                    //long now = System.currentTimeMillis();
                    if (hier == anlage.getHome()) {
                        int deltaTms = (int) (now - ps.getNow());
                        String eMediaName = ps.getCurrentMediaName();
                        // Achtung, speed ist hier falsch, nehme die normale Geschwindigkeit
                        // TODO: die Durchschnittsgeschwindigkeit berechnen und einsetzen
                        mLogService.writeGPSLocation(ps.getLatitude(), ps.getLongitude(), 4, ps.getDirection(), ps.getWifiLevel(), deltaTms, eMediaName, ps.getOrt(),ps.getAccuracy());
                        if (logOrt != null) {
                            mGPSTrackService.writelnGPSTrack(hier);
                        } else {
                            mGPSTrackService.writeGPSTrack(hier);
                        }
                    } else {
                        String eMediaName = "";
                        if (ps.getCurrentEreignis() != null) {
                            eMediaName = Data.getInstance().getMediaName(ps.getCurrentEreignis().getMediaId());
                        }
                        //long now = System.currentTimeMillis();
                        int deltaTms = (int) (now - ps.getNow());
                        mLogService.writeGPSLocation(ps.getLatitude(), ps.getLongitude(), ps.getSpeed(), ps.getDirection(), ps.getWifiLevel(), deltaTms, eMediaName, ps.getOrt(), ps.getAccuracy());
                        mGPSTrackService.writeGPSTrack((Ort) ps.getOrt());
                    }
                } else {
                    writeLog(4, "GPSTrack schon geschrieben:" +hier);
                }

            }

        }
    }

    /*
     holt die NetworkId eines WLANs dessen Namen wir kennen
     Damit wir am Ort versuchen können auf das WLAN zu connecten
     */
    // "Pizol_Betrieb_1"
    private int getWLANNetworkId(String SSID) {
        List<WifiConfiguration> wifilist = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConf : wifilist) {
            String confSSID = wifiConf.SSID;    // ssid.lenght=30, confSSID.lenght=34
            if (confSSID.contains(SSID)) {
                return wifiConf.networkId;
            }
        }
        return 0;
    }

    public static Context getContext() {
        return myContext;
    }

   public void restartGPS() {
        // bei stillstand und in der Station versuchen wir keinen Restart
        if (ps.getSpeed() > ACSettings.getInstance().getGpsMinSpeed()) {
            writeLog(4, ps.getNowTime() + " versuche Restart");
            Long now = System.currentTimeMillis();
            if (lastRestart == 0) {
                lastRestart = now - ACSettings.getInstance().getRestartDelaySec();
            }
            // 3*60*1000  = 180000 ms
            long dauer = (now - lastRestart) / 1000;
            if (dauer > ACSettings.getInstance().getRestartDelaySec() || dauer == 0) {
                writeLog(4, "mache Restart");
                lastRestart = now;
                // darf nur alle 3 Minuten gemacht werden
                // V1.10: TrackerActivity Neustart falls es schon mal gestartet wurde
                Intent resultIntent = new Intent();
                resultIntent.putExtra("action", "RESTART");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            } else {
                writeLog(4, "kein Restart, letzter war vor (Sec):" + dauer);
            }
        }
   }

    /*
     * prüft, ob wir an einem Ort oder in WLAN Distanze von einem Ort sind.
     * Wird benutzt um zu prüfen, wie genau das GPS ist, wenn wir WLAN haben und nicht bei einem
     * Ort sind, stimmt die Position nicht
     */
    public boolean isNearOrt(PlayerStatus ps) {
        IrgendWo ort = ps.getOrt();
        Long now = System.currentTimeMillis();
        // wenn wir auf einem Weg sind, stimmt die Position nicht, WLAN kommt immer an einem Ort
        if (ort instanceof Ort) {
            writeLog(4, "wir sind in:"+ ort.getName());
            lastRestart = now;   // Restart am Ort verhindern
            return true;

            // wir sind am Ankommen,WLAN ist da, aber wir sind nicht auf dem Weg
        } else if (ps.getAbstandZiel() < ACSettings.getInstance().getMaxWlanDistance()) {
            writeLog(4, "wir sind näher Zielort (m):"+ps.getAbstandZiel()+" als:"+ACSettings.getInstance().getMaxWlanDistance());
            // 100m von Ort weg ist akzeptable
            lastRestart = now;   // Restart am Ort verhindern
            return true;
            // wir sind am Wegfahren, aber noch nah am Startort
        }  else if (ps.getAbstandStart() < ACSettings.getInstance().getMaxWlanDistance()) {
            writeLog(4, "wir sind näher Startort (m):"+ps.getAbstandStart()+" als:"+ACSettings.getInstance().getMaxWlanDistance());
            lastRestart = now;   // Restart am Ort verhindern
            return true;
        }
        writeLog(4, ps.getNowTime()+" zu weit vom Ort. Ziel:"+ps.getAbstandZiel()+" Start:"+ps.getAbstandStart()+" WLAN-Dist:"+ACSettings.getInstance().getMaxWlanDistance());

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.location_tracker);
        myContext = this;
        locationTv = findViewById(R.id.location);
        mLogService = LogService.getInstance(this);

        writeLog(4, "\nApp:"+getResources().getString(R.string.app_name));
        mAlarmService = AlarmService.getInstance(this);

        mGPSTrackService = GPSTrackService.getInstance(this);
        ps.setGPSTracking(mGPSTrackService.isTracking());
        ps.setNowDateTime(DateFormat.getDateTimeInstance().format(new Date()));
        long now = System.currentTimeMillis();
        ps.setNow(now);

        if (mWifiManager == null) {
            askForPermission("android.permission.ACCESS_WIFI_STATE", 132);
            askForPermission("android.permission.CHANGE_WIFI_STATE", 133);
            mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        }

        try {
            mPreferredNetworkID = getWLANNetworkId(ACSettings.getInstance().getPreferredWifiSSID());
        } catch (Exception e) {
            writeLog(4, "WLAN Connect Error:"+e.getMessage());
        }

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            story = (Story) getIntent().getSerializableExtra("Story"); //Obtaining data
            testOn = (String) getIntent().getStringExtra("TestOn"); //Obtaining data
            mAnlageIndex = getIntent().getIntExtra("AnlageIndex", 0);
            if (data == null) {
                data = new Data(getApplicationContext());
            }
            // holt die Anlage am Index i und stellt sicher, dass sie geladen ist
            anlage = data.getAnlage(mAnlageIndex);
            darkScreen = getIntent().getBooleanExtra("DarkScreen", true);
            stopActivity = getIntent().getBooleanExtra("Stop", false);

        }
        // stopActivity wird vom Controller Remote Control gesetzt
        if (stopActivity == false) {
            if (story != null) {
                storyName = story.getName();
            }

            if (darkScreen) {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                params.screenBrightness = 0;
                getWindow().setAttributes(params);
            }

            registerAkkuListener();
            registerCmdReceiver();  // kann den LocationTracker stoppen
            //registerRadiusReceiver();
            registerFLICReceiver();  // reagiert auf den Flic-Button

            // WLAN Listener Registrieren
            manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            manager.registerDefaultNetworkCallback(networkCallback);


            mDeviceName = mLogService.getDeviceName();
            ps.setPlayerName(mDeviceName);



            // Logmeldungen auf Airtabel schreiben
            //mAirtableService = new AirtableService();
            //mAirtableService.onStart();

            // TODO:Background Music für die Story aus Aktion holen
            Aktion backgroundAktion = anlage.getBackgroundMusic();
            if (backgroundAktion != null) {
                int id = backgroundAktion.getMediaId();
                if (id != 0) {
                    mBackgroundMediaId = id;
                } else {
                    writeLog(3, "Fehler: keine mediaId für Backgroud");
                }
            }
            //Dine holt die default Background Musik der Anlage
            startBackgroundMusic();
            // TODO: die richtige Journey holen
            journey = anlage.createNewJourney();
            ps.setHomeLatitude(anlage.getHome().getLatitude());
            ps.setHomeLongitude(anlage.getHome().getLongitude());
            ps.setAngekommenUm(System.currentTimeMillis());
            // für alle Wege, die keine Richtung haben, die Richtung berechnen
            // TODO: Richtungen sind falsch
            for (Weg w : anlage.getWege()) {
                int dir = (int) w.getDirection();
                if (dir == 0) {
                    float d = w.getVonOrt().calculateDirection(w.getNachOrt());
                    w.setDirection(d);
                }
            }
            // Simulator oder echte GPS Daten als Treiber?
            // Done: Simulator könnten die ganze Anlage simulieren
            // TODO: SImulation ab GPSTrack von Airtable laufen lassen, inkl WIFI
            LocationSim l = null;
            boolean simWangs = false;
            mIsTrackSim = true;
            if (testOn.equals("1")) {
                ACSettings.getInstance().setIsWLANMaster(2);  // GPS ist bei einer Simulation immer Master
                // dadurch kann ich kein WLAN Master Simulieren, aber wenigstens die Anlage
                LocationSim ls = new LocationSim();
                if (mIsTrackSim) {
                    String name = "Wangs-3-2020-07-15";
                    int no = 1;
                    List<LocationSim> track = ls.getTrack(mLogService, name, no);
                    ls.setLocation(track);
                    mSimulationHandler.post(mPeriodicUpdateTrack);
                    writeLog(4, "Track: "+no+" Name"+name+" GPS Simulator gestartet");

                } else {
                    if (simWangs) {
                        List<LocationSim> track = ls.createWangs();
                        ls.setLocation(track);
                        mSimulationHandler.post(mPeriodicUpdateTrack);
                        writeLog(4, "Wangs GPS Simulator gestartet");
                    } else {
                        Weg weg = anlage.findWegStartingAt(anlage.getHome());
                        // der Track wird in die statische Liste eingetragen
                        List<LocationSim> track = ls.createTrack(weg, 7);
                        ls.setLocation(track);
                        float direction = weg.getDirection();
                        Ort ende = weg.getNachOrt();
                        // suche den Weg, der am ende des ersten Wegs startet und in die gleiche Richtung fährt
                        weg = anlage.findWegStartingAt(ende, direction);
                        List<LocationSim> track1 = ls.createTrack(weg, 7);
                        ls.appendLocation(track1);
                        l = LocationSim.getLocation(0);
                        // der Simulator geht durch die statische Liste einmal vorwärts und dann rückwärst durch
                        mSimulationHandler.post(mPeriodicUpdate);
                        writeLog(4, "GPS Simulator gestartet");
                        //test2(2);   // 1 wangs  2 = dekan
                    }
                }
            } else {
                // Client für GPS Tracking holen
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
                // createAndCheckLocationRequest();
                getLocation();
                writeLog(4, "GPS Modul gestartet");
                //TODO hier denn ConnectionListener starten

            }

            // CompanionAudioService ist ein Singleton und muss in der aufrufenden Activity gesetzt sein
            CompanionAudioService as = CompanionAudioService.getInstance();

            // beim Start wird die Lautstärke gemäss einstellungen gesetzt
            if (as != null) {
                as.setVolume(ACSettings.getInstance().getVolume());
                writeLog(4, "Volume auf :"+ ACSettings.getInstance().getVolume());
            }

            mRecorder = new Recorder(getApplicationContext());

        } else {
            writeLog(3, "Stop Command von Konsole bekommen");
            LocationTrackerActivity.this.finish();
        }
    }
    /*
     * der Location Tracker kann aus einer anderen App mit dem Intent Stop gestoppt werden
     */
    /*
    @Override
    protected void onNewIntent(Intent intent) {
        String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        Bundle extras = intent.getExtras();
        super.onNewIntent(intent);
        extras = intent.getExtras();
        String actionType  = intent.getType();
        String action = intent.getAction();
        //String text = getIntent().getStringExtra(Intent.EXTRA_TEXT);
        stopActivity = getIntent().getBooleanExtra("Stop", false);
        if(stopActivity)
        {
            LocationTrackerActivity.this.finish();
        }
        //LocationTrackerActivity.this.finish();
    }
    */

    private void registerFLICReceiver() {
        mFlicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //String msg = (String) intent.getStringExtra("Command");
                Log.d(TAG, "registerFLICReceiver: ");
                CompanionAudioService ac = CompanionAudioService.getInstance();
                if (ac.isSilent) {
                    ac.setLoud();
                    writeLog( 4, "SMS Volume Loud: " + ac.getVolume());
                } else {
                    ac.setSilent();
                    writeLog( 4, "SMS Volume Silent: " + ac.getVolume());
                }

            }
        };
        registerReceiver(mFlicReceiver, new IntentFilter("com.condires.adventure.companion.FLIC"));
        writeLog(4, "FLIC Receiver registered for action:com.condires.adventure.companion.FLIC");
    }
    /*
     * Bequemlichkeitsfunktionen des LogServices
     * Schreibt nur wenn der WLAN Simulator sagt wir haben WLAN oder wenn wir eczt unterwegs sind
     */
    private void writeLog(int type, String msg) {
            mLogService.writeLog(TAG, type, msg);
    }
    // wurde für status eingeführt, status wird in 3 Felder geschrieben
    private void writeLog(int type, List<String> msgList) {
        mLogService.writeLog(TAG, type, msgList);
    }
    private void writeLnLog(String msg) {
            mLogService.writeLnLog(msg);

    }
    public void writeArrivalInfo(float speed, String zielOrtName, double distToZiel) {
            mLogService.writeArrivalInfo(speed, zielOrtName, distToZiel);
    }

    private String statusAsString(List<String> statusList) {
        // 6 Zeichen Name wegschneiden
        String msg = "";
        for (String zeile : statusList) {
            msg = msg+zeile.substring(7);
        }
        return msg;
    }
    private void writeStatusToLongLog() {
        mLogService.writeStatusToLongLog();
    }

    /*
     damit kann ein Wert i
      mitte:  mediaName = mitte  macCONNECT
      mitte: mediaName = musikMitte
      mediaName kann sein macAddressCONNECT  oder LOST  Value zb Wifilevel
     */
    public synchronized void writeStatus(String mediaName, String value) {
        String macUnten = "";
        String macMitte = "";
        String macOben = "";
        String idUnten = "";
        String idMitte = "";
        String idOben = "";
        int richtungMitte = 0;
        // Liste initialisieren
        if (mStatusElemente == null) {
            try {
                macUnten = anlage.getHome().getMACAddress();
                Weg untenMitte = anlage.findWegStartingAt(anlage.getHome());
                Ort mitte = untenMitte.getNachOrt();
                macMitte = mitte.getMACAddress();
                Weg mitteOben = anlage.findWegStartingAt(mitte, untenMitte.getDirection());
                richtungMitte = (int) mitteOben.getDirection();
                Ort oben = mitteOben.getNachOrt();
                macOben = oben.getMACAddress();
            } catch (Exception e) {
                // sollte nicht passieren
            }
            try {
                idUnten = anlage.getHome().getId();
                Weg untenMitte = anlage.findWegStartingAt(anlage.getHome());
                Ort mitte = untenMitte.getNachOrt();
                idMitte = mitte.getId();
                Weg mitteOben = anlage.findWegStartingAt(mitte, untenMitte.getDirection());
                richtungMitte = (int) mitteOben.getDirection();
                Ort oben = mitteOben.getNachOrt();
                idOben = oben.getId();
            } catch (Exception e) {
                // sollte nicht passieren
            }
            // die Namen (zb MediaName um den Index zu holen
            mStatusElemente = new ArrayList();
            mStatusElemente.add("Akku Level");
            mStatusElemente.add("Akku Temp");
            mStatusElemente.add(idUnten+"CONNECT");
            mStatusElemente.add(idUnten+"LOST");
            mStatusElemente.add(idMitte+"CONNECT");
            mStatusElemente.add(idMitte+"LOST");
            mStatusElemente.add(idOben+"CONNECT");
            mStatusElemente.add(idOben+"LOST");
            mStatusElemente.add(macMitte+"CONNECTx");
            mStatusElemente.add(macMitte+"LOSTx");

            mStartLoopIndex = mStatusElemente.size();   // ab hier ist der mp3 Loop
            mStatusElemente.add("musik_talstation.mp3");

            mStatusElemente.add("story1.mp3");
            mStatusElemente.add("story2.mp3");

            mStatusElemente.add("musik_mittelstation.mp3");

            mStatusElemente.add("story3.mp3");
            mStatusElemente.add("story4.mp3");

            mStatusElemente.add("musik_bergstation.mp3");

            mStatusElemente.add("story5.mp3");
            mStatusElemente.add("story6.mp3");

            mStatusElemente.add("musik_mittelstation.mp3x");

            mStatusElemente.add("story7.mp3");
            mStatusElemente.add("story8.mp3");
        }

        if (mStatusNamen == null) {
            // Übersetzung der Namen die auf der Logmeldung angezeigt werden sollen
            mStatusNamen = new HashMap<String, String>();
            mStatusNamen.put("musik_talstation.mp3", "Unten");
            mStatusNamen.put("musik_mittelstation.mp3", "Mitte");
            mStatusNamen.put("musik_mittelstation.mp3x", "Mitte2");
            mStatusNamen.put("musik_bergstation.mp3", "Oben");
            mStatusNamen.put("story1.mp3", "story1");
            mStatusNamen.put("story2.mp3", "story2");
            mStatusNamen.put("story3.mp3", "story3");
            mStatusNamen.put("story4.mp3", "story4");
            mStatusNamen.put("story5.mp3", "story5");
            mStatusNamen.put("story6.mp3", "story6");
            mStatusNamen.put("story7.mp3", "story7");
            mStatusNamen.put("story8.mp3", "story8");
            mStatusNamen.put(idUnten+"CONNECT", "wifi-u");
            mStatusNamen.put(idMitte+"CONNECT", "wifi-m");
            mStatusNamen.put(idMitte+"CONNECTx", "wifi-m");
            mStatusNamen.put(idOben+"CONNECT", "wifi-o");
            mStatusNamen.put(idUnten+"LOST", "lost-u");
            mStatusNamen.put(idMitte+"LOST", "lost-m");
            mStatusNamen.put(idMitte+"LOSTx", "lost-m");
            mStatusNamen.put(idOben+"LOST", "lost-o");
            mStatusNamen.put("Akku Level", "Akku");
            mStatusNamen.put("Akku Temp", "Temp");
        }

        // initial die Status Liste mit den leeren Elementen aufbauen
        if (mStatusList == null) {
            mStatusList = new ArrayList<String>();
            for (String s : mStatusElemente) {
                String name = mStatusNamen.get(s);
                String text = String.format("%-6.6s %4.4sm %8.8s:%10.10s%n", name, "----", "--:--:--", "x--------x");
                //mStatusMsg.append(text);
                mStatusList.add(text);
            }
        }

        // der Name der angezeigt werden soll
        // macMitte rauf  name=wifi-m
        // macMitte runter
        // den Anzeigenamen holen
        String name = mStatusNamen.get(mediaName);
        if (name != null) {
            if (name.equals("Mitte") || name.contains("-m")) {
                if (ps.getLetzterWeg() != null && ps.getLetzterWeg() instanceof Weg) {
                    try {
                        Weg letzterWeg = ps.getLetzterWeg();
                        if (letzterWeg != null) {
                            writeLog(4, "Status " + name + " letzterWeg:" + letzterWeg.getName());
                            // wenn wir noch auf dem Weg sind, dann ist letzterOrt noch der vorletzte
                            if (letzterWeg instanceof Weg) {
                                writeLog(4, "letzterOrt ist Weg:" + letzterWeg.getName());
                                //letzterWeg = (Weg) letzterWeg;
                            }
                            try {
                                // bei landing ist der letzte Weg immer noch der vorletzte Weg
                                if (letzterWeg.getVonOrt() != anlage.getHome() && letzterWeg.getNachOrt() != anlage.getHome()) {
                                    // wir fahren runter, daher x anhängen
                                    // mediaName = macMitteCONNECTx
                                    mediaName = mediaName + "x";
                                    // name=wifi-m
                                    name = mStatusNamen.get(mediaName);
                                }
                            } catch (Exception e) {
                                writeLog(4, "Status Fehler Mitte mit x:" + e.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        writeLog(4, "Status Fehler Mitte mit x1:" + ex.getMessage());
                    }
                }
            }
        }
        if (name != null) {
            String output = String.format("%-6.6s %4.4sm %8.8s:%10.10s%n", name, ps.getDistHome(), ps.getNowTime(), value);

            int index = mStatusElemente.indexOf(mediaName);
            // falls der Name gefunden wird, anzeigen
            if (index >= 0) {
                //int i = index * output.length();
                //mStatusMsg.replace(i, i + output.length() - 1, output);
                mStatusList.set(index, output);
                // nur ein Medium hat ein Folgemedium. dh nach einer wlan action wird nichts gelöscht.
                if (mediaName.contains(".mp3")) {
                    if (index < mStatusElemente.size() - 1) {
                        //i = i + output.length();
                        index++;
                    } else {
                        //i = 0;
                        index = mStartLoopIndex;
                    }
                    try {
                        String nextMediaName = mStatusElemente.get(index);
                        String nextName = mStatusNamen.get(nextMediaName);
                        String empty = String.format("%-6.6s %4.4s  %8.8s %10.10s%n", nextName, "    ", "  hier? ", "          ");
                        //mStatusMsg.replace(i, i + empty.length(), empty);
                        mStatusList.set(index, empty);
                    } catch (Exception e) {
                        writeLog(4, "Status Fehler NextName:"+e.getMessage());
                    }
                }
                //writeLog(7, mStatusMsg.toString());
                //writeLog(7, mStatusList.toString());
                try {
                    List<String> msgList = new ArrayList<String>();
                    msgList.add(mStatusList.toString());
                    msgList.add(statusAsString(mStatusList));
                    writeLog(7, msgList);
                } catch (Exception e) {
                    writeLog(4, "Status Fehler:"+e.getMessage());
                }
            }
        }
    }

    /*********************
       Der eventloop wird entweder von GPS Callback oder vom Simulationscallback angestossen. im Schnitt jede Sekunde einmal
     */

    public void eventLoop(PlayerStatus ps) {
        long now = System.currentTimeMillis();
        ps.setNowDateTime(DateFormat.getDateTimeInstance().format(new Date()));
        ps.setNow(now);

        writeLog(3, "loop: "+ps.getNowTime());
        // TODO: der Location Abfrage Takt könnte langsamer sein, wenn wir weit vom nächsten Ereignis weg sind.
        ps.setNextGPSCheck(now);
        // ====================================================
        // wir fahren, prüfen, ob wir aus einem Notstopp kommen
        if (ps.getSpeed() >= ACSettings.getInstance().getGpsMinSpeed() || ps.getSpeedCalc() >= ACSettings.getInstance().getGpsMinSpeed()) {
            if (ps.isNotStop()) {
                float dauer = (now - ps.getStartStillstand()) / 1000;  // in sekunden
                // wenn wir im Notstop waren, dann muss jetzt player oder background player wieder starten
                writeLog(4, ps.getNowTime()+" Start nach Notstop nach (s)"+dauer);
                ps.setNotStop(false);
                if (currentPlayer != null) {
                    writeLog(4, "Player starten");
                    try {
                        currentPlayer.start();
                        currentPlayer = null;
                    } catch (Exception e) {
                        writeLog(3, "Player Start Crashed:"+e.getMessage());
                    }
                }
            }
            // wenn wir uns bewegen, sicherstellen, dass der Stillstandtimmer auf 0 steht
            ps.setStartStillstand(0);
            ps.setDauerBisNotstop(0);
        } else {
            // TODO: prüfen ob wir uns nicht doch bewegen, distanz zwischen den letzten 4 Positionen
            // wenn wir uns bewegen,
            //ps.setStartStillstand(0);
            //ps.setDauerBisNotstop(0);
            // sonst stillstand weiter bearbeiten

            // ====================================================
            // notstop machen wir nur auf einem Weg
            if (ps.getOrt() != null && ps.getOrt() instanceof Weg) {
                if (ps.getStartStillstand() > 0) {
                    if (!ps.isNotStop()) writeLog(4, ps.getNowTime()+" stehen, S: "+Math.round(ps.getSpeed())+"< " + ACSettings.getInstance().getGpsMinSpeed());
                    // wenn wir zulange stehen, stoppen wir alles und springen raus
                    float dauer = (now - ps.getStartStillstand()) / 1000;  // in sekunden
                    if (dauer > anlage.getMaxStillstand()) {
                        writeLog(3, "Im Notstop seit (s):"+dauer);
                        // nur machen, wenn wir nicht schon im Stillstand sind
                        if (!ps.isNotStop()) {
                            ps.setNotStop(true);
                            writeLog(4, ps.getNowTime() + " Wir starten mit dem Notstop");
                            ps.setDauerBisNotstop(0);
                            //wir müssen den Sound auf Pause stellen und merken wer gespielt hat
                            if (mediaPlayer != null) {
                                if (mediaPlayer.isPlaying()) {
                                    currentPlayer = mediaPlayer;
                                    try {
                                        mediaPlayer.pause();
                                    } catch (Exception e) {
                                        writeLog(4, "Player crashed:" + e.getMessage());
                                    }
                                }
                            }
                            if (mBackgroundMediaPlayer != null) {
                                try {
                                    if (mBackgroundMediaPlayer.isPlaying()) {
                                        currentPlayer = mBackgroundMediaPlayer;
                                        mBackgroundMediaPlayer.pause();
                                        ps.setBackgroundIsPause(true);
                                    }
                                } catch (Exception e) {
                                    // NOTE: isPlaying() can potentially throw an exception and crash the application
                                    writeLog(4, "Background player crashed:" + e.getMessage());
                                    currentPlayer = mBackgroundMediaPlayer;
                                    mBackgroundMediaPlayer.pause();
                                    ps.setBackgroundIsPause(true);
                                }
                            }
                        }
                        // blockieren macht keinen Sinn, weil GPS den Prozess immr wieder started
                        // wir blockieren alles für eine Sekunde
                        /*
                        try {
                            writeLog(4, "sleep 0.5s");
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        */
                        // ----------------------------------------
                        // wir sind im NotSTop und müssen warten, alles ist jetzt still.
                        return;
                    } else {
                        dauer = (now - ps.getStartStillstand()) / 1000;
                        ps.setDauerBisNotstop((long) (anlage.getMaxStillstand() - dauer));
                    }
                }
            }
        }

        // ====================================================
        // das Ende einer Durchsage ist erreicht, wir machen wieder Background Musik
        if (ps.getEreignisEnde() > 0 && ps.getEreignisEnde() < now) {
            //TODO bei Looping müsste man hier die aktuelle Story wieder starten
            // Nur wenn sie vorher auch gelaufen ist oder
            if (ps.isBackgroundIsPause()) {
                String ereignisName = "???";
                Ereignis er = ps.getCurrentEreignis();
                if (er != null) {
                    ereignisName = er.getName();
                }
                writeLog(3, "Ende Ansage "+ereignisName+" starten Background");
                startBackgroundMusic();
            }
        }

        // ====================================================
        // wenn gerade eine Ansage läuft, springen wir raus
        // das geht bei sehr langen Ansagen nicht, die Ansage
        // TODO: während der Durchsage verarbeiten wir die GPS Positionen nicht, sie darf nicht lange sein
        if (ps.getCurrentEreignis() != null) {
            Ereignis e = ps.getCurrentEreignis();
            if (e != null) {
                int actionType = e.getActionType();
                if (actionType == Aktion.ANSAGE || actionType == Aktion.GOODBY || actionType == Aktion.HELLO || actionType == Aktion.ALERT) {
                    if (ps.getEreignisEnde() > 0 && ps.getEreignisEnde() > now) {
                        writeLog(3, "Durchsage: "+ e.getName()+" warten");
                        return;
                    }
                }
            }
        }

        Ort myPos = new Ort(ps.getLatitude(), ps.getLongitude()); // die aktuelle Position die ich habe
        // ====================================================
        // wenn ich auf einer Journey bin (Weg oder Ort
        // bin ich beim warten am Startort auf einer Journey?
        if (ps.getStatus() == PlayerStatus.STATUS_JOURNEY) {
            int iSpeedMS = Math.round(ps.getSpeed());
            writeLog(2, "speed:"+iSpeedMS+"m/s");
            if (ps.getNextGPSCheck() == now) {  // sonst stehen wir immer noch am selben Ort
                IrgendWo letzterPlatz = ps.getOrt();
                writeLog(3, "GPS n"+String.format("%.3f", ps.getLatitude())+":"+String.format("%.3f", ps.getLongitude()));
                //TODO das kann falsch sein
                // in der Station ist Position und Richtung falsch oder unbekannt
                // ich war bis jetzt auf einem Weg
                // ====================================================
                if (letzterPlatz instanceof Weg) {

                    Weg jetzigerWeg = (Weg) letzterPlatz;
                    Ort zielOrt = jetzigerWeg.getNachOrt();
                    writeLog(3, "Weg:"+letzterPlatz.getName());
                    double dDistPosStart = myPos.calculateDistance(jetzigerWeg.getVonOrt().getLatitude(), jetzigerWeg.getVonOrt().getLongitude());
                    ps.setAbstandStart(dDistPosStart);

                    double dDistPos = myPos.calculateDistance(zielOrt.getLatitude(), zielOrt.getLongitude());
                    ps.setAbstandZiel(dDistPos);
                    writeLog(3, "Dist Ziel:"+dDistPos+" m");
                    if (ps.isWegneu()) {
                        // --------------------------------------
                        // bin ich gerade auf dem Weg losgefahren?
                        // status Infos rausschreiben
                        writeLog(4, "Speed:"+ps.getSpeed());
                        //writeLog(4, "Satelites"+ps.getSatelites());
                        writeLog(4, "GPS Source:"+ps.getGPSFrom());
                        writeLog(4, "Provider:"+ps.getProvider());
                        writeLog(4, "WegSpeed:"+ps.getWegSpeed());

                        writeLog(3, "Weg neu");
                        if (jetzigerWeg.getBackgroundMediaId() != 0) {
                            writeLog(3, "Background ersetzen: "+jetzigerWeg.getBackgroundMediaId());
                            replaceBackgroundPlayer(jetzigerWeg.getBackgroundMediaId());
                        }
                        Ereignis e = ps.sucheStoryEreignis(jetzigerWeg);
                        if (e!= null) {
                            writeLog(4, ps.getNowTime()+" Ereignis: " + e.getName());
                        }
                        // rein theoretisch ist der letzte Ort ein Ort, wenn wir auf einem neuen Weg sind
                        // wenn wir auf einem neuen Weg sind und wegen dem delayMeters das Ereignis nicht
                        // gestartet haben, könnte der letzteOrt der Wegs ein, oder?
                        // der letzteOrt ist immer ein Ort, nie ein Weg.
                        Ort ortVorher = null;
                        if (ps.getLetzterOrt() instanceof Ort) {
                            ortVorher = (Ort) ps.getLetzterOrt();
                        }
                        if (ortVorher != null) {
                            if (e != null) {
                                if (dDistPos > ortVorher.getRadius() + e.getStartDelayMeters()) {
                                    // sound auf neuem Weg abspielen, achtung warten bis Begrüssung fertig ist
                                    ps.setWegneu(false);
                                    // Abfahrt heisst ich fahre auf dem Weg los.
                                    executeEreignis(ps, now, e);
                                }
                            } else {
                                // der Weg hat kein Ereignis, als abgehandelt vermerken
                                ps.setWegneu(false);
                                writeLog(3, "Weg hat kein Ereignis");
                            }
                        } else {
                            writeLog(3, "letzter Ort: " + ps.getLetzterOrt().getName() + " ist kein Ort");
                        }
                    }  //else { // ich bin schon auf dem Weg unterwegs
                    // bin ich am Zielort angekommen?
                    // Done:ich kann aus zwei Richtungen am Ort ankommen, (Mittelstation von oben oder unten)
                    // ich sehe auf dem Weg OrtVon und OrtNach, woher ich komme
                    //if (dDistPos < zielOrt.getRadius()) {
                    // --------------------------------------
                    // Auf Weg, aber nicht am Landen
                    if (ps.isLanding() == false) {
                        if (dDistPos < jetzigerWeg.getRadiusNachOrt()) {
                           writeLog(4, ps.getNowTime()+" Landing now Dist: " + dDistPos);
                            if (zielOrt.getBackgroundMediaId() != 0) {
                                writeLog(3, "new Background");
                                replaceBackgroundPlayer(zielOrt.getBackgroundMediaId());
                            }
                            // OrtMitte ANKUNFT runter2
                            Ereignis e = ps.sucheAnkunftsEreignis(jetzigerWeg);  // ACTION Type = GoodBY
                            executeEreignis(ps, now, e);   // verabschieden  // Logmeldung wenn nicht gefunden
                            ps.setLanding(true);
                        }
                    }
                    // --------------------------------------
                    // auf Weg und jetzt am Ort angekommen, ab jetzt wird GPS Signal
                    // oder wifi Ort erkannt
                    if (dDistPos < zielOrt.getRadius()) {

                        ortAngekommen(ps, now);

                    } else {
                        // ich bin irgendwo auf dem Weg, nicht nahe beim Start oder Ziel
                        // TODO: Ereignisse können auch in beliebigem Abstand auf einem Weg auftauchen
                        if (ps.getSpeed() < 0.5) {
                            long dauer = now-ps.getStartStillstand()/1000;
                            writeLog(3, "stehe "+dauer+" s still");
                            // wir stehen still, wir merken uns wann das begonnen hat
                            if (ps.getStartStillstand() == 0) {
                                ps.setStartStillstand(now);
                            }
                        }
                    }
                    // bin ich auf dem richtigen Weg?  Nur wenn wir auch eine Richtung haben, sonst macht er in jeder Station Restart
                    // Zwischen 200 und 300m muss die Richtung stimmen
                    // -----------------------------------------
                    if (ps.getDirection() > 0) {
                        if (dDistPosStart > 200 && dDistPosStart < 300) {
                            // TODO nur machen wenn wir mehrere solcher Signale hintereinander haben
                            if (Anlage.isIn360(jetzigerWeg.getDirection(), ps.getDirection(), 45)) {
                                // dann ist alles ok;
                                countWrongDirection = 0;
                            } else {
                                countWrongDirection++;
                                writeLog(4, ps.getNowTime() + " Dir Weg:" + jetzigerWeg.getDirection() + " Dir Player:" + ps.getDirection() + " DistStart:" + dDistPosStart);
                                if (countWrongDirection > 4) {
                                    // wir sind auf dem falschen Weg, Fahrtrichtung stimmt nicht mit Weg Richtung überein
                                    restartGPS();
                                }
                            }
                        }
                    }
                    if (dDistPosStart < ACSettings.getInstance().getDistLogDist() && (ps.getSpeed() > 0.4 || ps.getSpeedCalc() > 0.4 )) {
                        writeLog(4, "A:"+dDistPosStart+"m, D:"+ps.getDirection()+((ps.getSpeed()>0)?" S:"+Math.round(ps.getSpeed()):" SC:"+ps.getSpeedCalc()));
                    }
                    // an einer bestimmten Stelle im der Story messen wir die Dezibels
                    if (mDBTracking) {
                        if (ps.getCurrentEreignis().getStart() < ps.getNow() && ps.getCurrentEreignis().getEnde() > ps.getNow()) {
                            // dezibel messen
                            //mHandler.postDelayed(mUpdateTimer, 300L);
                            // oder
                            writeDB();
                        }
                    }

                } else {  // ich bin in einem Ort
                    // ====================================================
                    writeLog(3, "Ort: "+letzterPlatz.getName());
                    // solage wir keine Geschwindigkeit haben, macht es keinen Sinn die GPS Daten zu verwerten
                    // V1.33 calcSpeed wird auch berücksichtig, wenn speed = 0
                    if (ps.getSpeed() >= ACSettings.getInstance().getGpsMinSpeed() || calcSpeed() >= ACSettings.getInstance().getGpsMinSpeed()) {
                        Ort jetzigerOrt = (Ort) letzterPlatz;

                        // was ist letzter Ort, wenn ich genau in der Mittelstation starte?
                        double dDistPos = myPos.calculateDistance(jetzigerOrt.getLatitude(), jetzigerOrt.getLongitude());

                        // wenn ich an einem Ort bin und der Ort war der Zielort auf dem Weg, dann muss ich die Distanz zum Zielort nachführen.
                        if (ps.getZielOrt() != null && ps.getZielOrt() == jetzigerOrt) {
                            ps.setAbstandZiel(dDistPos);
                            // das ist der Abstand von meiner Position zum aktuellen Ort, nicht zum Ziel
                        }
                        writeLog(3, dDistPos + "m Dist to: " + jetzigerOrt.getName());
                        // habe ich den Ort gerade verlassen?
                        // vor dem Verlassen des Ortes wollen wir die Ankunftszeit für den nächsten Ort melden
                        // in diesem Moment sollten wir Internet Connection haben
                        // das kann auch auf der Hinfahrt sein, weil nachOrt Radius != OrtRadius
                        if (dDistPos > jetzigerOrt.getRadius()) {
                            Weg naechsterWeg = anlage.findWegStartingAt(jetzigerOrt, ps.getDirection());
                            if (naechsterWeg != null) {
                                Ort zielOrt = naechsterWeg.getNachOrt();
                                if (zielOrt != null) {
                                    //TODO besser wäre duchschnittlichen Speed zu nehmen
                                    double dDistToZiel = zielOrt.calculateDistance(jetzigerOrt.getLatitude(), jetzigerOrt.getLongitude());
                                    writeArrivalInfo(ps.getSpeed(), zielOrt.getName(), dDistToZiel);
                                    ps.setAbstandZiel(dDistToZiel);
                                }
                            } else {
                                // 1.16 kein Weg gefunden
                                writeLog(4, "Weg notfound Dir:" + ps.getDirection() + " Dist:" + dDistPos+" von:"+letzterPlatz.getName());
                                if (dDistPos > ACSettings.getInstance().getRestartDistanceM()) {
                                    writeLog(4, "Dist:" + dDistPos + ">" + ACSettings.getInstance().getRestartDistanceM());
                                    restartGPS();
                                }
                            }
                        }
                        // TODO Distanz beim Verlassen ist nicht dieselbe wie beim Ankommen
                        // wenn ich angekommen bin, wird ort = Ziel Ort.
                        // wenn die Distanz im nächsten Loop etwas grösser ist als der Radius, dann glaube ich ich habe den Ort wieder verlassen
                        // TODO mindestens 1 Minute warten nach den Ankunft
                        // TODO Problem: Talstation wackeliges WLAN, WLAN wechselt ständig und GPS springt
                        if (dDistPos > jetzigerOrt.getRadius()) {
                            // TODO: V1.38 verlässt Ort fälschlicherweise aufgrund einzelner falscher GPS Positionen im Ort
                            // brauche 3 GPS-Ticks mit zunehmender Distanz
                            writeLog(4, "GPS w "+String.format("%.3f", ps.getLatitude())+":"+String.format("%.3f", ps.getLongitude()));
                            ortVerlassen(ps, now);
                        }
                    } else {
                        // ------------------------------------------
                        writeLog(4, "amOrt,"+ ((ps.getSpeed()>0)?"S:"+Math.round(ps.getSpeed()):"SC:"+Math.round(ps.getSpeedCalc()))+" wifi:"+ps.getWifiLevel());

                        //writeLog(3, "noch: " + jetzigerOrt.getName());
                        if ("connected to WIFI".equals(ps.getWlanStatus())) {

                        } else {
                            // wenn wir kein WIFI haben, versuchen zum preffered wifi zu connecten
                            // TODO am liebsten ca 10 Sekunden nach Ankunft
                            if (mPreferredNetworkID != 0) {
                                if (mWifiManager != null) {
                                    mWifiManager.enableNetwork(mPreferredNetworkID, true);
                                    mWifiManager.reconnect();
                                    writeLog(3, "try connect");
                                }
                            }
                            // am ort aber kein wifi heisst, wir sind kurz davor oder wir sind weg und wissen es nicht
                            // wenn wir weg sind ist der neue Weg, der Weg, der auf den folgt, der beim ort endete und den Ort als Startort hat
                        }

                    }
                }
            } else {  // schauen ob wir eine Ereigniskette am selben Ort haben
                if (ps.getEreignisEnde() > now) { // das aktuelle Ereignis ist zuende
                    // TODO: ereigniskette abarbeiten
                }

            }
        } else { // Journey suchen
            writeLog(4,"noch keine Journey, suche den nächsten Ort");
            Ort nearestOrt = anlage.InitalPositionFinden(ps.getLatitude(), ps.getLongitude(), ps.getDirection());

            double dDistPos = 99999999;
            if (nearestOrt != null) {
                writeLog(4, "Der nächstgelegene Ort :"+nearestOrt.getName());
                ps.setLanding(false);
                dDistPos = myPos.calculateDistance(nearestOrt.getLatitude(), nearestOrt.getLongitude());
                // TODO: wenn distance kleiner Radius, dann haben wir einen Ort und beginnen die Journey
                // oder wäre ein weg besser?
                // wenn ich den Ort habe, muss ich eine Journey erzeugen und am ps anhängen
                if (dDistPos <= nearestOrt.getRadius()) {
                    writeLog(4, "bin am Ort, Dist: "+dDistPos+"m");
                    ps.setStatus(PlayerStatus.STATUS_JOURNEY);
                    ps.setOrt(nearestOrt);
                    ps.setInitDone(false);

                    // TODO: was wenn eine Anlage mehrere Journies hat?
                    journey = anlage.createNewJourney();
                    ps.setJourney(journey);
                    writeLog(4, "Startort gefunden :"+nearestOrt.getName());

                } else {
                    // V1.23 auf Weg,
                    writeLog(4, "nirgendwo, Dist: "+dDistPos+"m");
                    // ich bin irgendwo, oder auf einem Weg. Ich sollte nur suchen, wenn ich einen Speed habe
                    // sonst sind wir auf dem Weg oder im nirvana
                    if (ps.getSpeed() > ACSettings.getInstance().getGpsMinSpeed()) {
                        if (ps.getDirection() == 0) {
                            float dir = nearestOrt.calculateDirection(myPos);
                            ps.setDirection(dir);
                            writeLog(3, "berechnete Richtung = " + dir);
                        }
                        // a) nearset ort ist vor mir: Weg Ending at ort, b) nearest Ort ist hinter mir, starting at Ort
                        Weg neuerWeg = anlage.findWegStartingAt(nearestOrt, ps.getDirection());
                        if (neuerWeg == null) {
                            neuerWeg = anlage.findWegEndingAt(nearestOrt, ps.getDirection());
                            if (neuerWeg != null) {
                                writeLog(4, "Weg gefunden, der am nächsten Ort endet. Weg: " + neuerWeg.getName());
                            } else {
                                writeLog(4, "keinen Weg vom nächsten Ort in Richtung: " + ps.getDirection());
                            }

                        } else {
                            writeLog(4, "Weg gefunden, der "+nearestOrt.getName()+" startet. Weg: " + neuerWeg.getName());
                        }

                        // findet in Sargans den Weg zum Speckhaus unabhängig von der Distanz, nur wegen der Richtung.
                        // spielt für die Bahn keine Rolle, aber für Fussgänger.
                        // könnte man anfangen, wenn man den längsten Weg kennt.
                        //TODO wenn wir in der Station sind
                        if (neuerWeg != null) {
                            writeLog(3, "Weg gefunden aufgrund der Richtung: " + neuerWeg.getName());
                            ps.setStatus(PlayerStatus.STATUS_JOURNEY);
                            ps.setLetzterOrt(neuerWeg.getVonOrt());  // letzterOrt ist jetzt der StartOrt
                            ps.setOrt(neuerWeg);
                            ps.setZielOrt(neuerWeg.getNachOrt());
                            ps.setInitDone(false);
                            // TODO: was wenn eine Anlage mehrere Journies hat?
                            journey = anlage.createNewJourney();
                            ps.setJourney(journey);
                            // TODO was bedeuten diese 30?
                            // auf den ersten 300 Metern, starten wir mit der Begrüssung auf dem Weg, dh wegneu=true
                            if (dDistPos < 500) {
                                ps.setWegneu(true);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean elapsed(long startTime, long elapsedSec) {
        return System.currentTimeMillis() > (startTime + (1000 * elapsedSec));
    }

    private synchronized void ortVerlassen(PlayerStatus ps, long now) {
        // TODO V1.49 verlassen geht frühestens 1 Minuten nach der Ankunft.
        // ortVerlassen wird auch bei WLAN Verlust aufgerufen, das kann aber zu früh sein oder ein wackeliges WLAN
        long elapsed = ACSettings.getInstance().getMinVerweilzeitSec();
        if (ps.getAngekommenUm() > 0 && elapsed(ps.getAngekommenUm(), ACSettings.getInstance().getMinVerweilzeitSec())) {
            IrgendWo letzterPlatz = ps.getOrt();
            if (letzterPlatz instanceof Ort) {
                Ort jetzigerOrt = (Ort) letzterPlatz;
                Ort myPos = new Ort(ps.getLatitude(), ps.getLongitude());
                // was ist letzter Ort, wenn ich genau in der Mittelstation starte?
                double dDistPos = myPos.calculateDistance(jetzigerOrt.getLatitude(), jetzigerOrt.getLongitude());

                if (!ps.isOrtVerlassen()) {
                    writeLog(4, ps.getNowTime() + " Ort verlassen");
                }
                ps.setOrtVerlassen(true);
                // in der Mitte gibt das zwei Weg, die Richtung bestimmt auf welchem Weg in bin

                // wenn ich keine Richtung habe, berechne ich die Richtung vom letztenOrt zu meiner Position
                if (ps.getDirection() == 0) {
                    //TODO wenn ich keine Direction habe, direction berechnen von Ort zu myPos
                    float dir = jetzigerOrt.calculateDirection(myPos);
                    //TODO die berechnete Richtung stimmt nicht mit den gemessenen Richtungen überein
                    //ps.setDirection(dir);
                    writeLog(4, "Dir==0, calc: " + dir);
                }
                // jetzizer Ort = Wangs unten und letzterWeg=null (das erste mal unterwegs)
                if (jetzigerOrt == anlage.getHome()) {
                    ps.setLetzterWeg(anlage.findWegEndingAt(anlage.getHome()));
                }

                // ab V1.26 kennt ein Weg seinen nächsten Weg
                Weg neuerWeg = null;
                Weg letzterWeg = (Weg) ps.getLetzterWeg();
                if (letzterWeg != null) {
                    neuerWeg = letzterWeg.getNextWeg();

                }
                // auf die alte Art den Weg gemäss Richtung suchen
                if (neuerWeg == null) {
                    neuerWeg = anlage.findWegStartingAt(jetzigerOrt, ps.getDirection());
                    writeLog(4, "Weg dank Richtung?");
                } else {
                    writeLog(4, "Weg dank Model");
                }

                if (neuerWeg != null) {
                    ps.setOrtVerlassen(false);
                    // habe ich mich schon verabschiedet?
                    writeLog(4, "neuer Weg: " + neuerWeg.getName() + " Dist:" + dDistPos + " Dir:" + ps.getDirection() + " S:" + ps.getSpeed());
                    Ereignis e = ps.sucheAbfahrtsEreignis(neuerWeg);
                    if (e != null) {
                        executeEreignis(ps, now, e);
                        // warten bis Ereignis fertig ist, falls es gespielt wurde
                        writeLog(3, "Ereignis: " + e.getName());
                    }
                    // warten weil zuerst die Begrüssung laufen muss
                    //e = ps.sucheEreignis(neuerWeg, Aktion.ABFAHRT);
                    //executeEreignis(ps, now, e);
                    // TODO: ende setzen = now + dauer
                    // TODO Ereignis starten
                    // auf welchem Weg bin ich jetzt?
                    // TODO: dafür sollte ich etwa 10 Sekunden warten
                    // ich bin jetzt auf einem neuen Weg oder im Nirvana
                    ps.setLetzterOrt(ps.getOrt());  // den aktuellen Ort speichern, wegen Startverzögerung der Action auf dem Weg
                    ps.setOrt(neuerWeg);
                    ps.setWegneu(true);
                    ps.setZielOrt(((Weg) ps.getOrt()).getNachOrt());
                    ps.setAbgefahrenUm(now);  // an einem Ort abgefahren = Weg gestartet
                    ps.setAngekommenUm(0);

                } else {
                    writeLog(4, "fort vom Ort, kein Weg von: " + jetzigerOrt.getName());
                    writeLog(4, "AO:" + dDistPos + "m, D:" + ps.getDirection() + " S:" + ps.getSpeed());
                    // TODO:soll ich hier versuchen den InitialOrt zu finden, der findet auch Wege
                    // müsste eigentlich mit der nächsten GPS Location wieder kommen und einen besseren Wert haben
                }
            } else {
                if (letzterPlatz != null) {
                    writeLog(4, "war schon weg vom Ort: " + letzterPlatz.getName());
                } else {
                    writeLog(4, "war schon weg vom Ort: ");
                }
            }
        } else {
            writeLog(4, ps.getNowTime()+" erst hier seit:"+(System.currentTimeMillis()-ps.getAngekommenUm())/1000+ "sec");
        }
    }

    /*
      Wir waren auf einem Weg und sind jetzt an der Ortsgrenze oder wir haben Wifi Signal
      Darf nur laufen, wenn wir wiklich noch auf dem Weg sind.
      TODO wenn der Ort aufgrund eines WLAN interrupts gesetzt wird, muss der aktuelle Event Loop abgebrochen werden
     */
    private synchronized void ortAngekommen(PlayerStatus ps, long now) {
        IrgendWo hier = ps.getOrt();    // Weg oder Ort, wo wir beim letzten GPS Tick waren
        if (hier instanceof Weg) {
            Weg jetzigerWeg = (Weg) hier;
            Ort zielOrt = jetzigerWeg.getNachOrt();
            //TODO wenn der Weg falsch war, ist jetzt auch der Nachort falsch.
            // wenn der Weg falsch ist, werde ich den neuen Weg am Ort nicht finden
            ps.setLetzterOrt(hier);  // letzterOrt ist jetzt ein Weg
            ps.setLetzterWeg(jetzigerWeg);
            if (ps.getZielOrt() != null) {
                ps.setOrt(ps.getZielOrt());   // Zielort
            } else {
                ps.setZielOrt(jetzigerWeg.getNachOrt());
            }
            writeLog(4, "\n" + ps.getNowDateTime() + " Ort: " + ps.getOrt() +
                    "\nGPS:" + String.format("%.6f", ps.getLatitude()) + ", " + String.format("%.6f", ps.getLongitude()));

            ps.setInitDone(false);
            ps.setAngekommenUm(now);   // an einem Ort angekommen
            ps.setAbgefahrenUm(0);
            ps.setLanding(false);
            // falls Distanz Landen < als Radius Ort, dann muss Begrüssung beim Ort kommen
            Ereignis e = ps.sucheAnkunftsEreignis(jetzigerWeg);  // ACTION Type = GoodBY
            // das wird gestartet, aber im GPS Log ist dafür kein Eintrag.
            executeEreignis(ps, now, e);   // verabschieden  // Logmeldung wenn nicht gefunden
            // wird auch in display erkannnt
            //if (ps.isGPSTracking()) {
            //    mLogService.writeGPSLocation(ps.getLatitude(),ps.getLongitude(), ps.getSpeed(),ps.getDirection(), ps.getWifiLevel(), 0, ps.getOrt().getName());
            //}
            //TODO 0.95: soll man hier schauen, ob es eine Story für den Ort gibt?
            //Ereignis e = ps.sucheStoryEreignis(zielOrt);  // ACTION Type = STORY
            //executeEreignis(ps, now, e);   // verabschieden  // Logmeldung wenn nicht gefunden
            // im Testmode haben wir kein WLAN on/off

            // wir beginnen eine neue Runde
            // TODO: Wenn ich am Zielort stehen bleibe wird ständig eine neue Journey angelegt und wieder begrüsst
            if (zielOrt == anlage.getHome()) {
                writeLog(4, "am Startort");
                journey = anlage.createNewJourney();
                ps.setJourney(journey);
                writeStatusToLongLog();   // V1.46
                //writeLnLog(zielOrt.getName());   // Log auf Airtable schreiben
                //if (testOn.equals("0")) {
                // bei home wird der ganze Track geschrieben und gelöscht
                // im Testmode haben wir kein WLAN on/off
                if (testOn.equals("1")) {
                    if (ps.isGPSTracking()) {
                        mGPSTrackService.writelnGPSTrack(anlage.getHome());
                    }
                }
            } else {
                // bei jeder Station wird ein Teil Track geschrieben
                // das Ereignis ist gestartet, aber noch nicht im GPS Log drinn.
                if (testOn.equals("1")) {
                    if (ps.isGPSTracking()) {
                        String eMediaName = "";
                        Ereignis currentEreignis = ps.getCurrentEreignis();
                        if (currentEreignis != null) {
                            eMediaName = Data.getInstance().getMediaName(currentEreignis.getMediaId());
                        }
                        //long now = System.currentTimeMillis();
                        int deltaTms = (int) (now - ps.getNow());
                        mLogService.writeGPSLocation(ps.getLatitude(), ps.getLongitude(), ps.getSpeed(), ps.getDirection(), ps.getWifiLevel(), deltaTms, eMediaName, ps.getOrt(), ps.getAccuracy());
                        IrgendWo ort = ps.getOrt();
                        if (ort instanceof Ort) {
                            mGPSTrackService.writeGPSTrack((Ort) ps.getOrt());
                        }
                    }
                }
            }
        }
    }

    /*
     * Ein Ereignis ist eine Erweiterung von Aktion
     */
    private void executeEreignis(PlayerStatus ps, long now, Ereignis e) {
        /* geht nicht, weil damit auch der laafende player blockiert wird
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                long waittime = now + e.getMinimalDuration() - ps.getEreignisEnde();
                if (waittime > 0) {
                    try {
                        Thread.sleep(waittime);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }x
                }
            }
        } */
        // TODO: Hintergrund Musik unterbrechen (easy)
        // ich habe ein Ereignis
        if (e != null) {
            if (!e.isDone()) {
                // TODO nur wenn sie nicht schon gestoppt ist
                pauseBackgroundMusic();
                writeLog(3, "starte mediaId: "+e.getMediaId());
                if (e.isVolumeOn()) {
                    CompanionAudioService.getInstance().setLoud();
                }

                // Lautstärke für einzelnes Stück anpassen
                CompanionAudioService.getInstance().changeVolumeTemp(e.getDeltaVolume());

                play(e.getMediaId());
                if (mediaPlayer != null) {
                    e.setStart(ps.getNow());
                    e.setEnde(ps.getNow()+mediaPlayer.getDuration());
                }

                ps.setEreignisEnde(now + getMediaDuration());
                ps.setLastEreignis(ps.getCurrentEreignis());
                ps.setCurrentEreignis(e);
                // wenn ein neues Mediafile kommt, in den db Log schreiben
                if (mDBTracking) {
                    writeLog(6,ps.getCurrentMediaName()+",since Start Dezibel");
                }
                e.setDone(true);
                writeLog(4, ps.getNowTime()+" start T: "+e.getActionType()+ " N: " + ps.getCurrentMediaName() + " id=" + e.getMediaId());
                writeStatus(ps.getCurrentMediaName(),"started");
            }
        }
    }

    /*
     *  die Background Musik wird durch eine andere ersetzt
     */
    private void replaceBackgroundPlayer(int backgroundMediaId) {

        if (mBackgroundMediaPlayer != null) {
            // sehr wahrscheinlich ist er gerade am spielen
            mBackgroundMediaPlayer.pause();
            //if (isFinishing()) {
                mBackgroundMediaPlayer.stop();
                mBackgroundMediaPlayer.release();
                mBackgroundMediaPlayer = null;
            //}
            mBackgroundMediaId = backgroundMediaId;
            startBackgroundMusic();
        }
    }


    // die Background Musik läuft auf einem eigenen Player
    private void startBackgroundMusic() {

        CompanionAudioService.getInstance().changeVolumeTemp(0);
        ps.setVolume(CompanionAudioService.getInstance().getVolume());

        if (mBackgroundMediaPlayer == null) {
            mBackgroundMediaPlayer = MediaPlayer.create(this, mBackgroundMediaId);
            mBackgroundMediaPlayer.setLooping(true);
        }
        if (mediaPlayer == null) {
            mBackgroundMediaPlayer.start();
            //mBackgroundMediaPlayer.setVolume(ps.getVolume(), ps.getVolume());
            ps.setBackgroundIsPause(false);
            try {
                ps.setCurrentMediaName(Data.getInstance().getMediaName(mBackgroundMediaId));
                writeLog(4, ps.getNowTime()+" (1)start Background id:"+mBackgroundMediaId + " " +ps.getCurrentMediaName());
                writeStatus(ps.getCurrentMediaName(), "started");
            } catch (Exception e) {
                Log.d(TAG, "kenne Namen von MediaId:"+mBackgroundMediaId+"nicht");
            }

            // TODO isPlaying kann crashen wenn er nicht spielt
        } else {
            try {
                if (!mediaPlayer.isPlaying()) {
                    Log.d(TAG, "startBackgroundMusic:"+mBackgroundMediaId);

                    mBackgroundMediaPlayer.start();
                    //mBackgroundMediaPlayer.setVolume(ps.getVolume(), ps.getVolume());
                    ps.setBackgroundIsPause(false);
                    try {
                        ps.setCurrentMediaName(Data.getInstance().getMediaName(mBackgroundMediaId));
                        writeLog(4, ps.getNowTime() + " (2)start Background id:" + mBackgroundMediaId + " " + ps.getCurrentMediaName());
                        writeStatus(ps.getCurrentMediaName(), "started");
                    } catch (Exception e1) {
                        Log.d(TAG, "startBackgroundMusic e1:" + e1);
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "startBackgroundMusic e:" + e);
                mBackgroundMediaPlayer.start();
                //mBackgroundMediaPlayer.setVolume(ps.getVolume(), ps.getVolume());
                ps.setBackgroundIsPause(false);
                try {
                    ps.setCurrentMediaName(Data.getInstance().getMediaName(mBackgroundMediaId));
                    writeLog(4, ps.getNowTime() + " (2)start Background id:" + mBackgroundMediaId + " " + ps.getCurrentMediaName());
                    writeStatus(ps.getCurrentMediaName(), "started");
                } catch (Exception e2) {
                    Log.d(TAG, "startBackgroundMusic e1:" + e2);
                }
            }
        }
    }


    private void pauseBackgroundMusic() {
        if (mBackgroundMediaPlayer != null) {
            try {
                if (mBackgroundMediaPlayer.isPlaying()) {
                    mBackgroundMediaPlayer.pause();
                    ps.setBackgroundIsPause(true);
                }
            } catch (Exception e) {
                try {
                    mBackgroundMediaPlayer.pause();
                    ps.setBackgroundIsPause(true);
                } catch (Exception e1) {
                    writeLog(4, "kann keine Pause für Background machen Error:"+e.getMessage());
                }
            }
        }
    }

    // MediaObject Player
    private void play(int mediaId) {
        if (mediaPlayer != null) {
            Log.d(TAG, "play: stoppen weil das nächste kommt");
            try {
                mediaPlayer.pause();
                //if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
                //}
            } catch (Exception e) {
                writeLog(4, "Exception bei Pause:"+e.getMessage());
            }
        }
        //writeLog(4, "starte MediaId:" + mediaId);
        mediaPlayer = MediaPlayer.create(this, mediaId);
        mediaPlayer.start();
        ps.setVolume(CompanionAudioService.getInstance().getVolume());
        //mediaPlayer.setVolume(ps.getVolume(), ps.getVolume());
        try {
            ps.setCurrentMediaName(Data.getInstance().getMediaName(mediaId));
        } catch (Exception e) {
            Log.d(TAG, "kenne Namen von MediaId:"+mediaId+"nicht");
        }
        writeLog(3, "starte MediaId:" + mediaId);

    }

    // MediaObject Player get Duration
    private int getMediaDuration() {
        //if (mPlayerAdapter != null) {
        //    return mPlayerAdapter.getDuration();
        //}
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 3000;
    }

    /*
     V1.31  Sateliten holen
     */
    private void GPSStatus() {

    }

    private float calcSpeed() {
        Location lastLocation = null;
        Location firstLocation = null;
        float maxSpeed = 8;  // ms
        float speed = 0;
        if (mLocationFifo.size() > 0) {
            lastLocation = mLocationFifo.get(mLocationFifo.size()-1);
            if (lastLocation.getSpeed() >= ACSettings.getInstance().getGpsMinSpeed()) {
                speed =  lastLocation.getSpeed();
            }
        }
        // speed wird nur berechnet wenn mehr als 1 Wert vorhanden ist.
        if (mLocationFifo.size() > 1) {
            firstLocation = mLocationFifo.get(0);
            long t1 = firstLocation.getTime();


            Ort firstPlace = new Ort(firstLocation.getLatitude(), firstLocation.getLongitude());
            double deltaPos = firstPlace.calculateDistance(lastLocation.getLatitude(), lastLocation.getLongitude());
            // zeit seit letzten gps
            //long now = System.currentTimeMillis();
            long deltaT = (lastLocation.getTime() - firstLocation.getTime())/1000;   // in s
            // speed berechnet
            float speedCalc = Math.round( deltaPos/deltaT);
            ps.setDeltaPos(deltaPos);
            ps.setDeltaT(deltaT);
            ps.setSpeedCalc(speedCalc);
            if (speed == 0) {  // kein speed vom GPS
                if (speedCalc <= maxSpeed) {
                    if (speedCalc >= ACSettings.getInstance().getGpsMinSpeed()) {
                        writeLog(3, "Speed c:" + speedCalc);
                    }
                    speed = speedCalc;
                }
            }
        }
        return speed;
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                /*
                // distanz zur letzten Messung berechnen
                Ort lastPlace = new Ort(ps.getLatitude(), ps.getLongitude());
                double deltaPos = lastPlace.calculateDistance(location.getLatitude(), location.getLongitude());
                // zeit seit letzten gps
                long now = System.currentTimeMillis();
                long deltaT = (now - ps.getNow())/1000;   // in s
                // speed berechnet
                float speedCalc = (float) deltaPos/deltaT;
                // dir berechnet
                */

                // die letzten 4 locations behalten
                mLocationFifo.add(location);
                if (mLocationFifo.size() > 4) {
                    mLocationFifo.remove(0);  // das letzte entfernen
                }

                calcSpeed();    // Speed aus den letzten 4 Positionen berechnen
                ps.setGPSFrom("mLocationCallback");

                // Update UI with location data
                ps.setLatitude(location.getLatitude());
                ps.setLongitude(location.getLongitude());
                ps.setSpeed(location.getSpeed());
                ps.setAltitude(location.getAltitude());
                ps.setDirection(location.getBearing());
                // kommt eh nichts zurück
                if (location != null) {
                    Bundle extras = location.getExtras();
                    if (extras != null) {
                        ps.setSatelites(location.getExtras().getInt("satellites"));
                        if (ps.getSatelites() > 0) {
                            writeLog(4, "mLocationCallback.satelites:"+ps.getSatelites());
                        }
                    }
                }

                displayLocation(ps);
                //mLastLocation = location;
                //Toast.makeText(AirtableEditActivity.this, "Lat :" + location.getLatitude() + " Long :"
                //        + location.getLongitude(), Toast.LENGTH_SHORT).show();
                //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                //startIntentService();
            }
        }
    };

    /*
     //TODO: diese Funktion soll in Zukunft entscheiden, ob eine neue Position gültig ist oder nicht
     */
    private Location checkLocation(Location location) {
        final float[] results = new float[3];
        Location.distanceBetween(ps.getLatitude(), ps.getLongitude(), location.getLatitude(), location.getLongitude(), results);
        double dist = results[0];        // speed holen und zeit seit letztem tick.

        // die Frage ist, welche?
        return location;

    }
    private void getLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , LOCATION_PERMISSION);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        writeLog(4,  "getLocation callback aufgerufen");
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            ps.setLatitude(location.getLatitude());
                            ps.setLongitude(location.getLongitude());
                            ps.setSpeed(location.getSpeed());
                            ps.setAltitude(location.getAltitude());
                            ps.setDirection(location.getBearing());
                            if (location != null) {
                                Bundle extras = location.getExtras();
                                if (extras != null) {
                                    ps.setSatelites(location.getExtras().getInt("satellites"));
                                    if (ps.getSatelites() > 0) {
                                        writeLog(4, "getLocation.satelites:"+ps.getSatelites());
                                    }
                                }
                            }
                            ps.setAccuracy(location.getAccuracy());
                            ps.setProvider(location.getProvider());

                            ps.setGPSFrom("getLastLocation");

                            displayLocation(ps);
                            //mLastLocation = location;
                            //Toast.makeText(AirtableEditActivity.this, location.getLatitude() + " "
                            //        + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            //startIntentService();
                        } else {
                            createAndCheckLocationRequest();
                            Toast.makeText(LocationTrackerActivity.this, "location is null", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode
            , @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    //permission denied! we cant use location services.
                }
            }
            case 10: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }else{
                    //User denied Permission.
                    int i = 0;
                }
            }
        }
    }

    private void createAndCheckLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(4000);
        mLocationRequest.setFastestInterval(3000);
        writeLog(4, "createLocationRequest min:3s, norm:4s");
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                requestLocationUpdate();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    writeLog(4, "LocationRequest Failure");
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(LocationTrackerActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void requestLocationUpdate() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    , LOCATION_PERMISSION);
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                Looper.myLooper());
    }

    /*
      Akku Daten wie Level und Temperatur
     */
    // kann man auch in den Register Akku Listener verschieben
    public float getBatterieStatus() {
        // Akku Status holen
        if (mBatteryStatus == null) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            mBatteryStatus = this.registerReceiver(null, ifilter);
        }
        if (mBatteryStatus != null) {
            int level = mBatteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = mBatteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            return level / (float) scale;
        }
        writeLog(3, "Batterie Status kann nicht abgefragt werden");
        return -1;

    }

    //public void registerSMSListener() {
    //}


    public void registerAkkuListener() {
        //diese erstellte Klasse reagiert, auf den soeben erstellten Receiver
        mAkkuReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //gibt den Wert in Grad Celsius wieder, aber ohne Komma

                int temperatur = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                //wandelt den Wert in Grad Celsius, mit Komma um
                float genaueTemperatur = ((float) temperatur) / 10;
                //gibt den genauen Temperaturwert aus
                ps.setTemperatur(genaueTemperatur);
                ps.setBatterieStatus(level / (float) scale);
                long now = System.currentTimeMillis();
                long dauer = (int) (now - mLastAkkuCheck)/1000;
                int akkuAlarmWriteDelay = ACSettings.getInstance().getAirtableAkkuAlarmWriteDelay();  // in Sekunden, standard 300
                if (dauer > akkuAlarmWriteDelay || ps.getTemperatur() == 0)  { // 5Minuten
                    mLastAkkuCheck = now;
                    //int iLevel = Math.round(ps.getBatterieStatus());
                    int iTemp = Math.round(genaueTemperatur);
                    mAlarmService.checkAlarm("key_battery_level", level);
                    mAlarmService.checkAlarm("key_battery_temp", iTemp);
                }
                writeStatus("Akku Level", level+"" );
                writeStatus("Akku Temp", Math.round(genaueTemperatur)+"");
            }
        };
        //erstellt einen Receiver, der immer reagiert, wenn sich etwas bei dem Akku ändert (Akkuladung, Temperatur, Ladestatus, etc.)
        this.registerReceiver(mAkkuReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public static String splitTime(long longVal)
    {
        int hours = (int) longVal / 3600;
        int remainder = (int) longVal - hours * 3600;
        int mins = remainder / 60;
        remainder = remainder - mins * 60;
        int secs = remainder;

        int[] ints = {hours , mins , secs};
        return hours+":"+mins+":"+secs+" (h:m:s)";
    }
    /*
      Hier wird der ganze Display des Location Trackers aufbereitet und angezeigt
     */
    private void displayLocation(PlayerStatus ps) {

        // + aktuellerOrt!=null?aktuellerOrt.getName():"Ort unbekannt"

        eventLoop(ps);
        writeLog(3, "Screen:");
        // TODO: nicht jedes mal machen, je nach Ort hat das auch Zeit
        // dadurch wechselt der nächste Ort in der Mitte der Strecke
        Ort nearestOrt = anlage.InitalPositionFinden(ps.getLatitude(), ps.getLongitude(), ps.getDirection());
        Ort myPos = new Ort(ps.getLatitude(), ps.getLongitude());
        double dDistPos = 0;
        String eName = "nix";
        String nOrtName = "";
        if (nearestOrt != null) {
            nOrtName = nearestOrt.getName();
            dDistPos = myPos.calculateDistance(nearestOrt.getLatitude(), nearestOrt.getLongitude());
            //Ereignis nextEreignis = nearestOrt.getAktion(journey);
        }
        boolean eIsDone = false;
        String eMediaName ="";
        if (ps.getCurrentEreignis() != null) {
            eName = ps.getCurrentEreignis().getName();
            eMediaName = Data.getInstance().getMediaName(ps.getCurrentEreignis().getMediaId());
            eIsDone = ps.getCurrentEreignis().isDone();
        }
        String eLastName = "nix";
        String eLastMediaName ="";
        if (ps.getLastEreignis() != null) {
            eLastName = ps.getLastEreignis().getName();
            eLastMediaName = Data.getInstance().getMediaName(ps.getLastEreignis().getMediaId());
            //eIsDone = ps.getCurrentEreignis().isDone();
        }

        if (ps.isGPSTracking()) {
            // TODO Zeit seit letzten call einbauen
            // beim ersten Durchganng ist deltaTMs riesengross
            long now = System.currentTimeMillis();
            int deltaTms = (int) (now - ps.getNow());
            mLogService.writeGPSLocation(ps.getLatitude(),ps.getLongitude(), ps.getSpeed(),ps.getDirection(), ps.getWifiLevel(), deltaTms, eMediaName, ps.getOrt(), ps.getAccuracy());
        }

        String myOrtName = "";
        String oetNextOrt = "";
        int iRadius = 0;
        int calcDir = 0;
        if (ps.getOrt() != null) {
            myOrtName = ps.getOrt().getName();
            if (ps.getOrt() instanceof Weg) {
                iRadius = (int) ((Weg) ps.getOrt()).getRadiusNachOrt();
                oetNextOrt = "nextOrt";
                calcDir = (int) myPos.calculateDirection(ps.getZielOrt());
            } else {
                iRadius = (int) ((Ort) ps.getOrt()).getRadius();
                oetNextOrt = "Ort";

            }
        }

        String myZielName = "";
        if (ps.getZielOrt() != null) {
            myZielName = ps.getZielOrt().getName();
        }


        int iDistPos = (int) dDistPos;
        int iSpeedMS = Math.round(ps.getSpeed());
        int iSpeed = (int) Math.round(iSpeedMS *  3.6);   // in km/h umgerechnet
        String iDuration = "";
        if (ps.getAbstandZiel() > 0) {
            float ms = ps.getSpeed();
            if (ms > 0) {
                // 60m / 2m/s = 30 Sekunden / 60 = 0.5 Minuten
                iDuration = splitTime(Math.round(ps.getAbstandZiel() / ms));
            }
        }
        int iDirection = Math.round(ps.getDirection());
        final float[] results = new float[3];
        Location.distanceBetween(ps.getLatitude(), ps.getLongitude(), ps.getHomeLatitude(), ps.getHomeLongitude(), results);
        double dist = results[0];
        double dDistToHome = new Ort(ps.getLatitude(), ps.getLongitude()).calculateDistance(ps.getHomeLatitude(), ps.getHomeLongitude());
        int iDistToHome = (int) dDistToHome;
        ps.setDistHome(iDistToHome);
        int iAltitude = (int) ps.getAltitude();
        //int iSats = ps.getSatelites();

        ps.setCount(count++);

        ps.setBluetoothStatus(mLogService.getChatStatus());

        // Lautstärke, wenn sie vom System verstellt wurde, wird sie zurückgesetzt
        // das war um zu verhindern, dass das Betriebssystem die Lautstärke runtersetzt, funktioniert ber nicht
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        int currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        // aber nur, wenn wir nicht im silent Mode sind
        if (!CompanionAudioService.getInstance().isSilent) {
            if (currVolume != CompanionAudioService.theVolume) {
                long now = System.currentTimeMillis();
                long sincelastLog = (now - lastVolumeLog);
                if (sincelastLog > 180000) { // Minuten
                    writeLog(4, "Volume falsch: " + currVolume + " setze: " + CompanionAudioService.theVolume);
                    lastVolumeLog = now;
                }
                if (sincelastLog > 18000) { // Minuten
                    CompanionAudioService.getInstance().setVolume(CompanionAudioService.theVolume);
                    currVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                }
            }
        }
        ps.setVolume(currVolume);


        locationTv.setText(anlage.getName()+" "+mDeviceName
                + "\nNow: " + ps.getNowDateTime()
                + "\nMyOrt: " + myOrtName
                + "\nZiel: " + myZielName
                + "\nDirection: " + iDirection + " calc:" + calcDir
                + "\nDist Ziel: " + (int) ps.getAbstandZiel() + " m"
                + "\nDist Start: " + (int) ps.getAbstandStart() + " m"
                + "\nDist Home: " + iDistToHome + " m "
                + "\nTime to Ziel: " + iDuration
                + "\nWLAN: " + ps.getWlanStatus()
                + "\nSSID: " + ps.getWifiSSID()
                + "\nRadius "+oetNextOrt+": " + iRadius + " m"
                + "\nLanden: " + ps.isLanding()
                + "\nLat: " + String.format("%.6f", ps.getLatitude())
                + "\nLng: " + String.format("%.6f", ps.getLongitude())
                + "\nGPSTrack: " + ps.isGPSTracking()
                + "\nAccuracy: " + (int) ps.getAccuracy()
                + "\nDeltaPos: " +  ps.getDeltaPos()
                + "\nDeltaT: " +  ps.getDeltaT()
                + "\nSpeed Calc: " +  ps.getSpeedCalc()
                + "\nNext Ort: " + nOrtName
                + "\nDist Ort: " + (int) dDistPos + " m"
                + "\nVolume: " + currVolume
                + "\nTemp: " + ps.getTemperatur()
                + "\nAkku: " + ps.getBatterieStatus()
                + "\nNotStop: " + ps.isNotStop() +(ps.getDauerBisNotstop()>0?" Stop in:"+ps.getDauerBisNotstop():"")
                + "\nSpeed: " + iSpeed + " km/h " + iSpeedMS + " m/s"
                + "\nGPSCount: " + ps.getCount()
                + "\nSoundfile: " + ps.getCurrentMediaName()
                + "\nAkt.Ereignis: " + eMediaName +": "+ eName + " Done:" + eIsDone
                + "\nLst.Ereignis: " + eLastMediaName +": " + eLastName
                + "\nBluetooth: " + ps.getBluetoothStatus()
                + "\nHöhe: " + iAltitude
                + "\nEnde\n\n"
        );

        // schreibt Log, Status und Screen nach Bluetooth und falls verfügbar nach Airtable
        mLogService.writeScreenToLog(locationTv.getText().toString(), myOrtName, ps);

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mLogService.onActivityResult(requestCode, resultCode, data);
    }

    private void ensureDiscoverable() {
        Log.d(TAG, "wird das aufgerufen?");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (testOn != null) {
            if (testOn.equals("1")) {
                // nix das ist Test

            } else {
                //TODO GPS Tracker wieder aktivieren, wenn er geschlafen hat
                createAndCheckLocationRequest();
                // falls WLAN abgeschaltet ist, einschalten
                if (mWifiManager != null) {
                    if (!mWifiManager.isWifiEnabled()) {
                        mWifiManager.setWifiEnabled(true);
                    }
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.RECORD_AUDIO },
                    10);
        } else {
            mRecorder.RecorderInit();
            //mHandler.postDelayed(mUpdateTimer, 200L);
        }

        //mBluetoothLogger.setupBT();
    }



    @Override
    protected void onPause() {
        super.onPause();
    }


    /*
     * wird nach onPause aufgerufen, wenn die Activity wieder sichtbar wird
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        mLogService.start();
    }



    @Override
    public void onBackPressed() {
        writeLog(4, "user pressed back");
        if (mSimulationHandler != null) {
            mSimulationHandler.removeCallbacks(mPeriodicUpdate);
            mSimulationHandler.removeCallbacks(mPeriodicUpdateTrack);
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
            } catch (Exception e) {
                // nix
            }
            try {
                mediaPlayer.reset();
            } catch (Exception e) {
                // nix
            }
            try {
                mediaPlayer.release();
            } catch (Exception e) {
                // nix
            }
        }
        if (mBackgroundMediaPlayer != null) {
            try {
                mBackgroundMediaPlayer.release();
            } catch (Exception e) {
                writeLog(4, "backgroudplayer.release() crashed:"+e.getMessage());
            }
        }
        if (darkScreen) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.screenBrightness = -1;
            getWindow().setAttributes(params);
        }

        // V1.10: wenn BAck gerückt wurde, soll die Main Activiry warten
        Intent resultIntent = new Intent();
        // TODO Add extras or a data URI to this intent as appropriate.
        resultIntent.putExtra("action", "STOP");
        setResult(Activity.RESULT_OK, resultIntent);
    }
    /*
     * Activität ist nicht mehr sichtbar, wenn sie wieder sichtbar wird, wird onRestart aufgerufen
     * Die Activity läuft aber im Hintergrund weiter.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mRecorder.RecorderRelease();
        //mHandler.removeCallbacks(mUpdateTimer);
    }

    /*
     * wird nicht aufgerufen, wenn ich via Intent eine andere Activity starte
     * DAs ist die Remote Stop Situation
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Connection Detection
        if (manager != null) {
            manager.unregisterNetworkCallback(networkCallback);
        }
        //mBluetoothLogger.stop();

        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                mediaPlayer.reset();
                mediaPlayer.release();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
        if (mBackgroundMediaPlayer != null) {
            try {
                mBackgroundMediaPlayer.pause();
                mBackgroundMediaPlayer.reset();
                mBackgroundMediaPlayer.release();
            } catch (Exception e) {
               Log.d(TAG, e.getMessage());
            }
        }

        //if (mSmsBroadcastReceiver != null) {
        //    unregisterReceiver(mSmsBroadcastReceiver);
        //}
        try {
            this.unregisterReceiver(mAkkuReceiver);
        } catch (Exception e) {
            // wenn es keinen Akku hat, zb der Simulator
        }

        //stopService(new Intent(LocationTrackerActivity.this, BackGroundMusic.class));
        try {
            this.unregisterReceiver(mMessageCmdReceiver);
        } catch (Exception e) {
            // you never know
            Log.d(TAG, e.getMessage());
        }

        if(mFlicReceiver != null) {
            try {
                unregisterReceiver(mFlicReceiver);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }

        if (mSimulationHandler != null) {
            mSimulationHandler.removeCallbacks(mPeriodicUpdate);
            mSimulationHandler.removeCallbacks(mPeriodicUpdateTrack);
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void askForWIFIPermission() {

        Context context = this;
        String readPermission = "android.permission.CHANGE_WIFI_STATE";
        if (context.checkSelfPermission(readPermission) != PackageManager.PERMISSION_GRANTED) {
            //requestGrantPermission(readPermission);
        }

    }

    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {

                //This is called if user has denied the permission before
                //In this case I am just asking the permission again
                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
            }
        } else {
            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }
    }

}
