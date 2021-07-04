package com.condires.adventure.companion.model;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;
/*
 * Das Media Objekt dient der Speicherung von MediaIds und Dateinamen auf Airtable, damit die MediaDataien
 * auch auf Airtable konfiguriert werden können
 */
public class MediaObject implements Serializable, AirtableObject {
    public static String tableName = "Medien";
    private Date createdTime;
    @SerializedName("Name")
    String mediaName;
    //@SerializedName("id")
    public String id;         // eine eindeutig id über alle Anlagen hinweg
    @SerializedName("Media Id")
    int mediaId;

    public MediaObject() {}

    public MediaObject(String mediaName, int mediaId) {
        this.mediaId = mediaId;
        this.mediaName = mediaName;
    }
    public String getTableName() { return tableName;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id =  id;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public String getMediaName() {
        return mediaName;
    }

    public void setMediaName(String mediaName) {
        this.mediaName = mediaName;
    }
}
