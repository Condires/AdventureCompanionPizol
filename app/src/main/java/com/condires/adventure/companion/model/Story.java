package com.condires.adventure.companion.model;

import java.io.Serializable;


/*
  eine Story erzählt eine Geschichte auf einer Anlage
  eine Geschichte wird aus den Aktionen erzeugt

 */
public class Story implements Serializable {
    private String name;
    private int id;
    //private List<Aktion> aktionen;

    public Story(String name, int id) {
        this.name = name;
        this.id = id;
    }

    /*public void setAktionen (List<Aktion> aktionen) {
        this.aktionen = aktionen;
    }
    public List<Aktion> getAktionen() {
        return aktionen;
    }
    */

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String toString() {
        return name;
    }


}
