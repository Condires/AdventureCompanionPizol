package com.condires.adventure.companion.setting;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.condires.adventure.companion.MainActivity;
import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/*
 * die Klasse soll ein Wrapper über das Perference APIs sein, für das Programm soll es irrelevant sein
 * ob die Daten in Airtable der lokalen db oder als Preference gespeichert sind.
 * Die Preferenzen werden von den settern der Klasse diret in die Preferenzen geschriebne und beim lesen direkt
 * aus den Preferenzen geholt.
 * TODO: beim leswen der Preferenzen könnten die lokalen Variablen nachgeführt werden
 * TODO: sollte eigentlich ein singleton sein, geht das mit Airtable?
 */
public class ACSettings implements Serializable, AirtableObject {

    @Expose(serialize = false)
    public static String tableName = "Einstellungen";
    @Expose(serialize = false)
    private static String TAG = "ACSettings";

    private Date createdTime;

    private String id;
    @SerializedName("Name")
    private String name;
    @SerializedName("Volume")
    private int volume;                // Lautstärke der Musik
    @SerializedName("Caller")
    private String keyCaller;
    // Liste der Telefonnummern, die via SMS steuern dürfen
    @SerializedName("Notfall Nummer")
    private String keyNotfallNummer;// Telefonnummer die eine SMS bekommt, wenn etwas ganz schief geht

    @SerializedName("Default Anlage")
    private String defaultAnlage;

    @SerializedName("Sekunden bis Notstop")
    private int timeToNotstop;
    @SerializedName("Akku Alarm Level")
    private int batteryAlarmLevel;
    @SerializedName("Akku Alarm Temp")
    private int batteryAlarmTemp;
    @SerializedName("Internet Timeout")
    private int internetTimeout;
    @SerializedName("SMS Signatur")
    private String smsSignature;
    @SerializedName("Restart Delay")   // minimaler Abstand in Sekunden zwischen zwei GPS Restart Versuchen
    private int restartDelaySec;
    @SerializedName("Restart Distance")   // maximal Abstand ohne Weg, bis Restart gemacht wird
    private int restartDistanceM;
    @SerializedName("Dist Log m")    // Anzahl Meter in der die Distanz und Richtung ins Log geschrieben wird
    private int distLogDist;
    @SerializedName("Max WLAN Dist m")    // Anzahl Meter in der die Distanz und Richtung ins Log geschrieben wird
    private int maxWlanDistance;
    @SerializedName("GPS min Speed ms")    // minimale Geschwindigkeit dass ich GPS Signal traue
    private float gpsMinSpeed;
    @SerializedName("WLAN")
    private int isWLANMaster;           // 1 wlan ist master 2 gps ist Master
    @SerializedName("Min Verweilzeit s")
    private int minVerweilzeitSec;

    //TODO toleranz für Richtung als Setting definieren Klasse Anlage ganz unten

    private transient int airtableLogSize = 48000;        // Bytes maximale grösse des LogBuffers für Airtable Logs
    private transient int airtableLogWriteDelay = 15000;  // nur alle 15000ms  Log auf Airtable schreiben
    private transient int btLogSize = 1000;               // Bytes max grösse des Bluetooth Logs Buffers
    private transient int btLogDelay = 3130;              // in ms
    private transient int btScreenUpdateDelay = 2355;     // in ms
    private transient int airtableAkkuAlarmWriteDelay = 300;  // Sekunden, bis das nächste Mal ein Alarm auf Airtable geschrieben wird
    private transient String preferredWifiSSID = "Pizol_Betrieb_1";   // kann pro Ort übersteuert werden$
    //private transient int maxWlanDistance = 100;        // WLAN wird frühestens 100m vor dem Zielort erkannt


    // APi Key und Database muss auf dem Player manuell eingegeben werden
    private transient String airtableApiKey;       //
    private transient String airtableBaseName;

    private transient SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.getContext());
    private transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static ACSettings instance;
    // Verhindere die Erzeugung des Objektes über andere Methoden
    public ACSettings() {
        if (instance == null) {
            instance = this;
        }
    }
    // Eine Zugriffsmethode auf Klassenebene, welches dir '''einmal''' ein konkretes
    // Objekt erzeugt und dieses zurückliefert.



    public static ACSettings getInstance () {
        if (ACSettings.instance == null) {
            ACSettings.instance = new ACSettings();
        }
        return ACSettings.instance;
    }

    public String getAsJson() {
        setName(getName());
        setId(getId());
        setVolume(getVolume());
        setKeyCaller(getKeyCaller());
        setKeyNotfallNummer(getKeyNotfallNummer());
        setDefaultAnlage(getDefaultAnlage());
        setTimeToNotstop(getTimeToNotstop());
        setRestartDelaySec(getRestartDelaySec());
        setRestartDistanceM(getRestartDistanceM());
        setDistLogDist(getDistLogDist());
        setBatteryAlarmLevel(getBatteryAlarmLevel());
        setBatteryAlarmTemp(getBatteryAlarmTemp());
        setInternetTimeout(getInternetTimeout());
        setMaxWlanDistance(getMaxWlanDistance());
        setGpsMinSpeed(getGpsMinSpeed());

        String json = gson.toJson(this);
        return json;
    }

    public String getId() {
        return id;
    }

    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    @Override
    public String getTableName() {
        return tableName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void storeToPref(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value); // value to store
        editor.commit();
    }

    public String get(String key) {
        return prefs.getString(key, "");
    }


    public void set() {
        /*
        //https://www.b4x.com/android/forum/threads/audio-safe-volume.103515/
        mSafeMediaVolumeState = new Integer(Settings.Global.getInt(mContentResolver,
                Settings.Global.AUDIO_SAFE_VOLUME_STATE,
                SAFE_MEDIA_VOLUME_NOT_CONFIGURED));
                */

    }



    public int getAirtableLogSize() {
        return airtableLogSize;
    }

    public void setAirtableLogSize(int airtableLogSize) {
        this.airtableLogSize = airtableLogSize;
    }

    public int getAirtableLogWriteDelay() {
        return airtableLogWriteDelay;
    }

    public void setAirtableLogWriteDelay(int airtableLogWriteDelay) {
        this.airtableLogWriteDelay = airtableLogWriteDelay;
    }

    public int getAirtableAkkuAlarmWriteDelay() {
        return airtableAkkuAlarmWriteDelay;
    }

    public void setAirtableAkkuAlarmWriteDelay(int airtableAkkuAlarmWriteDelay) {
        this.airtableAkkuAlarmWriteDelay = airtableAkkuAlarmWriteDelay;
    }

    public int getBtLogSize() {
        return btLogSize;
    }

    public void setBtLogSize(int bTLogSize) {
        this.btLogSize = bTLogSize;
    }

    public int getBtLogDelay() {
        return btLogDelay;
    }

    public void setBtLogDelay(int btLogDelay) {
        this.btLogDelay = btLogDelay;
    }

    public int getBtScreenUpdateDelay() {
        return btScreenUpdateDelay;
    }

    public void setBtScreenUpdateDelay(int btScreenUpdateDelay) {
        this.btScreenUpdateDelay = btScreenUpdateDelay;
    }


    public String getPreferredWifiSSID() {
        return preferredWifiSSID;
    }

    public void setPreferredWifiSSID(String preferredWifiSSID) {
        this.preferredWifiSSID = preferredWifiSSID;
    }

    /*
     * prefs
     */

    public int getVolume() {
        String stringValue = prefs.getString("key_volume", "10");
        this.volume = Integer.parseInt(stringValue);
        return this.volume;
    }
    public void setVolume(int volume) {
        this.volume = volume;
        storeToPref("key_volume", Integer.toString(volume)); // value to store
    }


    public String getSMSSignature() {
        return prefs.getString("key_sms_signature", "Wangs:");
    }

    public void setSMSSignature(String smsSignature) {
        this.smsSignature = smsSignature;
        storeToPref("key_sms_signature", smsSignature);
    }

    public String getKeyCaller() {
        this.keyCaller = prefs.getString("key_caller", "+41795432109");
        return this.keyCaller;
    }

    public void setKeyCaller(String keyCaller) {
        this.keyCaller = keyCaller;
        storeToPref("key_caller", keyCaller);
    }

    public String getKeyNotfallNummer() {
        this.keyNotfallNummer = prefs.getString("key_notfall_nummer", "+41795432109");
        return this.keyNotfallNummer;
    }
    public void setKeyNotfallNummer(String keyNotfallNummer) {
        this.keyNotfallNummer = keyNotfallNummer;
        storeToPref("key_notfall_nummer", keyNotfallNummer);
    }
    // ***********


    public String getDefaultAnlage() {
        this.defaultAnlage = prefs.getString("key_default_anlage", "Pizolbahn Wangs");
        return this.defaultAnlage;
    }

    public void setDefaultAnlage(String defaultAnlage) {
        this.defaultAnlage = defaultAnlage;
        storeToPref("key_default_anlage", defaultAnlage);
    }

    // ***********
    public int getTimeToNotstop() {
        String value =  prefs.getString("key_time_to_notstop", "180");
        timeToNotstop = Integer.parseInt(value);
        return timeToNotstop;
    }

    public void setTimeToNotstop(int timeToNotstop) {
        this.timeToNotstop = timeToNotstop;
        storeToPref("key_time_to_notstop", Integer.toString(timeToNotstop)); // value to store
    }

    public int getMinVerweilzeitSec() {
        String value =  prefs.getString("key_min_verweilzeit_sec", "60");
        minVerweilzeitSec = Integer.parseInt(value);
        return minVerweilzeitSec;
    }

    public void setMinVerweilzeitSec(int minVerweilzeitSec) {
        this.minVerweilzeitSec = minVerweilzeitSec;
        storeToPref("key_min_verweilzeit_sec", Integer.toString(minVerweilzeitSec)); // value to store
    }

    public int getRestartDelaySec() {
        String value =  prefs.getString("key_restart_delay_sec", "180");
        restartDelaySec = Integer.parseInt(value);
        return restartDelaySec;
    }

    public void setRestartDelaySec(int restartDelaySec) {
        this.restartDelaySec = restartDelaySec;
        storeToPref("key_restart_delay_sec", Integer.toString(restartDelaySec)); // value to store
    }

    public int getRestartDistanceM() {
        String value =  prefs.getString("key_restart_distance_m", "300");
        restartDistanceM = Integer.parseInt(value);
        return restartDistanceM;
    }

    public void setRestartDistanceM(int restartDistanceM) {
        this.restartDistanceM = restartDistanceM;
        storeToPref("key_restart_distance_m", Integer.toString(restartDistanceM)); // value to store
    }



    public float getGpsMinSpeed() {
        String value =  prefs.getString("key_gps_min_speed", "0.9");
        gpsMinSpeed = Float.parseFloat(value);
        if (gpsMinSpeed == 0) {
            gpsMinSpeed = 0.5f;
        }
        return gpsMinSpeed;
    }

    public void setGpsMinSpeed(float gpsMinSpeed) {
        this.gpsMinSpeed = gpsMinSpeed;
        storeToPref("key_gps_min_speed", Float.toString(gpsMinSpeed)); // value to store
    }

    public int getMaxWlanDistance() {
        String value =  prefs.getString("key_wlan_distance_m", "200");
        maxWlanDistance = Integer.parseInt(value);
        if (maxWlanDistance == 0) {
            maxWlanDistance  = 200;
        }
        return maxWlanDistance;
    }

    public void setMaxWlanDistance(int maxWlanDistance) {
        this.maxWlanDistance = maxWlanDistance;
        storeToPref("key_wlan_distance_m", Integer.toString(maxWlanDistance)); // value to store
    }

    public int getIsWLANMaster() {
        String value =  prefs.getString("key_wlan_master_i", "1");
        isWLANMaster = Integer.parseInt(value);
        if (isWLANMaster == 0) {
            isWLANMaster  = 1;
        }
        return isWLANMaster;
    }

    public void setIsWLANMaster(int isWLANMaster) {
        this.isWLANMaster = isWLANMaster;
        storeToPref("key_wlan_master_i", Integer.toString(isWLANMaster)); // value to store
    }

    public int getDistLogDist() {
        String value =  prefs.getString("key_dist_log_m", "300");
        distLogDist = Integer.parseInt(value);
        return distLogDist;
    }

    public void setDistLogDist(int distLogDist) {
        this.distLogDist = distLogDist;
        storeToPref("key_dist_log_m", Integer.toString(distLogDist)); // value to store
    }


    public int getBatteryAlarmLevel() {
        String value =  prefs.getString("key_battery_level", "1");
        batteryAlarmLevel = Integer.parseInt(value);
        return batteryAlarmLevel;
    }

    public void setBatteryAlarmLevel(int batteryAlarmLevel) {
        this.batteryAlarmLevel = batteryAlarmLevel;
        storeToPref("key_battery_level", Integer.toString(batteryAlarmLevel)); // value to store
    }

    public int getBatteryAlarmTemp() {
        String value =  prefs.getString("key_battery_temp", "1");
        batteryAlarmTemp = Integer.parseInt(value);
        return batteryAlarmTemp;
    }

    public void setBatteryAlarmTemp(int batteryAlarmTemp) {
        this.batteryAlarmTemp = batteryAlarmTemp;
        storeToPref("key_battery_temp", Integer.toString(batteryAlarmTemp)); // value to store
    }

    public int getInternetTimeout() {
        String value =  prefs.getString("key_internet_timeout", "30");
        internetTimeout = Integer.parseInt(value);
        return internetTimeout;
    }

    public void setInternetTimeout(int internetTimeout) {
        this.internetTimeout = internetTimeout;
        storeToPref("key_internet_timeout", Integer.toString(internetTimeout)); // value to store
    }


    // ***********
    /*
     * In Zukunft wird jeder Kunde seine eigene Airtabl haben, die Airtable Keys können von der
     * Companion Adventure Table geholt werde, deren Key muss im Programm fix programmiert sein
     */
    public String getAirtableApiKey() {
        return prefs.getString("key_apikey", "");
    }

    public void setAirtableApiKey(String airtableApiKey) {
        this.airtableApiKey = airtableApiKey;
        storeToPref("key_apikey", keyCaller);
    }

    public String getAirtableBaseName() {
        return prefs.getString("key_base_name", "");
    }

    public void setAirtableBaseName(String airtableBaseName) {
        this.airtableBaseName = airtableBaseName;
        storeToPref("key_base_name", keyCaller);
    }


}
