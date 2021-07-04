package com.condires.adventure.companion.model;

import com.condires.adventure.companion.setting.ACSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
Eine Anlage hält alles zusammen was benötigt wird um an einer Kundenlocation einen Erlebnisweg zu haben
Der Erlebnisweg kann aus mehreren Teilwegen bestehen, die alle miteinander verbunden sind.

 */
public class Anlage extends AnlageObj implements Serializable {


    //@SerializedName("Name")
    //public String name;    // Der Name der angezeigt wird

    //@SerializedName("id")
    //public String id;         // eine eindeutig id über alle Anlagen hinweg
    //@SerializedName("StartOrt")
    //public String startOrtIds;
    //@SerializedName("MaxStillstand")
    //public float maxStillstand = 99999999;   // in Minuten, ab wann soll alles still sein, wenn wir auf einem Weg stillstehen?

    //private String dbId;   // die ID der Anlage in der lokalen DB

    //TODO: durch Radius pro Ort ersetzen.
    private static double ORT_GROESSE = 10; // innerhalb von 10 Metern ist man am Ort, sonst auf dem Weg dazu
    // Home ist der Startort, der via Startort ID ermittelt wird.
    private transient Ort home;       // Ort wo der Ausgangspunkt des Trails ist.

    private List<Ort> orte = new ArrayList<Ort>();
    private List<Weg> wege = new ArrayList<Weg>();
    private List<Aktion> aktionen = new ArrayList<Aktion>();
    private transient List<Ereignis> journey = new ArrayList<Ereignis>();
    // Aktion mit StoryId speichern StoryId ist AnlageId+StoryId
    private transient Map storyMap = new HashMap();

    // Spinner Drop down elements
    private List<Story> stories = new ArrayList<Story>();
    private ACSettings mACSettings;  // die ACSettings der Anlage aus den Pref oder von Airtable

    private transient Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Anlage() {
        super();
    }

    /*
    public Anlage(String name, String id, String startOrtIds, float maxStillstand) {
        this.name = name;
        this.id = id;
        this.startOrtIds = startOrtIds;
        this.maxStillstand = maxStillstand;
    }
    */

    public Anlage(Anlage anlage) {
        this.name = anlage.name;
        this.id = anlage.id;
        this.startOrtIds = anlage.startOrtIds;
        this.maxStillstand = anlage.maxStillstand;
        this.dbId = anlage.dbId;
        this.version = anlage.version;
    }


    public Anlage(AnlageObj ao) {
        this.name = ao.name;
        this.id = ao.id;
        this.startOrtIds = ao.startOrtIds;
        this.maxStillstand = ao.maxStillstand;
        this.dbId = ao.dbId;
        this.version = ao.version;
    }

    public Anlage(String name, String dbId) {
        this.name = name;
        this.dbId = dbId;
    }

    public String toString() {
        return name;
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
        this.id =  id;
    }


    public float getMaxStillstand() {
        return maxStillstand;
    }

    public void setMaxStillstand(float maxStillstand) {
        this.maxStillstand = maxStillstand;
    }

    public String getAsJson() {

        //String json = gson.toJsonTree(anlage);
        String json = gson.toJson(this);
        return json;
    }

    public void setFromJson(String json) {

    }

    public List<Ort> getOrte() {
        return orte;
    }

    public void setOrte(List<Ort> orte) {
        this.orte = orte;
    }

    public void mapActionToStory(String anlageId, int StoryId, List<Aktion> aktionen) {
        storyMap.put(anlageId+"-"+StoryId, aktionen);
    }

    public List<Aktion> getActionen(int anlageId, int StoryId) {
        return (List<Aktion>) storyMap.get(anlageId+"-"+StoryId);
    }

    public List<Weg> getWege() {
        return wege;
    }

    public void setWege(List<Weg> wege) {
        this.wege = wege;
    }

    public List<Aktion> getAktionen() {
        return aktionen;
    }

    public void setAktionen(List<Aktion> aktionen) {
        this.aktionen = aktionen;
    }

    public List<Ereignis> getJourney() {
        return journey;
    }

    public void setJourney(List<Ereignis> journey) {
        this.journey = journey;
    }

    public Ort getHome() {
        return home;
    }

    public void setHome(Ort home) {
        this.home = home;
    }

    public Anlage deepCopy(Anlage anlage) {
        Anlage anl = new Anlage();
        anl.setName("new"+anlage.getName());
        for (Ort ort : anlage.orte) {
            Ort newOrt = new Ort();
            newOrt.setRadius(ort.getRadius());
            newOrt.setMACAddress(ort.getMACAddress());
            newOrt.setBackgroundMediaId(ort.getBackgroundMediaId());
            newOrt.setLongitude(ort.getLongitude());
            newOrt.setLatitude(ort.getLatitude());
            newOrt.setBackgroundMediaType(ort.getBackgroundMediaType());
            newOrt.setAddress(ort.getAddress());
            newOrt.setAltitude(ort.getAltitude());
            newOrt.setName("new"+ort.getName());
            anl.orte.add(newOrt);
        }
        for (Weg weg : anlage.wege) {
            Weg newWeg = new Weg();
            newWeg.setName("new"+weg.getName());
            newWeg.setRadiusNachOrt(weg.getRadiusNachOrt());
            newWeg.setDirection(weg.getDirection());
            newWeg.setBackgroundMediaId(weg.getBackgroundMediaId());
            newWeg.setBackgroundMediaType(weg.getBackgroundMediaType());
            Ort vonOrt = anl.getOrtByName("new"+weg.getVonOrt().getName());
            newWeg.setVonOrt(vonOrt);
            Ort nachOrt = anl.getOrtByName("new"+weg.getNachOrt().getName());
            newWeg.setNachOrt(nachOrt);
            anl.wege.add(newWeg);
        }
        for (Aktion aktion : anlage.aktionen) {
            Aktion newAktion = new Aktion();
            newAktion.setActionType(aktion.getActionType());
            newAktion.setMediaId(aktion.getMediaId());
            newAktion.setMediaType(aktion.getMediaType());
            newAktion.setName(aktion.getName());
            newAktion.setReihenfolge(aktion.getReihenfolge());
            newAktion.setRepeat(aktion.isRepeat());
            newAktion.setStartDelayMeters(aktion.getStartDelayMeters());
            Ort ort = aktion.getOrt();
            if (ort != null) {
                newAktion.setOrt(anl.getOrtByName("new"+ort.getName()));
            }
            Weg weg = aktion.getWeg();
            if (weg != null) {
                newAktion.setWeg(anl.getWegByName("new"+weg.getName()));
            }
            newAktion.setAnlage(anl);
            Ort nachOrt = aktion.getNachOrt();
            if (nachOrt != null) {
                newAktion.setNachOrt(anl.getOrtByName("new"+nachOrt.getName()));
            }
            anl.aktionen.add(newAktion);
        }
        Ort startOrt = anlage.getHome();
        if (startOrt != null) {
            anl.setHome(anl.getOrtByName("new"+startOrt.getName()));
        }
        return anl;
    }

    public  void rebuildReferences() {
        Anlage anlage = this;
        if (anlage.getStartOrtIds() != null) {
            anlage.setHome(anlage.getOrtById(anlage.getStartOrtId()));
        }
        if (anlage.getStartOrtIds() != null) {
            anlage.setHome(anlage.getOrtById(anlage.getStartOrtId()));
        }

        for (Weg weg : anlage.getWege()) {
            weg.setVonOrt(anlage.getOrtById(weg.getVonOrtId()));
            weg.setNachOrt(anlage.getOrtById(weg.getNachOrtId()));
            weg.setNextWeg(anlage.getWegById(weg.getNextWegId()));
        }
        for (Aktion aktion : anlage.getAktionen()) {
            aktion.setWeg(anlage.getWegById(aktion.getWegId()));
            aktion.setOrt(anlage.getOrtById(aktion.getOrtId()));
            aktion.setNachOrt(anlage.getOrtById(aktion.getNachOrtId()));
            aktion.setFolgeAktion(anlage.getAktionById(aktion.getFolgeAktionId()));
        }
    }

    public String getStartOrtId() {
        if (startOrtIds != null) {
            return startOrtIds[0];
        }
        return null;
    }

    public List<Story> getStories() {
        return stories;
    }

    // TODO: in Zukunft sollen mehrere Background-Stücke gespielt werden können
    // TODO die Background Musik des Startortes holen, falles es ihn gibt
    public Aktion getBackgroundMusic() {
        Aktion act = null;
        Weg startWeg = findWegStartingAt(getHome());
        for (Aktion aktion : aktionen) {
            if (aktion.getActionType() == Aktion.BACKGROUND) {
                // wenn wir eine Background Musik für den ersten Weg finden, spielen wir die
                if (aktion.getWeg() == startWeg) {
                    return aktion;
                } else {
                    // sonst einfach eine
                    act = aktion;
                }
            }
        }
        return act;
    }

    public Weg findWegStartingAt(Ort ort) {
        for (Weg w : wege) {
            if (w.getVonOrt() == ort) {
                return w;
            }
        }
        return null;
    }

    public boolean existsOrt(Ort ort) {
        return orte.indexOf(ort) > -1;
    }

    public Ort getOrtById(String id) {
        if (id == null) return null;
        for (Ort ort : orte) {
            if (id.equals(ort.getId())) {
                return ort;
            }
        }
        return null;
    }

    // Neu kann es eine Liste von MAC Adressen pro Ort sein
    public Ort getOrtByMAC(String MACAddress) {
        if (MACAddress == null) return null;
        List os = orte;
        for (Ort o : orte)  {
            Ort mOrt = o;
            //TODO: könnten auch mehrere MAC Adressen sein
            String mac = mOrt.getMACAddress();
            if (mac!=null && mac.indexOf(MACAddress)>=0) {
                return mOrt;
            }
        }
        return null;
    }

    public Ort getOrtByName(String ortName) {
        if (ortName == null) return null;
        List os = orte;
        for (Ort o : orte)  {
            Ort mOrt = o;
            String mName = mOrt.getName();
            if (ortName.equals(mName)) {
                return mOrt;
            }
        }
        return null;
    }
    public Weg getWegByName(String wegName) {
        if (wegName == null) return null;
        for (Weg weg : wege)  {
            if (wegName.equals(weg.getName())) {
                return weg;
            }
        }
        return null;
    }
    public Weg getWegById(String id) {
        if (id == null) return null;
        for (Weg weg : wege) {
            if (id.equals(weg.getId())) {
                return weg;
            }
        }
        return null;
    }

    public Aktion getAktionById(String id) {
        if (id == null) return null;
        for (Aktion aktion : aktionen) {
            if (id.equals(aktion.getId())) {
                return aktion;
            }
        }
        return null;
    }



    public Weg findWegStartingAt(Ort ort, float direction) {
        if (direction == 0.0) return null;
        for (Weg w : wege) {
            Ort vonOrt = w.getVonOrt();
            if (vonOrt == ort) {
                if (isIn360(w.getDirection(), direction, 35)) {
                    return w;
                }
            }
        }
        return null;
    }

    public Weg findWegEndingAt(Ort ort, float direction) {
        if (direction == 0.0) return null;
        for (Weg w : wege) {
            Ort nachOrt = w.getNachOrt();
            if (nachOrt == ort) {
                if (isIn360(w.getDirection(), direction, 35)) {
                    return w;
                }
            }
        }
        return null;
    }

    public Weg findWegEndingAt(Ort ort) {
        for (Weg w : wege) {
            if (w.getNachOrt() == ort) {
                return w;
            }
        }
        return null;
    }

    public void setStories(List<Story> stories) {
        this.stories = stories;
    }
    public Story getStoryById(int id) {
        for (Story story : stories) {
            if (story.getId() == id) {
                return story;
            }
        }
        return null;
    }

    // erzeugt eine neue runde auf dem Pfad
    public List<Ereignis> createNewJourney() {
        //& TODO: darf ich alle aktionen nehmen? eher nicht, nur die der geladenen Story
        List<Aktion> aktionen = getAktionen();
        List<Ereignis> journey = new ArrayList<Ereignis>();
        for (Aktion aktion : aktionen) {
            journey.add(new Ereignis(aktion));
        }
        return journey;

    }

    // liefert einen Ort oder einen Weg auf dem ich bin
    public Ort InitalPositionFinden(double latitude, double longitude, float direction) {
        Ort nearestLocation = null;
        double currentDistance = 100000;  // sehr weit weg
        for (Ort ort : orte) {
            double d = ort.calculateDistance(latitude, longitude);
            if (d < currentDistance) {
                currentDistance = d;
                nearestLocation = ort;
            }
        }
        /*  // wir geben imme rnur den Ort zurück
        if (currentDistance > ORT_GROESSE) {
            // ich bin auf einem Weg
            for (Weg weg : wege) {
                if (weg.isOnWeg(latitude,longitude)) {
                    // stimmt die Richtung?
                    if (isIn(weg.getDirection(), direction, 20)) {
                        nearestLocation = weg;
                    }
                }
            }
        }
        */
        return nearestLocation;

    }
    public static boolean isIn(float wert1, float wert2, float toleranz) {
        float diff = wert1 - wert2;
        if (diff < 0) diff = diff * -1;
        return   diff < toleranz;
    }

    public static boolean isIn360(float wert1, float wert2, float toleranz) {
        float up = wert1 - toleranz;
        if (up < 0) {
            wert1 = wert1 - up;
            wert2 = wert2 - up;
            if (wert2 > 360) {
                wert2 = wert2 -360;
            }
        }
        float diff = wert1 - wert2;
        if (diff < 0) diff = diff * -1;
        return   diff < toleranz;
    }

}
