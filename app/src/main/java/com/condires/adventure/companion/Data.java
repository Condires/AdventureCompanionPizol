package com.condires.adventure.companion;

import android.content.Context;
import android.util.Log;

import com.condires.adventure.companion.airtable.AirtableService;
import com.condires.adventure.companion.logger.LogService;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.MediaObject;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Weg;
import com.condires.adventure.companion.setting.ACSettings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
   Datenmodell: Kunde(Pizolbahn), Anlagen (zB Wangs Bergbahn), Trails(Orte, Wege), Stories,
 */

public class Data {
    //TODO:  Datenbank aufbauen

    private List<Anlage> anlagen = new ArrayList();
    private String TAG = MainActivity.class.getSimpleName();
    // TODO: kein festes Attribute
    //public Anlage mAnlage;
    private static Data instance;
    AirtableService mAirtableService;
    DatabaseHelper  mDbHelper;
    LogService      mLogService;
    List<MediaObject> mMediaList;
    Map<String, String> mMediaMap;

    /*
     * soll die Daten zwischen Airtabel und lokale DB synchronisieren
     */
    private void anlageSync(Anlage anlage) {


    }

    public static Data getInstance () {
        return instance;
    }
    // diese Daten hängen von der gewählten Geschichte und dem Einsatzort ab
    /*
     * Im Normalfall wird die gewünschte Anlage von der lokalen Datenbank geladen
     */
    public Data(Context context) {
        if (instance != null) {
            Log.d(TAG, "new Data aufgerufen, obwohl instance schon da war");
            return;
        }

        mDbHelper = new DatabaseHelper(context);
        mLogService = LogService.getInstance(MainActivity.getInstance());

        // die Liste aller MediaId und deren Namen
        //createMediaList();

        //ACSettings settings = new ACSettings();
        //settings.setVolume(7);
        //int volume = settings.getVolume();
        //settings.setBatteryAlarmLevel(90);

        //settings = (ACSettings) airtableServiceOn().loadSettingFromAirtable("Pizolbahn Ragaz");
        //volume = settings.getVolume();
//
        // alle Anlagen von der Datenbank lesen
        anlagen = mDbHelper.getAllAnlageNamen();
        Anlage anl = null;
        if (anlagen == null || anlagen.size() == 0) {
            loadAnlageFromAirtable("Pizolbahn Wangs");
            saveAnlageToDb("Pizolbahn Wangs");
            // Achtung, diese Operation löscht alle geladenen Anlagen wieder aus dem Speicher
            anlagen = mDbHelper.getAllAnlageNamen();
        } else {
            anl = mDbHelper.getAnlage(anlagen.get(0).getDbId());
        }



        boolean neueStories = false;
        if (neueStories) {
            Anlage anlage = getAnlageByName("Pizolbahn Ragaz");
            // anlage durch Code überschreiben
            //loadAnlagePizol(anlage);
            airtableServiceOn().writeAnlageToAirtable(anlage);
            saveAnlageToDb(anlage.getName());
        }
        // wenn keine da sind, die Anlagen aus dem code laden
        if (anlagen == null || anlagen.size() == 0) {

            // sicherstellen, das auf der DB dasselbe ist wie im Code
            //loadAnlageBadRagaz();
            //loadAnlagePizolWangs();
            //loadAnlagePizol();
            //loadAnlageDekanOesch();
        }

        // es müssen nur die Anlagenamen und dbIds geladen sein, damit die Drop Down Box funktioniert
        // wenn keine da sind Anlage von Airtable laden
        if (anlagen == null) {
            Anlage b = airtableServiceOn().loadAnlageFromAirtable("Pizolbahn Ragaz");
            airtableServiceOn().writeAnlageToAirtable(b);
            Log.d(TAG, "von airtable gelesen und wieder geschrieben");
        }

        /*
        // alle Anlagen in mDbHelper einfügen oder updaten
        for (Anlage a: anlagen) {
            if (mDbHelper.existsAnlage(a.getDbId())) {
                mDbHelper.updateAnlage(a);
            } else {
                mDbHelper.addAnlage(a);
            }
        }
        */

        // wenn auf dem Geräte keine Anlagen in der DB sind, ist anl leer
        if (anl != null) {
            anl = anlageLaden(anl);
            String dao = createDAO(anl);
        }
        boolean reloadAll = false;
        if (reloadAll) {
            for (int i=0;i<anlagen.size(); i++) {
                Anlage a = anlagen.get(i);
                deleteAnlageFromDb(a.getName());
            }
            for (int i=0;i<anlagen.size(); i++) {
                Anlage a = anlagen.get(i);
                Anlage b = airtableServiceOn().loadAnlageFromAirtable(a.getName());
                airtableServiceOn().writeAnlageToAirtable(b);
                anlagen.set(i,b);
            }

        }

        boolean writeToAirtable = false;
        // alle Anlagen auf Airtable updaten oder einfügen
        if (writeToAirtable) {
            for (Anlage a : anlagen) {
                airtableServiceOn().writeAnlageToAirtable(a);
            }
        }

        /*
        int count = mDbHelper.getAnlageCount();
        if(count ==0 ) {
            mDbHelper.addAnlage(badragaz);
            mDbHelper.addAnlage(pizolWangs);
            mDbHelper.addAnlage(pizolRagaz);
            mDbHelper.addAnlage(dekanOesch);
        }
        */
        instance = this;

        // wenn neue Medienfiles geladen wurden, laufen lassen, damit die Medien Tabelle auf Airtable aktuell bleibt
        // der Update wird aufgrund des Namens gemacht,besser wäre aber nach MediaId, weil diese auch bei einem refactzoring
        // stabil bleibt.
        createMediaList();
        boolean newMedia = false;
        if (newMedia) {
            writeMediaOjectsToAirtable();
        }

        /*
        Anlage anlage = getAnlage(0);
        Ort ortGemMac = anlage.getOrtByMAC("MAC:32:23:f8:1e:93:d6");
        */


    }

    private void writeMediaOjectsToAirtable() {
        //createMediaList();
        if (mMediaList != null) {
            for (MediaObject mo : mMediaList) {
                airtableServiceOn().insertUpdateAirtableObject(mo, "{Media Id} = '" + mo.getMediaId() + "'");
            }
        }
    }


    private List<MediaObject> createMediaList() {
        //newMO("musik_talstation", R.raw.musik_talstation);
        //newMO("fatima_dunn_05_nebelwald.mp3", R.raw.fatima_dunn_05_nebelwald);
        newMO("goodbyoben.mp3", R.raw.goodbyoben);
        newMO("goodbyunten.mp3", R.raw.goodbyunten);
        newMO("hellooben.mp3", R.raw.hellooben);
        newMO("horizontal.mp3", R.raw.horizontal);
        //newMO("jazz_in_paris.mp3", R.raw.jazz_in_paris);
        newMO("musik_bergstation.mp3", R.raw.musik_bergstation);
        newMO("musik_mittelstation.mp3", R.raw.musik_mittelstation);
        //newMO("musik_mittelstation2.mp3", R.raw.musik6);
        newMO("musik_talstation.mp3", R.raw.musik_talstation);
        //newMO("nuschelpeter.mp3", R.raw.nuschelpeter);
        newMO("rauf.mp3", R.raw.rauf);
        newMO("rueckwaerts.mp3", R.raw.rueckwaerts);
        newMO("runter.mp3", R.raw.runter);
        //newMO("sample.mp3", R.raw.sample);
        newMO("still.mp3", R.raw.still);
        //newMO("stimme_beispiel.m4a", R.raw.stimme_beispiel);
        newMO("story1.mp3", R.raw.story1);
        newMO("story2.mp3", R.raw.story2);
        newMO("story3.mp3", R.raw.story3);
        newMO("story4.mp3", R.raw.story4);
        newMO("story5.mp3", R.raw.story5);
        newMO("story6.mp3", R.raw.story6);
        newMO("story7.mp3", R.raw.story7);
        newMO("story8.mp3", R.raw.story8);
        newMO("musik2.mp3", R.raw.musik2);
        newMO("musik3.mp3", R.raw.musik3);
        newMO("musik6.mp3", R.raw.musik6);
        //newMO("t02_zuviel_action_teil_01.mp3", R.raw.t02_zuviel_action_teil_01);
        newMO("vorwaerts.mp3", R.raw.vorwaerts);
        newMO("on_my_way_with_lyrics.mp3", R.raw.on_my_way_with_lyrics);

        return mMediaList;
    }


    private void newMO(String name, int mediaId) {

        MediaObject mo = new MediaObject(name, mediaId);
        if (mMediaList == null) mMediaList = new ArrayList<MediaObject>();
        if (mMediaMap == null) mMediaMap = new HashMap<String, String>();
        mMediaList.add(mo);
        mMediaMap.put(mediaId+"", name);
    }

    public String getMediaName(int mediaId) {
        String name = mMediaMap.get(mediaId+"");
        return name;
    }


    public static List<Integer> listRawMediaFiles() {
        List<Integer> ids = new ArrayList<>();
        for (Field field : R.raw.class.getFields()) {
            try {
                ids.add(field.getInt(field));
            } catch (Exception e) {
                //compiled app contains files like '$change' or 'serialVersionUID'
                //which are no real media files
            }
        }
        return ids;
    }

    private AirtableService airtableServiceOn() {
        if (mAirtableService == null) {
            mAirtableService = AirtableService.getInstance();
        }
        return mAirtableService;
    }

    public void loadSettingFromAirtable(String anlageName) {
        Anlage anlage = this.getAnlageByName(anlageName);
        if (anlage != null) {
            ACSettings setting = airtableServiceOn().loadSettingFromAirtable(anlageName);
            if (setting == null) {
                mLogService.writeLog(TAG, 3,"Die ACSettings für: "+anlageName+" konnten nicht geladen werden");
            }
        }
    }

    /*
     * holt die Anlage mit dem Index und stellt sicher, dass die Daten der Anlage geladen sind
     */
    public Anlage getAnlage(int index) {
        Anlage anlage = anlagen.get(index);
        Anlage geladen = anlageLaden(anlage);
        // falls beim Laden ein neues Objekt erzeugt wurde, muss es in der anlage Liste ersetzt werden
        if (geladen != null && anlage != geladen) {
            anlagen.set(index, geladen);
        }
        return geladen;
    }

    /*
     * die Anlage mit all ihren Daten wird in den Speicher geladen, das
     * Parameter Anlage Objekt muss nur den Namen und die dbId enthalten
     */
    public Anlage anlageLaden(Anlage anlage) {
        // mAnlage ist nicht geladen
        String dbId = anlage.getDbId();
        String name = anlage.getName();
        if (anlage.getOrte() == null || anlage.getOrte().size() == 0) {
            // ab db laden
            anlage = mDbHelper.getAnlage(dbId);
            if (anlage == null) {
                // wenn es nicht auf der db ist ab Airtable laden
                anlage = airtableServiceOn().loadAnlageFromAirtable(name);
                if (anlage == null) {
                    // wenn es nicht auf Airtable ist, ab Code laden
                    mLogService.writeLog(TAG, 3,"die Anlage "+anlage+" existiert nicht");
                } else {
                    // in der lokalenn speichern
                    mDbHelper.addAnlage(anlage);
                }
            }
        }
        // mAnlage ist schon geladen
        return anlage;
    }

    public void writeLog(int type, String msg) {
        LogService ls = LogService.getExistingInstance();
        if (ls != null) {
            ls.writeLog(TAG, type, msg);
        }
    }


    public void loadAnlageFromAirtable(String name) {

        Anlage anl = airtableServiceOn().loadAnlageFromAirtable(name);
        if (anl != null) {
            // über den Namen gehen, weil beim getIndex 0 kommt wenn die Anlage nicht existiert
            Anlage anlage = getAnlageByName(name);
            writeLog(4, "Anlage konnte geladen werden");
            if (anlage != null) {
                int i = getAnlageIndex(name);
                // die anlage im Speicher ersetzen
                anlagen.set(i, anl);
                writeLog(4, "Anlage ersetzt");
            } else {
                // TODO: es könnte auch der Name geändert worden sein, prüfen mit dbid und id
                // wenn sie nicht existiert, wird sie als neue angelegt
                // der View Adapter merkt es nicht
                anlagen.add(anl);
                writeLog(4, "Anlage hinzugefügt");
            }
        } else {
            writeLog(4, "Anlage konnte nicht geladen werden");
        }
    }
    public void saveAnlageToAirtable(String name) {
        Anlage anl = getAnlageByName(name);
        airtableServiceOn().writeAnlageToAirtable(anl);
    }

    public void saveAnlageAsToAirtable(String name, String newName) {
        Anlage anl = airtableServiceOn().loadAnlageFromAirtable(name);
        Anlage anlCopy = anl.deepCopy(anl);
        airtableServiceOn().writeAnlageToAirtable(anlCopy);
        mLogService.writeLog(TAG, 4, "Anlage "+name+" unter neuem Namen: "+newName+" auf Airtable gespeichert");
    }

    /*
     * Anlage von db laden und in die anlageliste einfügen
     */
    public void loadAnlageFromDb(String name) {
        Anlage anl = getAnlageByName(name);
        anl = mDbHelper.getAnlage(anl.getDbId());


        if (anl != null) {
            // über den Namen gehen, weil beim getIndex 0 kommt, wenn die Anlage nicht existiert
            Anlage anlage = getAnlageByName(name);
            if (anlage != null) {
                int i = getAnlageIndex(name);
                // die Anlage im speicher ersetzen
                anlagen.set(i, anl);
            }
        }
    }

    public void saveAnlageToDb(String name) {
        Anlage anl = getAnlageByName(name);
        saveAnlageToDb(anl);
    }

    public void saveAnlageToDb(Anlage anl) {

        if (mDbHelper.existsAnlage(anl.getDbId())) {
            mDbHelper.updateAnlage(anl);
        } else {
            mDbHelper.addAnlage(anl);
        }
    }


    public void deleteAnlageFromDb(String name) {
        Log.d(TAG, "löschen "+name+" von der lokalen db");
        mDbHelper.deleteAnlage(name);
        for (int i = 0;i<anlagen.size();i++) {
            Anlage anlage = anlagen.get(i);
            if (name.equals(anlage.getName())) {
                anlagen.remove(anlage);
            }
        }
    }

    public void reloadAll() {
        List<Anlage> origList = new ArrayList<Anlage>();
        for (int i=0;i<anlagen.size(); i++) {

            Anlage a = anlagen.get(i);
            mLogService.writeLog(TAG, 4, "Anlage "+a.getName()+" von local DB löschen");
            origList.add(a);
            deleteAnlageFromDb(a.getName());
        }
        for (int i=0;i<origList.size(); i++) {
            Anlage a = origList.get(i);

            Anlage b = airtableServiceOn().loadAnlageFromAirtable(a.getName());
            mLogService.writeLog(TAG, 4, "Anlage "+a.getName()+" von Airtable geholt");
            saveAnlageToDb(b);
            origList.set(i,b);
            mLogService.writeLog(TAG, 4, "Anlage "+b.getName()+" auf localdb gespeichert");
        }

    }

    // damit können die Orte und Wege an die Konsole geschickt werden
    public String createDAO(Anlage anlage) {
        AnlageDAO dao = new AnlageDAO();
        dao.anlageName = anlage.getName();
        for (Ort ort : anlage.getOrte()) {
            dao.orte.add(ort.getName());
            dao.radien.put(ort.getName(), ort.getRadius()+"");
        }
        for (Weg weg : anlage.getWege()) {
            dao.wege.add(weg.getName());
            dao.directions.put(weg.getName(), Math.round(weg.getDirection())+"");
            dao.radien.put(weg.getName(), weg.getRadiusNachOrt()+"");
        }
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();
        String daoString = gson.toJson(dao);
        return daoString;
    }

    class AnlageDAO implements Serializable  {
        String anlageName;
        List<String> orte = new ArrayList<String>();
        List<String> wege = new ArrayList<String>();
        Map<String,String> directions = new HashMap<String, String>();
        public Map<String,String> radien = new HashMap<String, String>();
    }


    public Anlage getAnlageByName(String name) {
        for (Anlage anlage : anlagen) {
            if (name.equals(anlage.getName())) {
                return anlage;
            }
        }
        return null;
    }

    public int getAnlageIndexByName(String name) {
        for (int i = 0; i< anlagen.size(); i++) {
            Anlage anlage = anlagen.get(i);
            if (name.equals(anlage.getName())) {
                return i;
            }
        }
        return 0;
    }

    private void dbtester(DatabaseHelper db, Anlage dekanOesch) {
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

        String js = gson.toJson(dekanOesch);
        Anlage al = gson.fromJson(js, Anlage.class);

        List<Anlage> alist = db.getAllAnlagen();
        for (Anlage a : alist) {
            db.deleteAnlage(a);

        }
        Log.d("TTT", "TTT");
    }



    public List<Anlage> getAnlagen() {
      return anlagen;
    }

    public int getAnlageIndex(String name) {
        int i = 0;
        for (Anlage anlage : anlagen){
            if (anlage.getName().equals(name)) {
                return i;
            }
            i++;
        }
        return 0;
    }

        /*
    public Anlage loadAnlagePizol() {
        Anlage anlage = new Anlage("Pizolbahn Ragaz", null);
        anlagen.add(anlage);
        anlage.setVersion("1.0");
        anlage.setDbId("4");
        return anlage;
    }



    public Anlage loadAnlagePizol(Anlage anlage) {
        anlage.setMaxStillstand(180);  // Sekunden

        // das ist die Wangs Anwendung
        anlage.getStories().add(new Story("Mounteens 1", 1));
        anlage.getStories().add(new Story("Mounteens 2", 2));
        anlage.getStories().add(new Story("Heide", 3));
        // nur für Prototyp
        //stories = mAnlage.getStories();

        Ort ortStart = new Ort("Ragaz Unten", 47.016791, 9.473544, 488.7, "Pizolbanh Talstation Ragaz");
        anlage.setHome(ortStart);
        ortStart.setRadius(20);
        ortStart.setBackgroundMediaId(R.raw.musik_talstation);
        anlage.getOrte().add(ortStart);
        Ort ortMitte = new Ort("Ragaz Mittelstation", 47.003378, 9.469271, 1194, "Pizolbahn Mittelstation (fiktiv) Ragaz");
        // das ist wirklich Mitte
        //        Ort ortMitte = new Ort("Ragaz Mittelstation", 46.989317, 9.464773, 1619, "Pizolbanh Mittelstation Ragaz");
        anlage.getOrte().add(ortMitte);
        ortMitte.setRadius(15);
        ortMitte.setBackgroundMediaId(R.raw.musik_mittelstation1);
        Ort ortEnde = new Ort("Ragaz Oben", 46.989317, 9.464773, 1619, "Pizolbahn Bergstation (fiktiv) Ragaz");
        // Das ist wirklich oben
        //Ort ortEnde = new Ort("Ragaz Oben", 46.979781, 9.436314, 2222, "Pizolbahn Bergstation Ragaz");
        anlage.getOrte().add(ortEnde);
        ortEnde.setRadius(20);
        ortEnde.setBackgroundMediaId(R.raw.musik_bergstation);

        anlage.setHome(ortStart);
        //orte = mAnlage.getOrte();

        // die 4 Verbindungen zwischen den Orten erzeugen
        Weg wegRauf1 = new Weg(ortStart, ortMitte);
        wegRauf1.setDirection(190);
        wegRauf1.setBackgroundMediaId(R.raw.musik2);
        anlage.getWege().add(wegRauf1);
        Weg wegRauf2 = new Weg(ortMitte, ortEnde);
        wegRauf2.setDirection(190);
        wegRauf2.setBackgroundMediaId(R.raw.musik3);
        anlage.getWege().add(wegRauf2);
        Weg wegRunter2 = new Weg(ortEnde, ortMitte);
        wegRunter2.setDirection(15);
        wegRunter2.setBackgroundMediaId(R.raw.musik6);
        anlage.getWege().add(wegRunter2);
        Weg wegRunter1 = new Weg(ortMitte, ortStart);
        wegRunter1.setDirection(15);
        wegRunter1.setBackgroundMediaId(R.raw.musik6);
        anlage.getWege().add(wegRunter1);
        //wege = mAnlage.getWege();

        // TODO: eigentlich sind es Actionpoints an denen etwas passiert und eine Liste von Actions die den Actionspoints zugeordnet werden.
        // so kann man unterschiedliche Stories auf denselben Pfad legen
        // eigentoich ist jedes End eines Wegen ein Actionpoint und der Weg slebst ich auch einer.
        // ABFAHRT ud ANKUNFT kann aus dem Ort und dem Weg ermittelt werden. Ist der Ort der VonOrt ist es ABFAHRT
        //Aktion grussUnten = new Aktion("Hello Unten", wegRauf1, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        //mAnlage.getAktionen().add(grussUnten);
        Aktion goodbyeMitte = new Aktion(2, "kurz vor Mitte rauf", wegRauf1, R.raw.story2, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeMitte);
        //Aktion HelloMitte = new Aktion("Hello Mitte", wegRauf2, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        //mAnlage.getAktionen().add(HelloMitte);
        Aktion goodbyeOben = new Aktion(1, "kurz vor Bergstation rauf", wegRauf2, R.raw.story4, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeOben);
        //Aktion helloOben = new Aktion("Hello Oben", wegRunter2 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        //mAnlage.getAktionen().add(helloOben);
        Aktion goodByMitteRunter = new Aktion(5, "kurz vor Mittelstation runter", wegRunter2 , R.raw.story6, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodByMitteRunter);
        //Aktion helloMitteRunter = new Aktion("Hello Mitte runter", wegRunter1 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        //mAnlage.getAktionen().add(helloMitteRunter);
        Aktion helloUnten = new Aktion(1, "Gondel kurz vor Talstation", wegRunter1 ,R.raw.story8, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(helloUnten);

        Aktion StoryRauf1 = new Aktion(1, "Gondel fährt aus Talstation", wegRauf1, R.raw.story1, 0, false, 0, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf1);
        Aktion StoryRauf2 = new Aktion(3, "Gondel kurz vor Mittelstation", wegRauf2, R.raw.story3, 0, false, 0, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf2);
        Aktion StoryRunter2 = new Aktion(4, "Gondel fährt aus Bergstation", wegRunter2, R.raw.story5, 0, false, 0, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter2);
        Aktion StoryRunter1 = new Aktion(1, "Gondel fährt aus Mittelstation runter", wegRunter1, R.raw.story7, 0, false, 0, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter1);

        Aktion background = new Aktion(1, "background Music", wegRauf2, R.raw.musik_talstation, 0, false, 0, Aktion.BACKGROUND);
        anlage.getAktionen().add(background);

        // alle Aktionen werden in eine neue Liste aus Ereignissen übertragen, die Ereignisse wurden noch nicht ausgeführt
        //mAnlage.createNewJourney(mAnlage.getAktionen());
        // TODO: muss für jede Story sichergestellt sein.
        anlage.mapActionToStory(anlage.getId(), 1, anlage.getAktionen());
        //mAnlage.getStoryById(1).setAktionen(mAnlage.getAktionen());
        //Alle Distanzen zu home berechnen, ist aber nicht mehr sicher, dass es das braucht.
        //for (Ort ort : orte) {
        //    ort.setDistHome(ort.calculateDistance(home.getLatitude(), home.getLongitude()));
        //}
        return anlage;

    }

    public Anlage loadAnlagePizolWangs() {
        Anlage anlage = new Anlage("Pizolbahn Wangs", null);
        anlagen.add(anlage);
        anlage.setVersion("1.0");
        anlage.setDbId("1");
        return anlage;
    }

    public Anlage loadAnlagePizolWangs(Anlage anlage) {
            anlage.setMaxStillstand(180);  // Sekunden
        // das ist die Wangs Anwendung
        anlage.getStories().add(new Story("Mounteens 1", 1));
        anlage.getStories().add(new Story("Mounteens 2", 2));
        anlage.getStories().add(new Story("Heide", 3));
        // nur für Prototyp
        //stories = mAnlage.getStories();

        Ort ortStart = new Ort("Wangs Unten", 47.0287214, 9.432169, 0, "Pizolbanh Talstation Wangs");
        anlage.setHome(ortStart);
        ortStart.setRadius(20);
        anlage.getOrte().add(ortStart);
        Ort ortMitte = new Ort("Wangs Mittelstation", 47.0159094, 9.4266425, 0, "Pizolbanh Mittelstation Wangs");
        anlage.getOrte().add(ortMitte);
        ortMitte.setRadius(15);
        Ort ortEnde = new Ort("Wangs Oben", 47.002357, 9.420464, 0, "Pizolbanh Bergstation Wangs");
        anlage.getOrte().add(ortEnde);
        ortEnde.setRadius(20);

        anlage.setHome(ortStart);
        //orte = mAnlage.getOrte();

        // die 4 Verbindungen zwischen den Orten erzeugen
        Weg wegRauf1 = new Weg(ortStart, ortMitte);
        wegRauf1.setDirection(190);
        anlage.getWege().add(wegRauf1);
        Weg wegRauf2 = new Weg(ortMitte, ortEnde);
        wegRauf2.setDirection(190);
        anlage.getWege().add(wegRauf2);
        Weg wegRunter2 = new Weg(ortEnde, ortMitte);
        wegRunter2.setDirection(15);
        anlage.getWege().add(wegRunter2);
        Weg wegRunter1 = new Weg(ortMitte, ortStart);
        wegRunter1.setDirection(15);
        anlage.getWege().add(wegRunter1);
        //wege = mAnlage.getWege();

        // TODO: eigentlich sind es Actionpoints an denen etwas passiert udn eine Liste von Actions die den Actionspoints zugeordnet werden.
        // so kann man unterschiedliche Stories auf denselben Pfad legen
        // eigentoich ist jedes End eines Wegen ein Actionpoint und der Weg slebst ich auch einer.
        // ABFAHRT ud ANKUNFT kann aus dem Ort und dem Weg ermittelt werden. Ist der Ort der VonOrt ist es ABFAHRT
        Aktion grussUnten = new Aktion(1, "Hello Unten", wegRauf1, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(grussUnten);
        Aktion goodbyeMitte = new Aktion(1, "Goodby Mitte", wegRauf1, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeMitte);
        Aktion HelloMitte = new Aktion(1, "Hello Mitte", wegRauf2, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(HelloMitte);
        Aktion goodbyeOben = new Aktion(1, "GoodBy Oben", wegRauf2, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeOben);
        Aktion helloOben = new Aktion(1, "Hello Oben", wegRunter2 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloOben);
        Aktion goodByMitteRunter = new Aktion(1, "GoodBy Mitte runter", wegRunter2 , R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodByMitteRunter);
        Aktion helloMitteRunter = new Aktion(1, "Hello Mitte runter", wegRunter1 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloMitteRunter);
        Aktion helloUnten = new Aktion(1, "Goodby unten", wegRunter1 ,R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(helloUnten);

        Aktion StoryRauf1 = new Aktion(1, "Story Rauf 1", wegRauf1, R.raw.t02_zuviel_action_teil_01, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf1);
        Aktion StoryRauf2 = new Aktion(1, "Story Rauf 2", wegRauf2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf2);
        Aktion StoryRunter2 = new Aktion(1, "Story Runter 2", wegRunter2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter2);
        Aktion StoryRunter1 = new Aktion(1, "Story Runter 1", wegRunter1, R.raw.t02_zuviel_action_teil_01, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter1);

        Aktion background = new Aktion(1, "background Music", wegRauf2, R.raw.fatima_dunn_05_nebelwald, 0, false, 0, Aktion.BACKGROUND);
        anlage.getAktionen().add(background);

        // alle Aktionen werden in eine neue Liste aus Ereignissen übertragen, die Ereignisse wurden noch nicht ausgeführt
        //mAnlage.createNewJourney(mAnlage.getAktionen());
        // TODO: muss für jede Story sichergestellt sein.
        anlage.mapActionToStory(anlage.getId(), 1, anlage.getAktionen());
        //mAnlage.getStoryById(1).setAktionen(mAnlage.getAktionen());
        //Alle Distanzen zu home berechnen, ist aber nicht mehr sicher, dass es das braucht.
        //for (Ort ort : orte) {
        //    ort.setDistHome(ort.calculateDistance(home.getLatitude(), home.getLongitude()));
        //}
        return anlage;

    }



    public Anlage loadAnlageBadRagaz() {
        Anlage anlage = new Anlage("Bad Ragaz", null);
        anlagen.add(anlage);
        anlage.setVersion("1.0");
        anlage.setDbId("2");
        return anlage;
    }

    public Anlage loadAnlageBadRagaz(Anlage anlage) {
        anlage.setMaxStillstand(30);  // Sekunden
        // das ist die Wangs Anwendung
        anlage.getStories().add(new Story("Weg zum Bahnhof", 1));
        anlage.getStories().add(new Story("Weg ins Dorf", 2));
        // nur für Prototyp
        //stories = mAnlage.getStories();

        Ort ortStart = new Ort("Dekan-Oesch-Strasse 10", 47.005798, 9.5020390, 0, "Dekan-Oesch-Strasse 10");
        anlage.setHome(ortStart);
        anlage.getOrte().add(ortStart);
        //47.007083, 9.503614
        Ort ortMitte = new Ort("Speckhaus", 47.007083, 9.503614, 0, "Speckhaus");
        anlage.getOrte().add(ortMitte);
        // 47.010661, 9.504807
        Ort ortEnde = new Ort("Bahnhof", 47.010239, 9.504647, 0, "Bahnhof");
        anlage.getOrte().add(ortEnde);


        //orte = mAnlage.getOrte();

        // die 4 Verbindungen zwischen den Orten erzeugen
        Weg wegRauf1 = new Weg(ortStart, ortMitte);
        anlage.getWege().add(wegRauf1);
        Weg wegRauf2 = new Weg(ortMitte, ortEnde);
        anlage.getWege().add(wegRauf2);
        Weg wegRunter2 = new Weg(ortEnde, ortMitte);
        anlage.getWege().add(wegRunter2);
        Weg wegRunter1 = new Weg(ortMitte, ortStart);
        anlage.getWege().add(wegRunter1);
        //wege = mAnlage.getWege();

        Aktion grussUnten = new Aktion(1, "Hello Unten", wegRauf1, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(grussUnten);
        Aktion goodbyeMitte = new Aktion(1, "Goodby Mitte", wegRauf1, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeMitte);
        Aktion HelloMitte = new Aktion(1, "Hello Mitte", wegRauf2, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(HelloMitte);
        Aktion goodbyeOben = new Aktion(1, "GoodBy Oben", wegRauf2, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeOben);
        Aktion helloOben = new Aktion(1, "Hello Oben", wegRunter2 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloOben);
        Aktion goodByMitteRunter = new Aktion(1, "GoodBy Mitte runter", wegRunter2 , R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodByMitteRunter);
        Aktion helloMitteRunter = new Aktion(1, "Hello Mitte runter", wegRunter1 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloMitteRunter);
        Aktion helloUnten = new Aktion(1, "Goodby unten", wegRunter1 ,R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(helloUnten);

        Aktion StoryRauf1 = new Aktion(1, "Story Rauf 1", wegRauf1, R.raw.t02_zuviel_action_teil_01, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf1);
        Aktion StoryRauf2 = new Aktion(1, "Story Rauf 2", wegRauf2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf2);
        Aktion StoryRunter2 = new Aktion(1, "Story Runter 2", wegRunter2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter2);
        Aktion StoryRunter1 = new Aktion(1, "Story Runter 1", wegRunter1, R.raw.t02_zuviel_action_teil_01, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter1);


        Aktion background = new Aktion(1, "background Music", wegRauf2, R.raw.jazz_in_paris, 0, false, 0, Aktion.BACKGROUND);
        anlage.getAktionen().add(background);

        // alle Aktionen werden in eine neue Liste aus Ereignissen übertragen, die Ereignisse wurden noch nicht ausgeführt
        //mAnlage.createNewJourney(mAnlage.getAktionen());
        // TODO: muss für jede Story sichergestellt sein.
        anlage.mapActionToStory(anlage.getId(), 1, anlage.getAktionen());
        //mAnlage.getStoryById(1).setAktionen(mAnlage.getAktionen());
        //Alle Distanzen zu home berechnen, ist aber nicht mehr sicher, dass es das braucht.
        //for (Ort ort : orte) {
        //    ort.setDistHome(ort.calculateDistance(home.getLatitude(), home.getLongitude()));
        //}

    return anlage;

    }


    public Anlage loadAnlageDekanOesch() {
        Anlage anlage = new Anlage("Dekan-Oesch", null);
        anlagen.add(anlage);
        anlage.setVersion("1.0");
        anlage.setDbId("3");
        return anlage;
    }

    public Anlage loadAnlageDekanOesch(Anlage anlage) {
        anlage.setMaxStillstand(30);  // Sekunden

        // das ist die Wangs Anwendung
        anlage.getStories().add(new Story("Zum Begegnungszenter", 1));
        anlage.getStories().add(new Story("Weg ins Dorf", 2));
        // nur für Prototyp
        //stories = mAnlage.getStories();

        Ort ortStart = new Ort("Dekan-Oesch-Strasse 10 Home", 47.0060766, 9.5016545, 0, "Dekan-Oesch-Strasse 10");
        anlage.setHome(ortStart);
        anlage.getOrte().add(ortStart);
        //47.007083, 9.503614
        Ort ortMitte = new Ort("Hundesalon", 47.0057049, 9.5006846, 0, "Hundesalon");
        anlage.getOrte().add(ortMitte);
        // 47.010661, 9.504807
        Ort ortEnde = new Ort("Begegnungszenter", 47.0053733, 9.4999192, 0, "Begegnungszenter");
        anlage.getOrte().add(ortEnde);

        anlage.setHome(ortStart);
        //orte = mAnlage.getOrte();

        // die 4 Verbindungen zwischen den Orten erzeugen
        Weg wegRauf1 = new Weg(ortStart, ortMitte);
        wegRauf1.setDirection(238);
        anlage.getWege().add(wegRauf1);
        Weg wegRauf2 = new Weg(ortMitte, ortEnde);
        wegRauf2.setDirection(240);
        anlage.getWege().add(wegRauf2);
        Weg wegRunter2 = new Weg(ortEnde, ortMitte);
        anlage.getWege().add(wegRunter2);
        wegRunter2.setDirection(74);
        Weg wegRunter1 = new Weg(ortMitte, ortStart);
        anlage.getWege().add(wegRunter1);
        wegRunter1.setDirection(74);
        //wege = mAnlage.getWege();

        Aktion grussUnten = new Aktion(1, "Hello Unten", wegRauf1, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(grussUnten);
        Aktion goodbyeMitte = new Aktion(1, "Goodby Mitte", wegRauf1, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeMitte);
        Aktion HelloMitte = new Aktion(1, "Hello Mitte", wegRauf2, R.raw.hello_unten, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(HelloMitte);
        Aktion goodbyeOben = new Aktion(1, "GoodBy Oben", wegRauf2, R.raw.goodbyoben, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodbyeOben);
        Aktion helloOben = new Aktion(1, "Hello Oben", wegRunter2 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloOben);
        Aktion goodByMitteRunter = new Aktion(1, "GoodBy Mitte runter", wegRunter2 , R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(goodByMitteRunter);
        Aktion helloMitteRunter = new Aktion(1, "Hello Mitte runter", wegRunter1 ,R.raw.hellooben, 0, false, 0, Aktion.HELLO);
        anlage.getAktionen().add(helloMitteRunter);
        Aktion helloUnten = new Aktion(1, "Goodby unten", wegRunter1 ,R.raw.goodbyunten, 0, false, 0, Aktion.GOODBY);
        anlage.getAktionen().add(helloUnten);

        Aktion StoryRauf1 = new Aktion(1, "Story Rauf 1", wegRauf1, R.raw.sample, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf1);
        Aktion StoryRauf2 = new Aktion(1, "Story Rauf 2", wegRauf2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRauf2);
        Aktion StoryRunter2 = new Aktion(1, "Story Runter 2", wegRunter2, R.raw.nuschelpeter, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter2);
        Aktion StoryRunter1 = new Aktion(1, "Story Runter 1", wegRunter1, R.raw.t02_zuviel_action_teil_01, 0, false, 20, Aktion.STORY);
        anlage.getAktionen().add(StoryRunter1);


        Aktion background = new Aktion(1, "background Music", wegRauf1, R.raw.fatima_dunn_05_nebelwald, 0, false, 0, Aktion.BACKGROUND);
        anlage.getAktionen().add(background);

        // alle Aktionen werden in eine neue Liste aus Ereignissen übertragen, die Ereignisse wurden noch nicht ausgeführt
        //mAnlage.createNewJourney(mAnlage.getAktionen());
        // TODO: muss für jede Story sichergestellt sein.
        anlage.mapActionToStory(anlage.getId(), 1, anlage.getAktionen());
        //mAnlage.getStoryById(1).setAktionen(mAnlage.getAktionen());
        //Alle Distanzen zu home berechnen, ist aber nicht mehr sicher, dass es das braucht.
        //for (Ort ort : orte) {
        //    ort.setDistHome(ort.calculateDistance(home.getLatitude(), home.getLongitude()));
        //}

        return anlage;

    }

    */

    private void test(Anlage anlage) {


    }

    // Methode um einen Ort zu erzeuigen und gelich in die Liste abzulegen
    public void createOrt(List<Ort> orte) {

    }


}
