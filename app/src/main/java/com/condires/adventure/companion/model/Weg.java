package com.condires.adventure.companion.model;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Weg extends IrgendWo implements AirtableObject {
    @Expose(serialize = false)
    public static String tableName = "Weg";
    @Expose(serialize = false)
    private static String TAG = "Weg";

    public String id;         // eine eindeutig id über alle Objekte hinweg


    private Date createdTime;

    @SerializedName("VonOrt")
    private String[] vonOrtIds;
    private transient Ort vonOrt;
    @SerializedName("NachOrt")
    private String[] nachOrtIds;
    private transient Ort nachOrt;
    @SerializedName("Richtung")
    private float direction;
    @SerializedName("Naechster Weg")
    private String[] nextWegIds;
    private transient Weg nextWeg;
    //private String name;
    // der Radius des Nachortes kann auf dem Weg überschrieben werden, er bestimmt in welchem Abstand vom
    // Ziel wir sagen, dass wir den Ort erreicht haben
    @SerializedName("Nach Ort Radius")
    private long radiusNachOrt;

    @SerializedName("Anlage")
    public String[] anlageIds;


    @SerializedName("Background Id")
    public int backgroundMediaId;
    @SerializedName("Background Type")
    public int backgroundMediaType; // 0 Sound, 1 Video,


    public Weg() {
        super();
    }


    public Weg(Ort vonOrt, Ort nachOrt, String name) {

        this.name = name;
        this.vonOrt = vonOrt;
        this.nachOrt = nachOrt;
        this.direction = vonOrt.calculateDirection(nachOrt);
    }

    public Weg(Ort vonOrt) {

        this.name = "noname";
        this.vonOrt = vonOrt;
    }

    public Weg(Ort vonOrt, Ort nachOrt) {
        this(vonOrt, nachOrt, "noname");
        String vonName = "unknown";
        if (vonOrt != null) {
            vonName = vonOrt.getName();

        }
        String nachName = "unknown";
        if (nachOrt != null) {
            nachName = nachOrt.getName();
            radiusNachOrt = nachOrt.getRadius();
        }
        this.setName(vonName +" nach " + nachName);

    }

    public String toString() {
        return "Name:"+name +
                "\nRadius bei Ankunft:"+this.radiusNachOrt;
    }

    public void resetWegName() {
        this.setName(vonOrt.getName() + " nach " + nachOrt.getName());
    }

    public long calculateLength() {
        Ort vonOrt = getVonOrt();
        if (vonOrt != null) {
            return vonOrt.calculateDistance(nachOrt.getLatitude(), nachOrt.getLongitude());
        }
        return 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }




    public String getTableName() { return tableName;}

    public String getVonOrtId() {
        if (vonOrtIds != null) {
            return vonOrtIds[0];
        }
        return  null;
    }

    public void setVonOrtId(String vonOrtId) {
        if (vonOrtIds == null) {
            vonOrtIds = new String[1];
        }
        vonOrtIds[0] = vonOrtId;
    }

    public void setVonOrtIds(String[] ids) {
        vonOrtIds = ids;
    }
    public String[] getVonOrtIds() {
        return vonOrtIds;
    }

    public String getNachOrtId() {
        if (nachOrtIds != null) {
            return nachOrtIds[0];
        }
        return null;
    }

    public void setNachOrtId(String nachOrtId) {
        if (nachOrtIds == null) {
            nachOrtIds = new String[1];
        }
        this.nachOrtIds[0] = nachOrtId;
    }

    public void setNachOrtIds(String[] ids) {
        nachOrtIds = ids;
    }
    public String[] getNachOrtIds() {
        return nachOrtIds;
    }

    public String getNextWegId() {
        if (nextWegIds != null) {
            return nextWegIds[0];
        }
        return null;
    }

    public void setNextWegId(String nextWegId) {
        if (nextWegIds == null) {
            nextWegIds = new String[1];
        }
        this.nextWegIds[0] = nextWegId;
    }

    public Weg getNextWeg() {
        return nextWeg;
    }

    public void setNextWeg(Weg nextWeg) {
        this.nextWeg = nextWeg;
    }

    public String[] getNextWegIds() {
        return nextWegIds;
    }

    public void setNextWegIds(String[] nextWegIds) {
        this.nextWegIds = nextWegIds;
    }

    public Ort getVonOrt() {
        return vonOrt;
    }

    public void setVonOrt(Ort vonOrt) {
        this.vonOrt = vonOrt;
    }

    public Ort getNachOrt() {
        return nachOrt;
    }

    public void setNachOrt(Ort nachOrt) {
        this.nachOrt = nachOrt;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }


    public long getRadiusNachOrt() {
        return radiusNachOrt;
    }

    public void setRadiusNachOrt(long radiusNachOrt) {
        this.radiusNachOrt = radiusNachOrt;
    }

    /*
            Ist der Punkt auf diesem Weg?
             */
    public boolean isOnWeg(double latitude, double longitude) {
        Ort o = new Ort(latitude, longitude);
        return o.isOnLine(vonOrt, nachOrt);
    }


    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String[] getAnlageIds() {
        return anlageIds;
    }
    public void setAnlageIds(String[] anlageIds) {
        this.anlageIds = anlageIds;
    }

    public void setAnlageId(String anlageId) {
        if (anlageIds == null) {
            anlageIds = new String[1];
        }
        this.anlageIds[0] =  anlageId;
    }



    public int getBackgroundMediaId() {
        return backgroundMediaId;
    }

    public void setBackgroundMediaId(int backgroundMediaId) {
        this.backgroundMediaId = backgroundMediaId;
    }

    public int getBackgroundMediaType() {
        return backgroundMediaType;
    }

    public void setBackgroundMediaType(int backgroundMediaType) {
        this.backgroundMediaType = backgroundMediaType;
    }

}
