package com.condires.adventure.companion.model;

import android.util.Log;

import com.condires.adventure.companion.airtable.AirtableObject;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;


public class Ort extends IrgendWo implements AirtableObject {
    public static String tableName = "Ort";
    @Expose(serialize = false)
    private static String TAG = "Ort";

    public String id;         // eine eindeutig id über alle Objekte hinweg


    private Date createdTime;
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;
    //private float speed;
    //private double distance;
    @SerializedName("Altitude")
    private double altitude;
    //private float direction;
    @SerializedName("Adresse")
    private String address;
    @SerializedName("MAC")
    private String MACAddress;    // die MAC Adresse des Routers
    // TODO: später auf 10 reduzieren
    @SerializedName("Radius")
    private long radius = 10;     // 10 ist Default und wird pro Ort überschrieben

    @SerializedName("Anlage")
    public String[] anlageIds;


    @SerializedName("Background Id")
    public int backgroundMediaId;
    @SerializedName("Background Type")
    public int backgroundMediaType; // 0 Sound, 1 Video,


    public Ort() {
        this.name = "noname";
    }

    public Ort(String name) {
        this.name = name;
    }

    public Ort(Double latitude, Double longitude) {
        this.name = "noname";
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Ort(Ort ort) {
        this(ort.name, ort.latitude, ort.longitude, ort.altitude, ort.address);
    }

    public Ort(String name, double latitude, double longitude, double altitude, String address) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        //this.speed = speed;
        //this.distance = distance;
        this.altitude = altitude;
        //this.direction = direction;
        this.address = address;
    }

    public String toString() {
        return "Name:"+name +
                "\nRadius bei Abfahrt:"+this.radius;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }




    public String getTableName() { return tableName;}

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
    /*
    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
    */
    public long getRadius() {
        return radius;
    }

    public void setRadius(long radius) {
        this.radius = radius;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public String getMACAddress() {
        return MACAddress;
    }

    public void setMACAddress(String MACAddress) {
        this.MACAddress = MACAddress;
    }

    /*public float getDirection() {
        return direction;
    }

    public void setDirection(float direction) {
        this.direction = direction;
    }
    */

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public void setAnlageId(String anlageId) {
        if (anlageIds == null) {
            anlageIds = new String[1];
        }
        this.anlageIds[0] =  anlageId;
    }

    public void setAnlageIds(String[] anlageIds) {
        this.anlageIds = anlageIds;
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

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    //TODO: das teuer calculateDistance nur aufrufeb, wenn wir in der nähe von etwas sind.
    public long calculateDistance(double lat2, double lon2) {

        double lat1 = this.latitude;
        double lon1 = this.longitude;
        final int R = 6371; // Radius of the earth
        double el1 = 0.0;
        double el2 = 0.0;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);
        long dist = (long) Math.sqrt(distance);
        return dist;
    }

    public long calculateDistance(Ort nachOrt) {
        if (nachOrt != null) {
            return calculateDistance(nachOrt.getLatitude(), nachOrt.getLongitude());
        }
        return 0;
    }




    public boolean isOnLine(Ort a, Ort b) {
        // TODO: tolleranz beachten
        double m1 = (latitude - a.latitude)/(longitude - a.longitude);
        double m2 = (latitude - b.latitude)/(longitude - b.longitude);
        return m1 == m2;
    }

    public float calculateDirection(Ort nachOrt) {
        double x1 = this.getLatitude();
        double y1 = this.getLongitude();
        double latitude = nachOrt.getLatitude();
        double longitude = nachOrt.getLongitude();

        double angle = Math.toDegrees(Math.atan2(latitude - x1,
                longitude - y1));
        //Keep angle between 0 and 360
        angle = angle + Math.ceil(-angle / 360) * 360;
        Log.d(TAG, "calculateDirection: " + angle);
        //return (float) angle;

        double lng1 = this.getLongitude();
        double lng2 = nachOrt.getLongitude();
        double lat1 = this.getLatitude();
        double lat2 = nachOrt.getLatitude();
        double dLon = (lng2 - lng1);
        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon);
        double brng = Math.toDegrees((Math.atan2(y, x)));
        brng = (360 - ((brng + 360) % 360));

        return (float) brng;
    }

    /*
         bin ich an einem Ort
         oder auf einem Weg
         ich weiss wo ich bin, auf einem Weg oder Ort
        */
    public Ereignis getAktion(List<Ereignis> journey) {
        // den nächsten Ort suchen
        // bei einer Bahn reicht Distanz und Richtung als Filter
        for (Ereignis ereignis : journey) {
            if (this instanceof Ort) {
                if (ereignis.getOrt() == this)
                    // die aktion findet an unserem Ort statt
                    // dh wir sind nicht auf dem Weg zwischen zwei Orten
                    return ereignis;
            } else {
                if (ereignis.getOrt() == this) {
                    // ort ist ein Weg
                    return ereignis;
                }
            }
        }
        return null;
    }
}
