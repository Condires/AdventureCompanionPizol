package com.condires.adventure.companion.model;

import java.io.Serializable;

public class Radius implements Serializable {
    private String anlage;
    private String ortname;
    private int    direction;
    private String anAb;
    private int Radius;

    public Radius()  {}

    public Radius(String anlage, String ortname, String anAb, int radius) {
        this.anlage = anlage;
        this.ortname = ortname;
        this.anAb = anAb;
        Radius = radius;
    }

    public String getAnlage() {
        return anlage;
    }

    public void setAnlage(String anlage) {
        this.anlage = anlage;
    }

    public String getOrtname() {
        return ortname;
    }

    public void setOrtname(String ortname) {
        this.ortname = ortname;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public String getAnAb() {
        return anAb;
    }

    public void setAnAb(String anAb) {
        this.anAb = anAb;
    }

    public int getRadius() {
        return Radius;
    }

    public void setRadius(int radius) {
        Radius = radius;
    }
}
