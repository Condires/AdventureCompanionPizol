package com.condires.adventure.companion.bluetooth;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StatusDAO implements Serializable {

    @Expose(serialize = false)
    private static String TAG = "Ort";

    @SerializedName("PL")
    private String player;
    @SerializedName("T")
    private String temperatur;
    @SerializedName("TM")
    private String maxTemperatur;
    @SerializedName("L")
    private String batteryLevel;
    @SerializedName("LM")
    private String maxBatteryLevel;
    @SerializedName("GP")
    private String gpsCount;
    @SerializedName("WS")
    private String wlanStatus;
    @SerializedName("WC")
    private String wlanConnected;
    @SerializedName("BS")
    private String bluetoothStatus;
    @SerializedName("BC")
    private String bluetoothConnected;
    @SerializedName("O")
    private String ort;
    @SerializedName("AZ")
    private String abstandZiel;
    @SerializedName("M")
    private String mediaName;
    @SerializedName("V")
    private String volume;

    public String getMiniScreen() {
        String screen =
                 "\nPlayer : " +player
                + "\nOrt : " + ort
                 + "\nGPS Count : " + gpsCount
                 + "\nOrt : " + ort
                         + "\nVolume : " + volume
                         + "\nTemp : " + temperatur
                         + "\nLadestand : " + batteryLevel
                         + "\nWLAN : " + wlanStatus
                         + "\nWLAN : " + wlanConnected
                         + "\nBluetooth : " + bluetoothStatus
                         + "\nBluetooth : " + bluetoothConnected
                         + "\nMedium : " + mediaName;
        return screen;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getTemperatur() {
        return temperatur;
    }

    public void setTemperatur(String temperatur) {
        this.temperatur = temperatur;
    }

    public String getMaxTemperatur() {
        return maxTemperatur;
    }

    public void setMaxTemperatur(String maxTemperatur) {
        this.maxTemperatur = maxTemperatur;
    }

    public String getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(String batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getMaxBatteryLevel() {
        return maxBatteryLevel;
    }

    public void setMaxBatteryLevel(String maxBatteryLevel) {
        this.maxBatteryLevel = maxBatteryLevel;
    }

    public String getGpsCount() {
        return gpsCount;
    }

    public void setGpsCount(String gpsCount) {
        this.gpsCount = gpsCount;
    }

    public String getWlanStatus() {
        return wlanStatus;
    }

    public void setWlanStatus(String wlanStatus) {
        this.wlanStatus = wlanStatus;
    }

    public String getWlanConnected() {
        return wlanConnected;
    }

    public void setWlanConnected(String wlanConnected) {
        this.wlanConnected = wlanConnected;
    }

    public String getBluetoothStatus() {
        return bluetoothStatus;
    }

    public void setBluetoothStatus(String bluetoothStatus) {
        this.bluetoothStatus = bluetoothStatus;
    }

    public String getBluetoothConnected() {
        return bluetoothConnected;
    }

    public void setBluetoothConnected(String bluetoothConnected) {
        this.bluetoothConnected = bluetoothConnected;
    }

    public String getOrt() {
        return ort;
    }

    public void setOrt(String ort) {
        this.ort = ort;
    }

    public String getAbstandZiel() {
        return abstandZiel;
    }

    public void setAbstandZiel(String abstandZiel) {
        this.abstandZiel = abstandZiel;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }
}
