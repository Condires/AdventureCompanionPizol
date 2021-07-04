package com.condires.adventure.companion.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
/*
  Die Abstaktion von Weg und Ort. wir können auf einem Weg oder an einem Ort sein. Auf Airtable sind das zwei unabhänige Tabellen
  in der Applikation ist ein Weg eigentlich ein Spezialfall eines Ortes. (ist einfach besonders lang)
  Wegen dem lesen und Schreiben auf Airtable müssen es aber zwei unabhänoge klassen sein, sie werden über diese gemeinsame
  Oberklasse zusammengenommen
 */
public class IrgendWo implements Serializable {

    @SerializedName("Name")
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
