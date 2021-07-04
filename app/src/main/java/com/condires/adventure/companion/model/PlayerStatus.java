package com.condires.adventure.companion.model;

import java.util.List;

public class PlayerStatus {
    public static int STATUS_UNKNOWN = 0;
    public static int STATUS_WEG = 1;
    public static int STATUS_JOURNEY = 2;

    private double homeLatitude = 47.028888;
    private double homeLongitude = 9.432332;
    private double accuracy;  // Genauigkeit der Position
    private double latitude;
    private double longitude;
    private float  speed;
    private double distHome;
    private double altitude;
    private float  direction;
    private int    count = 0;
    private int    satelites;
    private String provider;

    private String msg;
    boolean ortVerlassen;   // beim verlassen true, bei ankunft false

    String  playerName;
    long    startStillstand;  // Zeit in ms wenn ein Stillstand auf dem Weg started
    boolean notStop;          // Sound wurde wegen langen Stillstand abgeschaltet
    long    dauerBisNotstop;               //
    boolean initDone = false;
    int     status   = STATUS_UNKNOWN;
    double  abstandZiel;
    double  abstandStart;
    IrgendWo     ort;         // ist ein Ort oder ein Weg
    Ort     zielOrt;     // ist immer ein Ort
    long    dauerBisZiel;
    int     wegSpeed;         // Durchschnittliche Geschwindigkeit auf dem Weg
    List<Ereignis> journey;
    long    nextGPSCheck;
    long    angekommenUm;
    long    abgefahrenUm;
    long    ereignisEnde;   // Zeit in Milisekunden bis zum Ende des Ereignisses
    boolean backgroundIsPause = false;
    Ereignis currentEreignis;
    Ereignis lastEreignis;
    int volume;
    boolean wegneu = false;      // true, wenn wir ganz frisch auf dem Weg sind
    private IrgendWo letzterOrt;  // der Ort an dem ich vorher war
    private Weg letzterWeg;      // der Weg auf dem er schon war
    float  temperatur;               // Akku Temperatur
    String nowDateTime;         // Formatierter Zeitstempel 17.07.2019 15:19:58
    String nowTime;             // Formatiert  nur aktuelle Zeit
    long   now;                 // Zeit ms beim start des eventloops, same wie nowDateTime
    float  batterieStatus;       // ladezustand ders Akkus
    String wlanStatus;
    String wifiSSID;
    int    wifiLevel;            // Stärke des WLANs 1..5
    String MACAddress;             // die aktuelle MAC Adresse des WIFIs
    String lastMACAddress;       // letzte MACAdresse zu der connected wurde, um einen reconecct zu erkennen
    String bluetoothStatus;
    String currentMediaName;
    boolean landing;            // zwischen WebNachOrtRadius und OrtRadius ist man am Landen
    double  deltaPos;           // m distanz zwischen zwei GPS Werten
    long   deltaT;              // s  zwioschen zwei GPS Punkten
    float  speedCalc;           // berechneter Speed szwischen zwei GPS Punkten
    String GPSFrom;             // aus welcher Prozedure kommt das GPS Signal?
    boolean GPSTracking;

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getBluetoothStatus() {
        return bluetoothStatus;
    }

    public void setBluetoothStatus(String bluetoothStatus) {
        this.bluetoothStatus = bluetoothStatus;
    }

    public boolean isWegneu() {
        return wegneu;
    }

    public void setWegneu(boolean wegneu) {
        this.wegneu = wegneu;
    }

    public Ereignis getCurrentEreignis() {
        return currentEreignis;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setCurrentEreignis(Ereignis currentEreignis) {
        this.currentEreignis = currentEreignis;
    }

    public Ereignis getLastEreignis() {
        return lastEreignis;
    }

    public void setLastEreignis(Ereignis lastEreignis) {
        this.lastEreignis = lastEreignis;
    }

    public long getEreignisEnde() {
        return ereignisEnde;
    }
    public void setEreignisEnde(long ereignisEnde) {
        this.ereignisEnde = ereignisEnde;
    }

    public String getCurrentMediaName() {
        return currentMediaName;
    }

    public void setCurrentMediaName(String currentMediaName) {
        this.currentMediaName = currentMediaName;
    }

    public boolean isBackgroundIsPause() {
        return backgroundIsPause;
    }

    public void setBackgroundIsPause(boolean backgroundIsPause) {
        this.backgroundIsPause = backgroundIsPause;
    }

    public long getAngekommenUm() {
        return angekommenUm;
    }
    public void setAngekommenUm(long angekommenUm) {
        this.angekommenUm = angekommenUm;
    }

    public long getAbgefahrenUm() {
        return abgefahrenUm;
    }
    public void setAbgefahrenUm(long abgefahrenUm) {
        this.abgefahrenUm = abgefahrenUm;
    }

    public int getWegSpeed() {
        return wegSpeed;
    }

    public void setWegSpeed(int wegSpeed) {
        this.wegSpeed = wegSpeed;
    }

    public boolean isOrtVerlassen() {
        return ortVerlassen;
    }

    public void setOrtVerlassen(boolean ortVerlassen) {
        this.ortVerlassen = ortVerlassen;
    }

    public boolean isInitDone() {
        return initDone;
    }
    public void setInitDone(boolean initDone) {
        this.initDone = initDone;
    }

    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isLanding() {
        return landing;
    }

    public void setLanding(boolean landing) {
        this.landing = landing;
    }

    public double getAbstandZiel() {
        return abstandZiel;
    }
    public void setAbstandZiel(double abstandZiel) {
        this.abstandZiel = abstandZiel;
    }

    public double getAbstandStart() {
        return abstandStart;
    }

    public void setAbstandStart(double abstandStart) {
        this.abstandStart = abstandStart;
    }

    public IrgendWo getOrt() {
        return ort;
    }
    public void setOrt(IrgendWo ort) {
        // wenn wir den Ort wechseln, dann müssen wir den jetzigen Ort als letzetn ort speichern
        /*
        if (ort != letzterOrt) {
            letzterOrt = ort;
        }
        */
        this.ort = ort;
    }
    public IrgendWo getLetzterOrt() {
        return letzterOrt;
    }

    public Weg getLetzterWeg() {
        return letzterWeg;
    }

    public void setLetzterWeg(Weg letzterWeg) {
        this.letzterWeg = letzterWeg;
    }

    public Ort getZielOrt() { return zielOrt; }
    public void setZielOrt(Ort zielOrt) { this.zielOrt = zielOrt; }

    public float getSpeed() { return speed; }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getWlanStatus() {
        return wlanStatus;
    }

    public void setWlanStatus(String wlanStatus) {
        this.wlanStatus = wlanStatus;
    }

    public int getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(int wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    public String getLastMACAddress() {
        return lastMACAddress;
    }

    public void setLastMACAddress(String lastMACAddress) {
        this.lastMACAddress = lastMACAddress;
    }

    // in ms
    public long getStartStillstand() {
        return startStillstand;
    }

    public void setStartStillstand(long startStillstand) {
        this.startStillstand = startStillstand;
    }

    public boolean isNotStop() {
        return notStop;
    }

    public void setNotStop(boolean notStop) {
        this.notStop = notStop;
    }

    public long getDauerBisNotstop() {
        return dauerBisNotstop;
    }

    public void setDauerBisNotstop(long dauerBisNotstop) {
        this.dauerBisNotstop = dauerBisNotstop;
    }

    public long getDauerBisZiel() {
        return dauerBisZiel;
    }

    public void setDauerBisZiel(long dauerBisZiel) {
        this.dauerBisZiel = dauerBisZiel;
    }

    public List<Ereignis> getJourney() {
        return journey;
    }

    public void setJourney(List<Ereignis> journey) {
        this.journey = journey;
    }

    public long getNextGPSCheck() {
        return nextGPSCheck;
    }

    public void setNextGPSCheck(long nextGPSCheck) {
        this.nextGPSCheck = nextGPSCheck;
    }

    public boolean isGPSTracking() {
        return GPSTracking;
    }

    public void setGPSTracking(boolean GPSTracking) {
        this.GPSTracking = GPSTracking;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public double getDeltaPos() {
        return deltaPos;
    }

    public void setDeltaPos(double deltaPos) {
        this.deltaPos = deltaPos;
    }

    public long getDeltaT() {
        return deltaT;
    }

    public void setDeltaT(long deltaT) {
        this.deltaT = deltaT;
    }

    public float getSpeedCalc() {
        return speedCalc;
    }

    public void setSpeedCalc(float speedCalc) {
        this.speedCalc = speedCalc;
    }

    public String getGPSFrom() {
        return GPSFrom;
    }

    public void setGPSFrom(String GPSFrom) {
        this.GPSFrom = GPSFrom;
    }

    /*public Ereignis sucheEreignis(Weg weg, int anAb) {
        for (Ereignis e : journey) {
            if (e.getOrt() == weg)  {
                if (e.getAnAb() == Aktion.EGAL) {
                    return e;
                } else if (e.getAnAb() == anAb) {
                    return e;
                }
            }
        }
        return null;
    }
    */

    public Ereignis sucheAbfahrtsEreignis(Weg weg) {
        for (Ereignis e : journey) {
            if (e.getWeg() == weg)  {
                if (e.getActionType() == Aktion.HELLO) {
                    return e;
                }
            }
        }
        return null;
    }


    public Ereignis sucheStoryEreignis(Weg weg) {
        for (Ereignis e : journey) {
            if (e.getWeg() == weg)  {
                if (e.getActionType() == Aktion.STORY) {
                    return e;
                }
            }
        }
        return null;
    }

    public Ereignis sucheStoryEreignis(Ort ort) {
        for (Ereignis e : journey) {
            if (e.getOrt() == ort)  {
                if (e.getActionType() == Aktion.STORY) {
                    return e;
                }
            }
        }
        return null;
    }


    public Ereignis sucheAnkunftsEreignis(Weg weg) {
        for (Ereignis e : journey) {
            if (e.getWeg() == weg)  {
                if (e.getActionType() == Aktion.GOODBY || e.getActionType() == Aktion.CLOSING) {
                    return e;
                }
            }
        }
        return null;
    }

    public Ereignis sucheAnkunftsEreignis(Ort ort) {
        for (Ereignis e : journey) {
            if (e.getOrt() == ort)  {
                if (e.getActionType() == Aktion.GOODBY) {
                    return e;
                }
            }
        }
        return null;
    }


    // TODO: direction berücksichtigen
    // Ereignis das an einem Ort stattfindet, der Ort kann ein Ort oder ein Weg sein
    /*
    public Ereignis sucheEreignis(Ort ort, int anAb, Weg weg) {
        for (Ereignis e : journey) {
            if (e.getOrt() == ort) {
                if (e.getAnAb() == Aktion.EGAL) {
                    if (weg != null) {
                        if (weg == e.getWeg()) {
                            return e;
                        }
                    }  else {
                        return e;
                    }
                } else if (e.getAnAb() == anAb) {
                    if (weg != null) {
                        if (weg == e.getWeg()) {
                            return e;
                        }
                    }  else {
                        return e;
                    }
                }
            }
        }
        return null;

    }
    */

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public double getHomeLatitude() {
        return homeLatitude;
    }

    public void setHomeLatitude(double homeLatitude) {
        this.homeLatitude = homeLatitude;
    }

    public double getHomeLongitude() {
        return homeLongitude;
    }

    public void setHomeLongitude(double homeLongitude) {
        this.homeLongitude = homeLongitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getDistHome() {
        return distHome;
    }

    public void setDistHome(double distHome) {
        this.distHome = distHome;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setLetzterOrt(IrgendWo letzterOrt) {
        this.letzterOrt = letzterOrt;
    }

    public int getSatelites() {
        return satelites;
    }

    public void setSatelites(int satelites) {
        this.satelites = satelites;
    }

    public float getTemperatur() {
        return temperatur;
    }

    public void setTemperatur(float temperatur) {
        this.temperatur = temperatur;
    }

    public float getBatterieStatus() {
        return batterieStatus;
    }

    public void setBatterieStatus(float batterieStatus) {
        this.batterieStatus = batterieStatus;
    }

    public String getNowTime() {
        return nowTime;
    }
    public String getNowDateTime() {
        return nowDateTime;
    }

    public void setNowDateTime(String nowDateTime) {
        this.nowDateTime = nowDateTime;
        this.nowTime = nowDateTime.substring(11);
    }

    public long getNow() {
        return now;
    }

    public void setNow(long now) {
        this.now = now;
    }
}
