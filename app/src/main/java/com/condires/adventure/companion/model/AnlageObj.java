package com.condires.adventure.companion.model;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/*
  War der Teil einer Anlage, der auf Airtable gespeichert wird. Das Problem, war, dass die Klasse Anlage Attribute hatte,
  die nicht auf Airtable gespeichert werdenb, zB eine Liste von Orten, das hat dazu geführt, dass die Listen bei einem call
  auch serialisiert wurden und von Airtable nicht verstanden wurden, airtable versteht nur String[] und da sind die IDs der Orte
  drin und nicht die Ort Objekte.  Konnte durch das keyword transient gelöst werden. Transiente Attribte werden nicht seraialisiert
 */
public class AnlageObj implements Serializable, AirtableObject {
    public static String tableName = "Anlage";
    private Date createdTime;
    @SerializedName("Name")
    public String name;    // Der Name der angezeigt wird
    //@SerializedName("id")
    public String id;         // eine eindeutig id über alle Anlagen hinweg
    @SerializedName("StartOrt")
    public String[] startOrtIds;
    @SerializedName("MaxStillstand")   // TODO wir ersetzt durch Settings
    public float maxStillstand = 180;   // in Sekunden, ab wann soll alles still sein, wenn wir auf einem Weg stillstehen?
    @SerializedName("Version")
    public String version;
    @SerializedName("DB Id")
    public String dbId;


    public AnlageObj() { }

    /*
    public AnlageObj(String name, String id, String startOrtIds, float maxStillstand) {
        this.name = name;
        this.id = id;
        this.startOrtIds = startOrtIds;
        this.maxStillstand = maxStillstand;
    }
    */

    public AnlageObj(Anlage anlage) {
        this.name = anlage.name;
        this.id = anlage.id;
        this.startOrtIds = anlage.startOrtIds;
        this.maxStillstand = anlage.maxStillstand;
        this.dbId = anlage.dbId;
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
        this.id =  id;
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public void setStartOrtIds(String[] startOrtIds) {
        this.startOrtIds = startOrtIds;
    }
    public String[] getStartOrtIds() {
        return startOrtIds;
    }

    public void setStartOrtId(String ids) {
        if (startOrtIds == null) {
            startOrtIds = new String[1];
        }
        startOrtIds[0] = ids;
    }
    public String getStartOrtId() {
        if (startOrtIds != null) {
            return startOrtIds[0];
        }
        return null;
    }

    public float getMaxStillstand() {
        return maxStillstand;
    }

    public void setMaxStillstand(float maxStillstand) {
        this.maxStillstand = maxStillstand;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
