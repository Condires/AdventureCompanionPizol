package com.condires.adventure.companion.alarm;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
/*
 * Alarme sollten eher selten sein, wenn aber ein Event auftritt bleibt er über längere Zeit bestehen
 * es muss verhindert werden, dass dieselbe Siuation immer wieder gemeldet wird
 */

public class Alarm implements Serializable, AirtableObject {
    public static String tableName = "Alarm";
    @Expose(serialize = false)
    private static String TAG = "Alarm";

    public String id;         // eine eindeutig id über alle Objekte hinweg, wird von airtable gesetzt
    private Date createdTime;
    @SerializedName("Name")
    private String name;


    @SerializedName("Player")
    public String device;
    @SerializedName("Beschreibung")
    private String description;
    @SerializedName("Parameter")
    private String parameter;

    @SerializedName("Soll Wert")
    private String sollWert;
    @SerializedName("Ist Wert")
    private String currentValue;

    @SerializedName("Status")
    private String status;

    @SerializedName("Timestamp")
    private long timestamp;

    @SerializedName("Erstmals")
    private String erstmals;

    @SerializedName("Letztmals")
    private String letztmals;

    @SerializedName("Save Time")
    private long airtableWriteTime;  // wann wurde er auf Airtable geschrieben

    public Alarm() {}

    @Override
    public String toString() {
        return "Alarm{" +
                "id='" + id + '\'' +
                ", createdTime=" + createdTime +
                ", name='" + name + '\'' +
                ", device='" + device + '\'' +
                ", description='" + description + '\'' +
                ", parameter='" + parameter + '\'' +
                ", sollWert='" + sollWert + '\'' +
                ", currentValue='" + currentValue + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public String getTableName() { return tableName;}

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id =  id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Date getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }



    public Alarm(String name, String description, String parameter, String sollWert, String currentValue, long timestamp) {
        this.name = name;
        this.description = description;
        this.parameter = parameter;
        this.sollWert = sollWert;
        this.currentValue = currentValue;
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getAirtableWriteTime() {
        return airtableWriteTime;
    }

    public void setAirtableWriteTime(long airtableWriteTime) {
        this.airtableWriteTime = airtableWriteTime;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getSollWert() {
        return sollWert;
    }

    public void setSollWert(String sollWert) {
        this.sollWert = sollWert;
    }

    public String getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(String currentValue) {
        this.currentValue = currentValue;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErstmals() {
        return erstmals;
    }

    public void setErstmals(String erstmals) {
        this.erstmals = erstmals;
    }

    public String getLetztmals() {
        return letztmals;
    }

    public void setLetztmals(String letztmals) {
        this.letztmals = letztmals;
    }
}
