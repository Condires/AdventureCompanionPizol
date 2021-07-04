package com.condires.adventure.companion.model;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

class GPSTrack implements AirtableObject, Serializable {

    public static String tableName = "GPSTrack";
    private Date createdTime;
    @SerializedName("Name")
    public String name;    // Der Name der angezeigt wird
    //@SerializedName("id")
    public String id;         // eine eindeutig id über alle Anlagen hinweg
    @SerializedName("Device")
    public String Device;
    @SerializedName("LogDate")
    public Date LogDate;

    public String getDevice() {
        return Device;
    }

    public void setDevice(String device) {
        Device = device;
    }

    public Date getLogDate() {
        return LogDate;
    }

    public void setLogDate(Date logDate) {
        LogDate = logDate;
    }

    public String getTrack1() {
        return Track1;
    }

    public void setTrack1(String track1) {
        Track1 = track1;
    }

    public String getTrack2() {
        return Track2;
    }

    public void setTrack2(String track2) {
        Track2 = track2;
    }

    @SerializedName("Track1")
    public String Track1;
    @SerializedName("Track2")
    public String Track2;


    public String getTableName() { return tableName;}

    @Override
    public Date getCreatedTime() {
        return createdTime;
    }

    @Override
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
