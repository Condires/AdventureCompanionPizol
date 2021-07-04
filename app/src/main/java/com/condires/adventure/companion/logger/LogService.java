package com.condires.adventure.companion.logger;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.condires.adventure.companion.MainActivity;
import com.condires.adventure.companion.airtable.AirtableLogger;
import com.condires.adventure.companion.airtable.AirtableService;
import com.condires.adventure.companion.alarm.Alarm;
import com.condires.adventure.companion.arrival.AirtableArrival;
import com.condires.adventure.companion.bluetooth.BluetoothChatService;
import com.condires.adventure.companion.bluetooth.BluetoothLogger;
import com.condires.adventure.companion.bluetooth.StatusDAO;
import com.condires.adventure.companion.gpstracker.GPSTrack;
import com.condires.adventure.companion.logwrapper.Log;
import com.condires.adventure.companion.model.IrgendWo;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.PlayerStatus;
import com.condires.adventure.companion.setting.ACSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/*
 * schreibt alle Logs in die verschiedenen Desitonationen,
 */

public class LogService {
    private String TAG = this.getClass().getSimpleName();
    BluetoothLogger mBluetoothLogger;
    AirtableLogger  mAirtableLogger;
    AirtableArrival mAirtableArrival;
    private AirtableService mAirtableService;
    private String          mDeviceName;  // der name des Gerätes
    private long lastScreenBTUpdate;   // time in ms, wann der letzte screen an den chat geschickt wurde
    private long lastLogBTUpdate;   // time in ms, wann der letzte Log an den chat geschickt wurde
    private long lastAirtableUpdate;  // zeit ms wann der letzte Log via Airtable verschickt wurde , Default alle Minute
    private String lastEreignis = "";  // GPS Tracker sucht Erreignis Wechsel
    private long lastGPSTrackNow = 0;

    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
     // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static LogService instance;
    private IrgendWo lastGPSOrt = new Ort();

    public static LogService getInstance (AppCompatActivity activity) {
        if (LogService.instance == null) {
            LogService.instance = new LogService (activity);
        }
        return LogService.instance;
    }

    // darf nur benutzt werden, wenn sicher ist, dass der Service schon initialisiert ist, kann null zurück geben
    public static LogService getExistingInstance() {
        return instance;
    }

    // Verhindere die Erzeugung des Objektes über andere Methoden
    private LogService (AppCompatActivity activity) {
        if (instance == null) {
            instance = this;
        }
        mBluetoothLogger = BluetoothLogger.getInstance(activity);
        //TODO: macht das Sinn, hier die Activity neu zu setzen?
        mBluetoothLogger.setActivity(activity);
        mDeviceName = getDeviceName();
        mAirtableService = AirtableService.getInstance();
    }

    public void writeScreenToLog(String screen, String myOrtName, PlayerStatus ps) {
        writeLog(TAG, 3, "BT?");
        // Wenn der Chat verbunden ist, wird hier das UI an den Chat geschickt.
        long now = System.currentTimeMillis();
        if (getState() == BluetoothChatService.STATE_CONNECTED) {
            writeLog(TAG, 3, "ja");
            if (lastLogBTUpdate > 0) {
                int btLogDelay = ACSettings.getInstance().getBtLogDelay();  // in ms  standard 3125
                if (lastLogBTUpdate + btLogDelay < now) {
                    if (lastLogBTUpdate > 0) {
                        mBluetoothLogger.sendLogToBT() ;
                        lastLogBTUpdate = now;
                    }
                }
            } else {
                lastLogBTUpdate = now;
            }

            if (lastScreenBTUpdate > 0) {
                int btScreenUpdateDelay = ACSettings.getInstance().getBtScreenUpdateDelay();  // in ms standard 2355
                if (lastScreenBTUpdate + btScreenUpdateDelay < now) {
                    writeLog(TAG, 3, "Screen&Status an BT");
                    String status = getStatusMessage(ps);
                    String content = "@<Screen>@" + screen + "@</Screen>@"+"@<Status>@"+status+"@</Status>@";
                    mBluetoothLogger.sendMessage(content);
                    lastScreenBTUpdate = now;
                    /*
                    mBluetoothLogger.sendScreenToBT(screen);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    writeLog(TAG, 3, "Ich schicke Status an Bluetooth");
                    String status = getStatusMessage(ps);
                    mBluetoothLogger.sendStatusToBT(status);
                    */
                    /*
                    try {
                        Thread.sleep(400);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //writeLog(TAG, 3, "Ich schicke Log an Bluetooth");
                    mBluetoothLogger.sendLogToBT() ;
                    */

                }
            } else {
                lastScreenBTUpdate = now;
            }
        } else {
            writeLog(TAG, 3, "no");
        }
        writeLog(TAG, 3, "WLAN?");
        // Falls WLAN da ist, Log auf Airtable schreiben
        //if (mAirtableService.isInternetAvailable(this.getApplicationContext())) {
        if (mAirtableService.isInternetAvailable2()) {
            if (lastAirtableUpdate > 0) {
                int airtableLogWriteDelay = ACSettings.getInstance().getAirtableLogWriteDelay();
                int dauer = (int) (airtableLogWriteDelay - (now - lastAirtableUpdate))/1000;
                //writeLog(TAG, 3, "now-lastUpdate="+(now - lastAirtableUpdate)/1000+" s");
                writeLog(TAG, 3, "write in "+dauer+"s");
                if ((lastAirtableUpdate + airtableLogWriteDelay) < now) {
                    if (mAirtableLogger != null) {
                        writeLog(TAG, 3, "write AirLog");
                        mAirtableLogger.writeAirtableScreen(screen);
                        mAirtableLogger.writeLnAirtableLog(myOrtName);
                        lastAirtableUpdate = now;
                    }
                }
            } else {
                lastAirtableUpdate = now;
            }

        } else {
            writeLog(TAG, 3, "no");
        }
    }

    public String getStatusMessage(PlayerStatus ps) {
        //TODO nur erzeugen, wenn wir eine bt Connection haben und wirklich senden wollen
        StatusDAO status = new StatusDAO();
        status.setBatteryLevel(ps.getBatterieStatus()+"");
        status.setGpsCount(ps.getCount()+"");
        if (ps.getOrt() != null) {
            status.setOrt(ps.getOrt().getName());
        }
        status.setMediaName(ps.getCurrentMediaName());
        status.setTemperatur(ps.getTemperatur()+"");
        status.setVolume(ps.getVolume()+"");
        status.setPlayer(ps.getPlayerName());
        status.setWlanStatus(ps.getWlanStatus());
        status.setWlanConnected(ps.getWifiSSID());
        status.setBluetoothConnected(ps.getBluetoothStatus());
        status.setAbstandZiel(ps.getAbstandZiel()+"");
        String json = gson.toJson(status);
        return json;
    }


    public void writeAlarm(Alarm alarm) {
        //auf allen Kanälen
        mBluetoothLogger.writeBluetoothLog(alarm.toString());
        // objekt, filter
        alarm.setDevice(mDeviceName);
        // "AND({Player} = '" + mDeviceName + "', {Parameter} ='"  + alarm.getParameter() + "')"
        long now = System.currentTimeMillis();
        writeLog(TAG, 3, "Alarm "+alarm.getParameter()+" an Airtable");
        mAirtableService.insertUpdateAirtableObject(alarm, "AND({Player} = '" + mDeviceName + "', {Parameter} ='"  + alarm.getParameter() + "')");
        // TODO bei erfolg airtableWriteTime setzen
        writeLog(TAG, 3, "Schreiben dauerte "+(System.currentTimeMillis()-now)/1000+" s");
    }

    public void writeStackTrace() {

    }

    public void writeGPSTrack(GPSTrack gpsTrack) {
        //auf allen Kanälen
        //mBluetoothLogger.writeBluetoothLog(gpsTrack.toString());
        // objekt, filter
        gpsTrack.setDevice(mDeviceName);
        // "AND({Player} = '" + mDeviceName + "', {Parameter} ='"  + alarm.getParameter() + "')"
        long now = System.currentTimeMillis();
        //writeLog(TAG, 3, "GPSTrack "+gpsTrack.getDevice()+" an Airtable");
        // IS_AFTER({Event Date}, DATETIME_PARSE('2017-05-01'))
        // IF(Date >= DATETIME_PARSE(DATETIME_FORMAT(TODAY(), 'YYYY-MM-01')), 1, 0)
        // IS_SAME({Date 1}, {Date 2}, 'hour')
        String date = gpsTrack.getStringDate();
        //mAirtableService.insertUpdateAirtableObject(gpsTrack, "AND({Player} = '" + mDeviceName + "', IS_SAME({LogDate}, '"+date+"', 'day')");
        mAirtableService.insertUpdateAirtableObject(gpsTrack, "{Name} = '" + mDeviceName + "-"+date+"'");
        // TODO bei Erfolg airtableWriteTime setzen
        writeLog(TAG, 4, "GPSwrite dauer "+(System.currentTimeMillis()-now)/1000+" s");
    }

    public String loadGPSTrack(String name, int trackNo) {
        GPSTrack track = new GPSTrack();
        String[] fields = {"Name","Track"+trackNo};
        track = (GPSTrack) mAirtableService.loadAirtableObject(track, "{Name} = '"+ name+"'", fields);
        return track.getTrack(trackNo);
    }

    // holt denn gesammten Track aus dem AirtableLogger, damit der GPS Service das Log in den richtgen Slot legen kann
    public String getGPSTrack() {
        return mAirtableLogger.getGPSTrack();
    }




    /*
     * schreibt eine Position in das GPS Log.
     * deltaTms wird neu im Tracker gemessen, es ist die Zeit zwischen zwei GPS Positionen
     */
    public void writeGPSLocation(double latitude, double longitude, float speed, float direction, int wifiLevel, int deltaTms, String currEreignis, IrgendWo currentOrt, double accuracy) {
        long now = System.currentTimeMillis();
        String ortName = null;
        String ereignis = null;
        String strDate = "";
        String mac = null;
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss");//dd/MM/yyyy
        Date nowDate = new Date();
        strDate = sdfDate.format(nowDate);
        if (currEreignis != null &&  currEreignis.indexOf(':') >=0) {
            //log WLAN Action
            mac = currEreignis;
        } else {
            if (lastEreignis != null && !lastEreignis.equals(currEreignis)) {
                ereignis = currEreignis;
                lastEreignis = currEreignis;
            }
            if (currentOrt != null && currentOrt instanceof Ort) {
                if (!lastGPSOrt.equals(currentOrt)) {
                    ortName = ((Ort) currentOrt).getName();
                    lastGPSOrt = currentOrt;
                }
            }
        }

        String lat = String.format("%.6f", latitude);
        lat = lat.replace(',','.');
        String lng = String.format("%.6f", longitude);
        lng = lng.replace(',','.');
        String sp = String.format("%.0f", speed);
        sp = sp.replace(',','.');
        String dir = String.format("%.0f", direction);
        dir = dir.replace(',','.');
        String acc = String.format("%.0f", accuracy);
        acc = acc.replace(',','.');
        deltaTms = (int) (now - lastGPSTrackNow);
        String msg = "";
        if (ereignis != null) {
            msg = "loc(" + lat + "," + lng + "," + sp + "," + dir + "," + wifiLevel + "," + deltaTms + "," + ereignis + "," + strDate + ","+acc+");";
            mAirtableLogger.writeAirtableGPSTrack(msg);
        } else if (ortName != null) {
            msg = "loc("+lat+","+lng+","+sp+","+dir+","+wifiLevel+",0,"+ortName+","+strDate+","+acc+");";
            mAirtableLogger.writeAirtableGPSTrack(msg);
        } else if (mac != null) {
            msg = "loc("+lat+","+lng+","+sp+","+dir+","+wifiLevel+",0,"+mac+","+strDate+","+acc+");";
            mAirtableLogger.writeAirtableGPSTrack(msg);
        } else {
            msg = "loc("+lat+","+lng+","+sp+","+dir+","+wifiLevel+","+deltaTms+",,,"+acc+");";
            mAirtableLogger.writeAirtableGPSTrack(msg);
        }
        lastGPSTrackNow = now;

    }

    /*
     schreibt eine Liste von Meldungen ins Log. Im Moment nur für Typ 7 Status Meldunge
     */
    public void writeLog(String tag, int type, List<String> msgList) {
        // Statusmeldungen
        if (type == 7) {
            mAirtableLogger.writeAirtableStatus(msgList);
        }
    }

    public void writeStatusToLongLog() {
        mAirtableLogger.writeStatusToLongLog();
    }
    /*
   Für den Display sollte das log via Bluetooth kontinuierlich ausgegeben werden, unabhängig vom Ort
   Airtable wird bedient, wenn eine WLAN Verbindung zur Verfügung steht. Die Anzahl Datensätze soll aber gering gehalten werden.
   Logs in File in Display und in Airtable steuern
   1) nur Logfile, 2) Logfile und Airtable, 3) Logfile, Airtable und Display
   TODO:loglevel steuern,
   TODO Loglevel via Chat steuern, oder via SMS
 */
    public void writeLog(String tag, int type, String msg) {
        //msg = tag+":"+msg;
        String nowDateTime = DateFormat.getDateTimeInstance().format(new Date());
        if (type != 1) {
            if (mAirtableLogger == null) {
                mAirtableLogger = new AirtableLogger(MainActivity.getContext(), mDeviceName, mAirtableService);
            }
        }
        // nur ins logfile schrieben
        if (type == 1) {
            Log.d(TAG, msg);
            // Logfile und Airtable Log schreiben
        } else if (type == 2) {
            Log.d(TAG, msg);
            mAirtableLogger.writeAirtableLog(msg, nowDateTime);
            mBluetoothLogger.writeBluetoothLog(msg);
        } else if (type == 4) {  //b neu sind Statusmeldungen
            Log.d(TAG, msg);
            //TODO Status meldung speziel behandeln, eigenee Att in Airtable
            mAirtableLogger.writeAirtableLog(msg, nowDateTime);
            mAirtableLogger.writeAirtableLogTyp4(msg, nowDateTime);
            mBluetoothLogger.writeBluetoothLog(msg);
            // Logfile, AirtableLog und Bluetooth Log schreiben
        } else if (type == 5) {
            mAirtableLogger.writeAirtableLogJSON(msg, nowDateTime);
        } else if (type == 6) {
            mAirtableLogger.writeAirtableLogDB(msg);
        } else if (type == 7) {
            mAirtableLogger.writeAirtableStatus(msg);
        } else {
            Log.d(TAG, msg);
            mAirtableLogger.writeAirtableLog(msg, nowDateTime);
            mBluetoothLogger.writeBluetoothLog(msg);
            //ps.setMsg(msg);
        }
    }

    public void setAirtableLogDBNull() {
        mAirtableLogger.setAirtableLogDBNull();
    }

    public void writeStackTrace(String msg) {
        mAirtableLogger.writeAirtableLogStackTrace(msg);
    }
    public void setAirtableLogStackTraceNull() {
        mAirtableLogger.setAirtableLogStackTraceNull();
    }


    public void setAirtableGPSTrackNull() {
        mAirtableLogger.setAirtableGPSTrackNull();
    }

    /*
     * jedes Gerät hat einen Log Record mit 5 Logfeldern die roundabout gefüllt werden
     * TODO: abfangen falls es einen schreibfehler gibt, sonst ist das Log verloren
     */
    // Log auf Airtable schreiben, geht nur wenn wir eine WLAN Verbindung haben, das sollte bei einer Rundreise auf home
    // der Fall sein.java.text.DateFormat
    public void writeLnLog(String ort) {
        if (mAirtableLogger == null) {
            mAirtableLogger = new AirtableLogger(MainActivity.getContext(), mDeviceName, mAirtableService);
        }
        // TODO nur schreiben und löschen, wenn das Schreiben auf Airtable auch erfolgt ist
        mAirtableLogger.writeLnAirtableLog(ort);
        //mAirtableService.write(mAirtableService.getLogTable(), new AirLogMsg(TAG, mDeviceName, mAirLogMessage.toString()));
        //mAirLogMessage = new StringBuilder();
        //mAirLogMessage.append(ps.getNowDateTime());

    }



    /*
     * Arrival Funktionen
     */
    public void writeArrivalInfo(float speed, String zielOrtName, double distToZiel) {
        if (mAirtableArrival == null) {
            mAirtableArrival = new AirtableArrival(MainActivity.getContext(),mDeviceName, mAirtableService);
        }
        // TODO nur schreiben und löschen, wenn das Schreiben auf Airtable auch erfolgt ist
        mAirtableArrival.write(speed, zielOrtName, distToZiel);

    }


    /*
     * Wrapper für Bluetooth Funktionen
     */
    public void start() {
        mBluetoothLogger.start();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mBluetoothLogger.onActivityResult(requestCode, resultCode, data);
    }


    public int getState() {
        return mBluetoothLogger.getState();
    }
    public String getChatStatus() {
        return mBluetoothLogger.getChatStatus();
    }
    public String getDeviceName() {
        return mBluetoothLogger.getDeviceName();
    }
}
