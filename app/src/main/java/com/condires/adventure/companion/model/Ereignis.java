package com.condires.adventure.companion.model;


public class Ereignis extends Aktion {
    boolean done = false;
    long ende;  // Zeitpunkt in milisec wann das Ereignis fertig sein sollte
    long start;  // Startzeitpunkt in ms

    public Ereignis(Aktion a) {
        super(a);
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getEnde() {
        return ende;
    }

    public void setEnde(long ende) {
        this.ende = ende;
    }
}
