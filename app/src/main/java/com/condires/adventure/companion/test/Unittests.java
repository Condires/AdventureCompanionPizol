package com.condires.adventure.companion.test;

import com.condires.adventure.companion.Data;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.PlayerStatus;

import java.util.List;


public class Unittests {


    private Data data = Data.getInstance();

    public void test1() {
        Anlage anlage = data.getAnlagen().get(2);
        List<Ort> orte = anlage.getOrte();

        PlayerStatus ps = new PlayerStatus();
        for (Ort ort : orte) {
            ps.sucheAnkunftsEreignis(ort);
        }

    }

    public void test2() {
        PlayerStatus ps = new PlayerStatus();
        Anlage anlage = data.getAnlagen().get(2);
        List<Ort> orte = anlage.getOrte();

        for (Ort ort : orte) {
            ps.setLatitude(ort.getLatitude());
            ps.setLongitude(ort.getLongitude());
            ps.setSpeed(2);
            ps.setAltitude(555);
            ps.setDirection(240);   // in Richtung Begegnuszentrum

            //eventLoop(ps);
        }

    }
}
