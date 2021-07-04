package com.condires.adventure.companion.alarm;

import android.support.v7.app.AppCompatActivity;

import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.setting.ACSettings;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlarmService {
    private String TAG = this.getClass().getSimpleName();
    Map<String, List> alarmlisten;
    Map<String, Alarm> currentAlarmMap;
    Map<String,String> alarmDescMap;
    LogService mLogService;
    List<String> keys;
    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static AlarmService instance;

    public static AlarmService getInstance (AppCompatActivity activity) {
        if (AlarmService.instance == null) {
            AlarmService.instance = new AlarmService (activity);
        }
        return AlarmService.instance;
    }

    // Verhindere die Erzeugung des Objektes über andere Methoden
    private AlarmService (AppCompatActivity activity) {
        if (instance == null) {
            instance = this;
        }
        mLogService = LogService.getInstance(activity);
        alarmlisten = new HashMap<String, List>();
        currentAlarmMap = new HashMap<String, Alarm>();
        alarmDescMap = new HashMap<String,String>();
        alarmDescMap.put("key_battery_level","Der Akku verliert Strom, er ist nicht an die Powerbank angeschlossen oder die Powerbank ist nicht geladen oder die Powerbank ist nicht eingeschaltet");
        alarmDescMap.put("key_battery_temp","Der Akku ist zu heiss, etwas braucht zuviel Strom, oder die Umgebungstemperatur ist zu hoch ");
        keys = new ArrayList<String>();
        keys.add("key_battery_level");
        keys.add("key_battery_temp");
    }

    /*
     * schreibt alle Alarme raus, die existieren
     * Wird benutzt wenn der Network Listener feststellt, dass wir WLAN haben
     */
    public void writeAlarme() {
        mLogService.writeLog(TAG, 3, "WLAN da: alle Alarme schreiben");
        for (String key : keys) {
            List<Alarm> alarme = alarmlisten.get(key);
            if (alarme != null) {
                // hole den letzten Eintrag
                Alarm alarm = alarme.get(alarme.size() - 1);
                mLogService.writeAlarm(alarm);
            }
        }

    }
    /*
     * prüft ob eine Alarmwert erreicht ist und managed den Alarm, falls nötig
     */
    public void checkAlarm(String key, int value) {
        switch (key) {
            case "key_battery_level":
                int alarmLevel = ACSettings.getInstance().getBatteryAlarmLevel();
                if (value < alarmLevel) {
                    createAlarmEvent(key, String.valueOf(value));
                } else {
                    resetAlarm(key, String.valueOf(value));
                }
            case "key_battery_temp":
                int alarmTemp = ACSettings.getInstance().getBatteryAlarmTemp();
                if (value > alarmTemp) {
                    createAlarmEvent(key, String.valueOf(value));
                } else {
                    resetAlarm(key, String.valueOf(value));
                }
        }

    }

    /*
     * prüft ob ein Alarm gesetzt ist und meldet ihn als erledigt, wenn das Problem ncht mehr besteht
     */
    public void resetAlarm(String key, String value) {
        List<Alarm> alarme = alarmlisten.get(key);
        Alarm alarm;
        if (alarme != null) {
            long now = System.currentTimeMillis();
            String nowDate = DateFormat.getDateTimeInstance().format(new Date());
            // hole den letzten Eintrag
            alarm = alarme.get(alarme.size() - 1);
            alarm.setLetztmals(nowDate);
            alarm.setStatus("erledigt");
            alarm.setCurrentValue(value);
            alarm.setTimestamp(now);  // wieder 3 Minuten warten
            mLogService.writeAlarm(alarm);
        }
    }

    /*
     * es wird geprüft, ob der Alarm schon existiert
     */
    public void createAlarmEvent(String key, String value) {
        long now = System.currentTimeMillis();
        // es gibt schon Alarme von diesem Typ zB temperatur
        List<Alarm> alarme = alarmlisten.get(key);
        Alarm alarm;
        String nowDate = DateFormat.getDateTimeInstance().format(new Date());
        if (alarme != null) {
            // hole den letzten Eintrag des entsprechenden Alarms
            alarm = alarme.get(alarme.size()-1);
            // prüfen wie lange es schon her ist
            // 5 Minuten = 300 Sekunden = 300000 Milisekunden
            long vergangen = (now - alarm.getTimestamp())/1000/60;  // Minuten seit der Alarm gemeldet wurde
            if (vergangen > 3) {
                // prüfen, ob der Alarm schon raus ist
                // prüfen ob der Alarm schon betätigt wurde
                alarm.setLetztmals(nowDate);
                alarm.setCurrentValue(value);
                alarm.setTimestamp(now);  // wieder 3 Minuten warten
                alarm.setStatus("immer noch da");
                // wenn er schon als behoben gemeldet ist, neuen Alarm erzeugen
                mLogService.writeAlarm(alarm);
            } else {
                mLogService.writeLog(TAG, 3, "Alarm "+alarm.getParameter()+" wurde schon gemeldet");
            }
        } else {
            // neuen Alarm erzeugen, neue Alarmliste erzeugen und in der Alarm Map ablegen
            String desc = alarmDescMap.get(key);
            String soll = ACSettings.getInstance().get(key);
            alarm = new Alarm(key, desc, key, soll, value, now);
            alarm.setErstmals(nowDate);
            alarm.setStatus("neu");
            List<Alarm> alarmList = new ArrayList<Alarm>();
            alarmList.add(alarm);
            alarmlisten.put(key,alarmList);
            mLogService.writeLog(TAG, 3, "Neuer Alarm "+alarm.getParameter()+" melden");
            mLogService.writeAlarm(alarm);
        }

    }

    public void alarmTest() {

        createAlarmEvent("key_battery_level", "90");
        createAlarmEvent("key_battery_level", "90");
        createAlarmEvent("key_battery_level", "90");

    }


}
