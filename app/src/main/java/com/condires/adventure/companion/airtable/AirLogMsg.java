package com.condires.adventure.companion.airtable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/*
 * Logmeldungen werden in eine Airtable geschribene, Logmeldungen einer ganzen Runde werden gesammelt und an der Talstation
 * sobald eine Internetverbindung existiert, rausgeschrieben
 */
public class AirLogMsg implements Serializable, AirtableObject {
    public static String tableName = "Log";
    private Date createdTime;
    @SerializedName("Name")
    public String name;    // Der Name der angezeigt wird
    @Expose
    @SerializedName("id")
    public String id;         // eine eindeutige id über alle Anlagen hinweg

    @SerializedName("Screen")
    public String screen;
    @SerializedName("Device")
    public String device;
    @SerializedName("Log")
    public String msg;
    @SerializedName("Status")
    public String status;
    @SerializedName("StatusMonitor")   // dasselbe wie Status aber ohne Feldnamen
    public String statusMonitor;
    @SerializedName("Long Log")
    public String msgType4;
    @SerializedName("Log0")
    public String msg0;
    @SerializedName("Log1")
    public String msg1;
    @SerializedName("Log2")
    public String msg2;
    @SerializedName("Log3")
    public String msg3;
    @SerializedName("Log4")
    public String msg4;
    @SerializedName("JSON")
    public String json;
    @SerializedName("StackTrace")
    public String stackTrace;
    @SerializedName("DB Log")
    public String DB;
    //TODO GPS Log Message einbauen

    public AirLogMsg() {
    }

    public AirLogMsg(String name, String device, String msg) {
        this.name = name;
        this.device = device;
        this.msg = msg;
    }
    public AirLogMsg(String name, String device, int logPointer, String msg) {
        this.name = name;
        this.device = device;
        this.msg = msg;
        this.setMgs(logPointer, msg);
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

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getMsgType4() {
        return msgType4;
    }

    public void setMsgType4(String msgType4) {
        this.msgType4 = msgType4;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusMonitor() {
        return statusMonitor;
    }

    public void setStatusMonitor(String statusMonitor) {
        this.statusMonitor = statusMonitor;
    }

    public String getDB() {
        return DB;
    }

    public void setDB(String DB) {
        this.DB = DB;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setMgs(int pos, String msg) {
        this.msg = msg;
        switch (pos) {
            case 0: msg0 = msg; break;
            case 1: msg1 = msg; break;
            case 2: msg2 = msg; break;
            case 3: msg3 = msg; break;
            case 4: msg4 = msg; break;
        }
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
    // TODO solange es keine setter für Log1, Log2 etc gibt, werden sie von Airtable nicht in den Record zurückgeschrieben
}
