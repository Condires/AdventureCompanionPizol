package com.condires.adventure.companion.airtable;

import android.content.Context;
import android.util.Log;

import com.condires.adventure.companion.setting.ACSettings;
import com.sybit.airtableandroid.Table;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
/*
 * schreibt Logmeldungen nach Airtable, falls eine Verbindung verfügbar ist.
 */
public class AirtableLogger {

    private String TAG = this.getClass().getSimpleName();
    // Wird benötigt um Log Meldungen an Airtable zu schicken
    // Auf Airtable werden Logmeldungen abgelegt.
    private AirtableService airtableService;
    private String          deviceName;  // der name des Gerätes
    private StringBuilder   mAirLogMessage = new StringBuilder(); // nur die Meldungen
    private StringBuilder   mAirLogMsgType4 = new StringBuilder();
    private String          mAirLogMsgJSON;
    private StringBuilder   mAirLogGPSTrack = new StringBuilder();  // logt die Positionen auf einer ganzen Runde
    private StringBuilder   mAirLogDB = new StringBuilder();
    private StringBuilder   mAirLogStackTrace = new StringBuilder();
    private AirLogMsg       mAirLogMsg;   // aktuelle Kopie der Log Records aus AirLog
    private String          mAirLogScreen;  // Bildschirm für das Airlog
    private String          mAirLogStatus;  // Status Meldung für eine Runde
    private String          mAirLogStatusMonitor;
    private int             mAirLogPointer = 0;   //
    private int             mMaxAirLogPointer = 5; // TODO soll parametriert werden können
    private Context         context;
    private String          logLocation;  // ort an dem das Log geschrieben wurde, nur einmal pro Ort

    private long lastTimeAirlog;  // wann wurde geschrieben

    public AirtableLogger(Context context, String deviceName, AirtableService airtableService) {
        this.context = context;
        this.deviceName = deviceName;
        this.airtableService = airtableService;
    }
    public void writeAirtableLogTyp4(String msg, String nowDateTime) {
        if (mAirLogMsgType4.length() == 0) mAirLogMsgType4.append(nowDateTime);
        if (mAirLogMsgType4.length() > ACSettings.getInstance().getAirtableLogSize()) {
            mAirLogMsgType4 = mAirLogMsgType4.delete(0,1000);
        }
        mAirLogMsgType4.append(msg + "\n");
    }


    public void writeAirtableLogJSON(String msg, String nowDateTime) {
        mAirLogMsgJSON = nowDateTime+"\n"+msg;
    }

    public void writeAirtableLogDB(String msg) {

        if (mAirLogDB.length() > ACSettings.getInstance().getAirtableLogSize()) {
            mAirLogDB = mAirLogDB.delete(0,1000);
        }
        mAirLogDB.append(msg + "\n");
    }
    public void setAirtableLogDBNull() {
        new StringBuilder();
    }

    public void writeAirtableLogStackTrace(String msg) {

        if (mAirLogStackTrace.length() > ACSettings.getInstance().getAirtableLogSize()) {
            mAirLogStackTrace = mAirLogStackTrace.delete(0,1000);
        }
        mAirLogStackTrace.append(msg + "\n");
    }
    public void setAirtableLogStackTraceNull() {
        new StringBuilder();
    }

    public void writeAirtableGPSTrack(String msg) {

        if (mAirLogGPSTrack.length() > ACSettings.getInstance().getAirtableLogSize()) {
            mAirLogGPSTrack = mAirLogGPSTrack.delete(0,1000);
        }
        mAirLogGPSTrack.append(msg + "\n");
    }

    public void setAirtableGPSTrackNull() {
        mAirLogGPSTrack = new StringBuilder();
    }
    public String getGPSTrack() {
        return mAirLogGPSTrack.toString();
    }

    public void writeAirtableStatus(String msg) {
        //mAirLogStatus = msg;
        mAirLogStatus=msg;
    }
    public void writeAirtableStatus(List<String> msgList) {
        // im 1. Element ist die liste mit toString aus der ArrayList umgewandelt
        if (msgList.size() > 0) {
            mAirLogStatus=msgList.get(0);
        }
        // im 2. Element ist die Liste asl formatierter Textblock
        if (msgList.size() > 1) {
            String hilfe = msgList.get(1);
            mAirLogStatusMonitor=hilfe;
        }
    }

    /*
     die aktuelle Meldung im Statusmoitor wird in s LongLog geschrieben.
     nowDAteTime bleibt leer, weil in diesem Moment das LongLog nicht leer sein sollte.
     */
    public void writeStatusToLongLog() {
        writeAirtableLogTyp4(mAirLogStatusMonitor, "");
    }

    public void writeAirtableLog(String msg, String nowDateTime) {
        if (mAirLogMessage.length() == 0) mAirLogMessage.append(nowDateTime);
        if (mAirLogMessage.length() > ACSettings.getInstance().getAirtableLogSize()) {
            mAirLogMessage = mAirLogMessage.delete(0,2000);
        }
        mAirLogMessage.append(msg + "\n");
    }

    public void writeAirtableScreen(String msg) {
        mAirLogScreen = msg;
    }

    public void setLogLocation(String logLocation) {
        this.logLocation = logLocation;
    }
    /*
     * jedes Gerät hat einen Log Record mit 5 Logfeldern die roundabout gefüllt werden
     * TODO: abfangen falls es einen schreibfehler gibt, sonst ist das Log verloren

     * TODO: asynchron ausführen, mit Listener der lokale Kopie löscht, wenn sie geschrieben ist die Logteile werden
     * in eine Queue geschrieben und asynchron abgearbeitet
     */
    public void writeLnAirtableLog(String logLocation) {
        // nur schreiben, wenn wir hier nicht schon geschrieben haben
        //TODO oder wir schon 5 Minuten hier sind
        long now = System.currentTimeMillis();

        if ((logLocation != null && !logLocation.equals(this.logLocation)) || (lastTimeAirlog + 5000 < now) ){
            String nowDateTime = DateFormat.getDateTimeInstance().format(new Date());
            // test ob wir wlan/bzw Internet Connection haben
            // TODO: warten bis Resultat da ist
            //if (airtableService.isInternetAvailable(this.context)) {
            if (airtableService.isInternetAvailable2()) {
                Log.d(TAG, "Wir haben internet-Connection");
                writeAirtableLog("Wir sollten Internet Connection haben","");
                // wir haben internetverbindung und es ist das erste mal
                if (mAirLogMsg == null) {
                    //mAirLogs = new ArrayList<String>();
                    //zuerst update machen und wenn nicht get write? Nein dieser Test wird pro Player nur einmal gemacht
                    // testen ob es den Record schon gibt // "{Name} = 'Bad Ragaz'" TODO geht sehr lange
                    //TODO das muss mit timeout laufen, wenn es nicht klappt

                    List msgs = trySelectAirtable(airtableService.getLogTable(), "{Device} = '" + deviceName + "'");
                    if (msgs == null) {
                        Log.d(TAG, "keine Airtable Connection");
                        return;
                    }
                    if (msgs.size() == 0) {
                        // Datensatz schreiben, falls er noch nicht existiert
                        // Message in Record schreiben
                        mAirLogMsg = new AirLogMsg(TAG, deviceName, mAirLogPointer, nowDateTime + ":" + logLocation + "\n" + mAirLogMessage.toString());
                        mAirLogMsg.setScreen(mAirLogScreen);
                        mAirLogMsg.setMsgType4(mAirLogMsgType4.toString());
                        mAirLogMsg.setJson(mAirLogMsgJSON);
                        mAirLogMsg.setStatus(mAirLogStatus);
                        mAirLogMsg.setStatusMonitor(mAirLogStatusMonitor);
                        if (mAirLogStackTrace != null) mAirLogMsg.setStackTrace(mAirLogStackTrace.toString()); else mAirLogMsg.setStackTrace(null);
                        if (mAirLogDB != null) mAirLogMsg.setDB(mAirLogDB.toString()); else mAirLogMsg.setDB(null);
                        //TODO jetzt sind alle anderen MSG Felder leer, oder?
                        mAirLogMsg = (AirLogMsg) airtableService.write(airtableService.getLogTable(), mAirLogMsg);
                    } else {
                        mAirLogMsg = (AirLogMsg) msgs.get(0);
                        mAirLogMsg.setMgs(mAirLogPointer, nowDateTime + ":" + logLocation + "\n" + mAirLogMessage.toString());
                        mAirLogMsg.setScreen(mAirLogScreen);
                        mAirLogMsg.setMsgType4(mAirLogMsgType4.toString());
                        mAirLogMsg.setJson(mAirLogMsgJSON);
                        mAirLogMsg.setStatus(mAirLogStatus);
                        mAirLogMsg.setStatusMonitor(mAirLogStatusMonitor);
                        if (mAirLogStackTrace != null) mAirLogMsg.setStackTrace(mAirLogStackTrace.toString()); else mAirLogMsg.setStackTrace(null);
                        if (mAirLogDB != null) mAirLogMsg.setDB(mAirLogDB.toString()); else mAirLogMsg.setDB(null);
                        mAirLogMsg = (AirLogMsg) airtableService.update(airtableService.getLogTable(), mAirLogMsg);
                    }

                } else {
                    mAirLogMsg.setMgs(mAirLogPointer, nowDateTime + ":" + logLocation + "\n" + mAirLogMessage.toString());
                    mAirLogMsg.setScreen(mAirLogScreen);
                    if (mAirLogMsgType4 != null) mAirLogMsg.setMsgType4(mAirLogMsgType4.toString());
                    mAirLogMsg.setJson(mAirLogMsgJSON);
                    mAirLogMsg.setStatus(mAirLogStatus);
                    mAirLogMsg.setStatusMonitor(mAirLogStatusMonitor);
                    if (mAirLogStackTrace != null) mAirLogMsg.setStackTrace(mAirLogStackTrace.toString()); else mAirLogMsg.setStackTrace(null);
                    if (mAirLogDB != null) mAirLogMsg.setDB(mAirLogDB.toString()); else mAirLogMsg.setDB(null);
                    mAirLogMsg = (AirLogMsg) airtableService.update(airtableService.getLogTable(), mAirLogMsg);
                }
                //    den neuen Logdatensatz schreiben und die Logmessage löschen
                mAirLogMessage = new StringBuilder();
                //    wenn > 5 den ältesten löschen
                if (mAirLogPointer < mMaxAirLogPointer) {
                    mAirLogPointer = mAirLogPointer + 1;
                } else {
                    mAirLogPointer = 0;
                }
                // Ort merken, solange wir hier sind wird nicht mehr geschrieben
                this.logLocation = logLocation;
                this.lastTimeAirlog = System.currentTimeMillis();
                mAirLogMsgJSON = null;
                //mAirLogGPSTrack = null;
                //mAirLogDB = null;

            }
        }

    }

    /*
     * schaut, ob wir einen Airtable call machen können, wenn nein, wird eintimout gemacht
     */
    private List trySelectAirtable(final Table table, final String filter) {
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() {
                return airtableService.exists(table, filter);
                //return airtableService.selectFiltered(table, filter);
            }
        };
        Future<Object> future = executor.submit(task);
        int timeout = ACSettings.getInstance().getInternetTimeout();
        try {
            //Give the task 5 seconds to complete
            //if not it raises a timeout exception
            Object result = future.get(timeout, TimeUnit.SECONDS);
            return (List) result; //finished in time
        } catch (
                TimeoutException | InterruptedException | ExecutionException ex) {
            //Didn't finish in time
            writeAirtableLog( "kein WLAN innerhalb von "+timeout+" s", "");
            Log.d( TAG,"Wir haben keine WLAN-Connection gefunden innerhalb von "+timeout+" Sekunden");
            //ex.printStackTrace();
            return null;
        }

    }

}
