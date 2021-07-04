package com.condires.adventure.companion.arrival;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
/*
 * wird im Moment nicht benutzt, wurde mit der Klasse @AirLogArrival umgesetzt
 */
public class AirPlayerPosition implements Serializable {
    @SerializedName("Name")
    public String name;    // Der Name der angezeigt wird

    @SerializedName("id")
    public String id;         // eine eindeutige id über alle Anlagen hinweg

    @SerializedName("Device")
    public String device;

    @SerializedName("Accuracy")
    private double accuracy;  // Genauigkeit der Position
    @SerializedName("Latitude")
    private double latitude;
    @SerializedName("Longitude")
    private double longitude;
    @SerializedName("Speed")
    private float  speed;
    @SerializedName("Distance Home")
    private double distance;

    private double altitude;
    @SerializedName("Direction")
    private float  direction;

    public AirPlayerPosition() {};

    public AirPlayerPosition(String name, String device, double accuracy, double latitude, double longitude, float speed, double distance, double altitude, float direction) {
        this.name = name;
        this.device = device;
        this.accuracy = accuracy;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = speed;
        this.distance = distance;
        this.altitude = altitude;
        this.direction = direction;
    }

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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
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
}
