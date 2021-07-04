package com.conteus.adventure.companion;

import com.condires.adventure.companion.R;
import com.condires.adventure.companion.model.Aktion;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Story;
import com.condires.adventure.companion.model.Weg;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class AnlageUnitTest {
    public         List<Anlage> anlagen = new ArrayList<Anlage>();

    @Test
    public void anlageListeErstellen() {
        Anlage anlage = new Anlage("Pizolbahn Wangs", null);
        anlagen.add(anlage);
        anlage.setVersion("1.0");
        anlage.setDbId("1");
        assertEquals(1.0, anlage.getVersion());
        assertEquals(1, anlage.getDbId());

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


}