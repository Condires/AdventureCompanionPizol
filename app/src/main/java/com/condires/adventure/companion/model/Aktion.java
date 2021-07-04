package com.condires.adventure.companion.model;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

/*
  Eine Aktion ist etwas, dass an einem bestimmten Ort passieren soll. in der Regel wird damit ein Medium
  (Ton, Bild, Video, etc gestartet)
  Aktionen definieren wann das geschehen soll.
 */
public class Aktion implements Serializable, AirtableObject {
    public static String tableName = "Aktion";
    public static int EGAL = 0;
    public static int ANKUNFT = 1;
    public static int ABFAHRT = 2;

    public static int ANSAGE = 0;  // wird im Moment nicht aktiv eingesetzt, wir arbieten mit Hello und goodby
    public static int STORY = 1;
    public static int BACKGROUND = 2;
    public static int HELLO = 3;   // am Anfang eines Weges
    public static int GOODBY = 4;  // am Ande eines Weges
    public static int ALERT = 5;
    public static int CLOSING = 6;  // ist wie GOODBY, kann aber länger sein und wird unterbrochen, aber nicht von Background

    private Date createdTime;
    @SerializedName("Name")
    private String name;

    public String id;
    @SerializedName("Ort")
    private String[] ortIds;
    private Ort ort;
    @SerializedName("Weg")// Der Ort/Weg an dem die Aktion statt findet
    private String[] wegIds;
    private transient Weg weg;          // der Weg, der zum Ort der Aktion führt
    //private int anAb = EGAL;  // ob die Aktion bei Ankunft am Ort oder beim Verlassen des Ortes stattfindet
    @SerializedName("Richtung")
    private float direction;  // in welcher Richtung kommt die Aktion?  // müsste aus der Richtung des Weges kommen
    @SerializedName("Distanz")
    private long distance;  // in welcher Distanz zum Ort kommt die Aktion?
    @SerializedName("Nach Ort")
    private String[] nachOrtIds;
    private transient Ort nachOrt;    // statt Richtung kann auch ein nachOrt als Referenz für die Richtung gegeben werden (wird durch weg ersetzt
    @SerializedName("Media Id")
    private int mediaId;
    @SerializedName("Media Type")
    private int mediaType; // 0 Sound, 1 Video,
    @SerializedName("ActionType")
    private int actionType;  // 0=ANSAGE, 1=STORY, 2=BACKGROUND
    // es gibt begrüssung, abschied, alarm, Story, Background, dh anab oder egal wird ersetzt durch den action Type.
    @SerializedName("Loop")
    // an diesem Punkt wird ie Lautstärke wieder auf Standard gesetzt
    private boolean repeat;
    @SerializedName("VolumeOn")
    private boolean volumeOn;
    @SerializedName("Folge Aktion")
    private String[] folgeAktionIds;
    private transient Aktion folgeAktion = null;
    @SerializedName("Start Verzoegerung")
    private long startDelayMeters;  // Abstand vom Startort / Startort ist der Radius des Ortes
    @SerializedName("Anlage")
    public String[] anlageIds;

    @SerializedName("Reihenfolge")
    public int reihenfolge;   // reihenfolge innerhalb der Anlage
    @SerializedName("Delta Volume")
    private int deltaVolume;

    public transient Anlage anlage;
    public Aktion() {super();}

    public Aktion(String name, Ort ort, float direction, long distance, Ort nachOrt, Weg weg, int mediaId, int mediaType, boolean repeat, Aktion folgeAktion, long startDelayMeters, int actionType, int reihenfolge, int deltaVolume, boolean volumeOn) {
        this.name = name;
        this.ort = ort;
        if (direction != 0) {
            this.direction = direction;
        } else if (nachOrt != null) {
            this.direction = ort.calculateDirection(nachOrt);
        }
        this.folgeAktion = folgeAktion;
        this.distance = distance;
        this.nachOrt = nachOrt;
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.repeat = repeat;
        this.weg = weg;
        //this.anAb = anAb;
        this.startDelayMeters = startDelayMeters;
        this.actionType = actionType;
        this.reihenfolge = reihenfolge;
        this.deltaVolume  = deltaVolume;
        this.volumeOn = volumeOn;

    }


    public Aktion(Aktion a) {
        this(a.name, a.ort, a.direction, a.distance, a.nachOrt, a.weg, a.mediaId, a.mediaType, a.repeat, a.folgeAktion, a.startDelayMeters, a.actionType, a.reihenfolge, a.deltaVolume, a.volumeOn);
    }

    public Aktion(int reihenfolge, String name, Weg weg, int mediaId, int mediaType, boolean repeat, long startDelayMeters, int actionType, int deltaVolume) {
        this.name = name;
        this.weg = weg;
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.repeat = repeat;
        //this.anAb = anAb;
        this.startDelayMeters = startDelayMeters;
        this.actionType = actionType;
        this.deltaVolume = deltaVolume;
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

    public int getReihenfolge() {
        return reihenfolge;
    }

    public void setReihenfolge(int reihenfolge) {
        this.reihenfolge = reihenfolge;
    }

    // ********** Weg
    public Weg getWeg() {
        return weg;
    }
    public void setWeg(Weg weg) {
        this.weg = weg;
    }

    public String getWegId() {
        if (wegIds != null) {
            return wegIds[0];
        }
        return  null;
    }
    public void setWegId(String wegId) {
        if (wegIds == null) {
            wegIds = new String[1];
        }
        wegIds[0] = wegId;
    }
    public void setwegIds(String[] ids) {
        wegIds = ids;
    }
    public String[] getwegIds() {
        return wegIds;
    }

    // ***** Ort

    public Ort getOrt() {
        return ort;
    }
    public void setOrt(Ort ort) {
        this.ort = ort;
    }
    public String getOrtId() {
        if (ortIds != null) {
            return ortIds[0];
        }
        return  null;
    }
    public void setOrtId(String ortId) {
        if (ortIds == null) {
            ortIds = new String[1];
        }
        ortIds[0] = ortId;
    }
    public void setOrtIds(String[] ids) {
        ortIds = ids;
    }
    public String[] getOrtIds() {
        return ortIds;
    }

    // ******* NachOrt
    public Ort getNachOrt() {
        return nachOrt;
    }
    public void setNachOrt(Ort nachOrt) {
        this.nachOrt = nachOrt;
    }
    public String getNachOrtId() {
        if (nachOrtIds != null) {
            return nachOrtIds[0];
        }
        return  null;
    }
    public void setNachOrtId(String nachOrtId) {
        if (nachOrtIds == null) {
            nachOrtIds = new String[1];
        }
        nachOrtIds[0] = nachOrtId;
    }
    public void setNachOrtIds(String[] ids) {
        nachOrtIds = ids;
    }
    public String[] getNachOrtIds() {
        return nachOrtIds;
    }

    // ************
    public Aktion getFolgeAktion() {
        return folgeAktion;
    }
    public void setFolgeAktion(Aktion folgeAktion) {
        this.folgeAktion = folgeAktion;
    }

    public String getFolgeAktionId() {
        if (folgeAktionIds != null) {
            return folgeAktionIds[0];
        }
        return  null;
    }
    public void setFolgeAktionId(String folgeAktionId) {
        if (folgeAktionIds == null) {
            folgeAktionIds = new String[1];
        }
        folgeAktionIds[0] = folgeAktionId;
    }
    public void setFolgeAktionIds(String[] ids) {
        folgeAktionIds = ids;
    }
    public String[] getFolgeAktionIds() {
        return folgeAktionIds;
    }

    // ************ Anlage Ids
    public Anlage getAnlage() {return anlage;}
    public void setAnlage(Anlage anlage) {this.anlage = anlage;}
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

    public int getMediaId() {
        return mediaId;
    }

    public void setMediaId(int mediaId) {
        this.mediaId = mediaId;
    }

    public int getMediaType() {
        return mediaType;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }


    public boolean isVolumeOn() {
        return volumeOn;
    }

    public void setVolumeOn(boolean volumeOn) {
        this.volumeOn = volumeOn;
    }

    public int getDeltaVolume() {
        return deltaVolume;
    }

    public void setDeltaVolume(int deltaVolume) {
        this.deltaVolume = deltaVolume;
    }

//public int getAnAb() {
    //    return anAb;
    //}

    //public void setAnAb(int anAb) {
     //   this.anAb = anAb;
    //}

    // wieviele Meter nach dem WegAnfang soll die Aktion starten?
    public long getStartDelayMeters() {
        return startDelayMeters;
    }

    public int getActionType() {
        return actionType;
    }

    public void setActionType(int actionType) {
        this.actionType = actionType;
    }

    public void setStartDelayMeters(long startDelayMeters) {
        this.startDelayMeters = startDelayMeters;
    }

    public Date getCreatedTime() {
        return createdTime;
    }
    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }
}
