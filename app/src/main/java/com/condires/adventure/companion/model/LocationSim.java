package com.condires.adventure.companion.model;

import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.simulator.GPSTrackSim;
import com.condires.adventure.companion.simulator.WangsGPSSim;

import java.util.ArrayList;
import java.util.List;

public class LocationSim {

    private double latitude;
    private double longitude;
    private double altitude = 500;

    private double accuracy = 5;  // Genauigkeit der Position
    private  float  speed = 12;
    private  float  direction;
    private static float  directionForeward;
    private static float  directionBackward;
    private static int    satelites;
    private static String provider;
    private int    wifiLevel;
    private int    delayms;
    private String MACAdress;

    /*
     * es gibt eine statische Liste von Locations in Form von LocationSim Objekten
     */
    static List<LocationSim> locations = null;

    // ersetzt die Liste der Locations durch eine neue Liste
    public void setLocation(List<LocationSim> list) {
        locations = list;
    }
    // ersetzt die Liste der Locations durch eine neue Liste
    public void appendLocation(List<LocationSim> list) {
        if (locations == null) {
            locations = list;
        } else {
            locations.addAll(list);
        }
    }

    // fügt an eine bestehende Liste von Location 2 an, die neue und eine in der Mitte
    static void setLocation(double latitude, double longitude, double altitude) {
       LocationSim l = new LocationSim(latitude, longitude, altitude);
       int i = locations.size();
       if (i > 0) {
           double lat0 = locations.get(i-1).latitude;
           double lon0 = locations.get(i-1).longitude;
           double lat = lat0+(latitude-lat0)/2;
           double lon = lon0+(longitude-lon0)/2;
           locations.add(new LocationSim(lat, lon, altitude));
       }
       locations.add(l);
    }

    // fügt an eine bestehende Liste von Locations eine neue an
    // loc(47.0031571,9.4207938,3.7971058,190.0,0,1125224622);
    public static void loc(double latitude, double longitude, double speed,  double direction, int wifiLevel, int deltams, String MACAdress, double accuracy) {
        LocationSim l = new LocationSim(latitude, longitude, 500);
        l.setDirection((float) direction);
        l.setSpeed((float)speed);
        l.setWifiLevel(wifiLevel);
        l.setDelayms((deltams));
        l.setSpeed(Math.round(speed));
        l.setMACAdress(MACAdress);
        l.setAccuracy(accuracy);

        locations.add(l);
    }
    /*
        Liefert die Location (LocationSim Objekt) an der Position i aus der Liste zurück
     */
    public static LocationSim getLocation(int i) {
        if (i < 0) {
            return locations.get(0);
        }
        int max = locations.size();
        if (i < max) {
            return locations.get(i);
        } else return locations.get(max-1);
    }

    public int size() {
        return locations.size();
    }

    public LocationSim(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = 4;
        this.direction = 15;
    }

    // erzeugt einen Default Simulations-Track mit der Dekan-Oesch-Strasse
    public LocationSim() {
        if (locations == null) {
            locations = new ArrayList<LocationSim>();
            // Dekan-Oesch-Strasse 10: 47.005798, 9.5020390
            LocationSim.setLocation(47.006021, 9.501533, 508.7);

            setLocation(47.006, 9.501456, 508.8);

            setLocation(47.005997, 9.501406, 508.8);
            //
                    setLocation(47.005996, 9.501376, 508.8);
            //
                    setLocation(47.005996, 9.501376, 508.8);
            //
                    setLocation(47.00595, 9.501281, 509.0);
            //
                    setLocation(47.005915, 9.501211, 509.1);
            //
                    setLocation(47.005887, 9.501154, 509.2);
            //
                    setLocation(47.005874, 9.50112, 509.2);
            //
                    setLocation(47.005839, 9.501029, 509.3);
            //
                    setLocation(47.0058, 9.500926, 509.4);
            //
                    setLocation(47.005772, 9.500853, 509.5);
            //
                    setLocation(47.005752, 9.5008, 509.5);
            //
                    setLocation(47.005752, 9.5008, 509.5);
            //
                    setLocation(47.005736, 9.500758, 509.6);
            //
                    setLocation(47.005699, 9.500663, 509.6);
            //
                    setLocation(47.005688, 9.500632, 509.7);
            //
                    setLocation(47.005646, 9.50055, 509.8);
            //
                    setLocation(47.005605, 9.500468, 509.9);
            //
                    setLocation(47.005553, 9.500364, 510.0);
            //
                    setLocation(47.005524, 9.500306, 510.1);
            //
                    setLocation(47.00551, 9.500266, 510.1);
            //
                    setLocation(47.005478, 9.500172, 510.2);
            //
                    setLocation(47.005451, 9.500091, 510.2);
            //
                    setLocation(47.005415, 9.499985, 510.3);
            //
                    setLocation(47.005387, 9.499904, 510.3);
            //
                    setLocation(47.005365, 9.499838, 510.4);
        }
    }
    /*
      erzeugt aus einer Listen von GPS Punkten einen Track
     */
    public List<LocationSim> createWangs() {
        locations = new ArrayList<LocationSim>();
        WangsGPSSim.loadSimUntenMitte();
        //WangsGPSSim.loadSim();
        return locations;
    }
    /* liest einen GPS Track aus der GPSTrackTable

     */
    public List<LocationSim> getTrack(LogService logService, String name, int nr) {
        locations = new ArrayList<LocationSim>();
        GPSTrackSim.loadTrack(logService, name, nr);
        return locations;
    }

    /*
        Generiert aus einem Weg eine Location List, die Punkte werden aus der gewünschten Geschwindigkeit so
        berechnet, dass ein Punkt pro Sekunde Weg erzeugt wird.
        TODO: stimmt so nicht, es werden zuwenig Punkte erzeugt
     */
    public List<LocationSim> createTrack(Weg weg, float mySpeed) {
        int dist = (int)  weg.getVonOrt().calculateDistance(weg.getNachOrt());  // ca 100m
        float time = dist/mySpeed;    // zb 100m   10m/s  = 10 sekunden  = 10 Zyklen a 1 Sekunde
        double lat0 = weg.getVonOrt().getLatitude();
        double lon0 = weg.getVonOrt().getLongitude();
        double dLat = (weg.getNachOrt().getLatitude()-lat0)/time;
        double dLon = (weg.getNachOrt().getLongitude()-lon0)/time;

        List track = new ArrayList<LocationSim>();
        LocationSim loc = new LocationSim(lat0, lon0, 500);
        loc.setSpeed(mySpeed);
        speed = mySpeed;
        // TODO rückwärtsrichtung aus vorwärtsrichtung berechnen
        loc.directionForeward = weg.getDirection();
        loc.directionBackward = weg.getDirection()-175;
        for (int i = 0; i < time; i++) {
            LocationSim l = new LocationSim(lat0, lon0, 500);
            track.add(l);
            lat0 = lat0 + dLat;
            lon0 = lon0 + dLon;
        }
        track.add(new LocationSim(weg.getNachOrt().getLatitude(),weg.getNachOrt().getLongitude(), 500));
        return track;
    }

    public  void setDirectionToForeward() { this.direction = directionForeward;}
    public  void setDirectionToBackward() { this.direction = directionBackward;}

    public static float getDirectionForeward() {return directionForeward;}
    public static float getDirectionBackward() {return directionBackward;}
    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
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

    public int getSatelites() {
        return satelites;
    }

    public void setSatelites(int satelites) {
        this.satelites = satelites;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public  int getWifiLevel() {
        return wifiLevel;
    }

    public void setWifiLevel(int wifiLevel) {
        this.wifiLevel = wifiLevel;
    }

    public int getDelayms() {
        return this.delayms;
    }

    public  void setDelayms(int delayms) {
        this.delayms = delayms;
    }

    public String getMACAdress() {
        return MACAdress;
    }

    public void setMACAdress(String MACAdress) {
        this.MACAdress = MACAdress;
    }
}
