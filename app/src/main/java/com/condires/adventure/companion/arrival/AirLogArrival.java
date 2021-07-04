package com.condires.adventure.companion.arrival;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AirLogArrival implements AirtableObject {
    public static String tableName = "Position";
    @SerializedName("Name")
    public String name;    // Der Name der angezeigt wird
    private Date createTime;
    @SerializedName("id")
    public String id;         // eine eindeutige id über alle Anlagen hinweg

    @SerializedName("Player")
    public String device;
    @SerializedName("Speed")
    public float speed;

    @SerializedName("Time")
    public Date now;

    @SerializedName("Ziel Ort")
    public String location;

    @SerializedName("Distanz")
    public double distance;

    @SerializedName("Ankunft")
    public Date ankunft;



    public AirLogArrival() {

    }

    public String getTableName() { return tableName;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
        // speed ist hie rin km/h wir rechnen um in m/s
        if (speed > 0) {
            float dauer = (long)distance/(speed/3.6f);  // sekunden
            LocalDateTime timenow = LocalDateTime.now();
            LocalDateTime myDateTime = timenow.plusSeconds((long)dauer);
            ankunft = Date.from(myDateTime.atZone(ZoneId.systemDefault()).toInstant());
            //java.util.Date date = java.sql.Date.valueOf(LocalDateTime.now());
            //myDateTime = timenow.plusSeconds((long)dauer);
            ankunft = new Date(System.currentTimeMillis() + (int) dauer*1000);
        }
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getCreatedTime() {
        return createTime;
    }

    public void setCreatedTime(Date createTime) {
        this.createTime = createTime;
    }
}
