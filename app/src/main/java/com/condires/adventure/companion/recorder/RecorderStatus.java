package com.condires.adventure.companion.recorder;

import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Weg;

public class RecorderStatus {



    private Anlage anlage;      // die Anlage, die im recoder erzeut oder bearbeitet wird
    private int status;         // 0 alles leer, 1 alles ist  nur im recorderstatus, 2: alles ist gespeichert

    private double latitude;
    private double longitude;
    private double accuracy;  // Genauigkeit der Position
    private float  speed;
    private double distance;
    private double altitude;
    private float  direction;

    private String ortName;

    private Ort homeOrt;
    private Ort vonOrt;
    private Ort nachOrt;
    private Ort currentOrt;
    private Weg currentWeg;



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

    public Anlage getAnlage() {
        return anlage;
    }

    public void setAnlage(Anlage anlage) {
        this.anlage = anlage;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOrtName() {
        return ortName;
    }

    public void setOrtName(String ortName) {
        this.ortName = ortName;
    }

    public Ort getHomeOrt() {
        return homeOrt;
    }

    public void setHomeOrt(Ort homeOrt) {
        this.homeOrt = homeOrt;
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

    public Ort getCurrentOrt() {
        return currentOrt;
    }

    public void setCurrentOrt(Ort currentOrt) {
        this.currentOrt = currentOrt;
    }

    public Weg getCurrentWeg() {
        return currentWeg;
    }

    public void setCurrentWeg(Weg currentWeg) {
        this.currentWeg = currentWeg;
    }
}
