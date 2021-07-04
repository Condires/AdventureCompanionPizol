package com.condires.adventure.companion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.condires.adventure.companion.airtable.AirtableService;
import com.condires.adventure.companion.logwrapper.Log;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.AnlageObj;
import com.condires.adventure.companion.model.Ort;
import com.sybit.airtableandroid.Table;

import java.util.List;

public class AirtableEditActivity extends AppCompatActivity {
    private TextView maintext;
    private AirtableService mAirtableService;
    private String TAG = this.getClass().getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airtable_edit);

        maintext = findViewById(R.id.maintext);
        maintext.setText("neues Hello World");


        mAirtableService = AirtableService.getInstance();
        mAirtableService.onStart();
        Log.d(TAG,"alle Anlagen lesen");
        List<Anlage> anlagen = mAirtableService.selectAll(mAirtableService.getAnlageTable());

        maintext.setText(anlagen.toString());
        Log.d(TAG,"alle Orte lesen");
        List<Ort> orte = mAirtableService.selectAll(mAirtableService.getOrtTable());
        maintext.append(orte.toString());


        //"{Name} = 'Bad Ragaz'"
        Log.d(TAG,"eine Anlage lesen");
        List<AnlageObj> anlagen1 = mAirtableService.selectFiltered(mAirtableService.getAnlageTable(),"{Name} = 'Dekan-Oesch'");
        if (anlagen1 != null) {
            maintext.append(anlagen1.toString());
            Anlage anl = new Anlage(anlagen1.get(0));
            if (anl != null) {
                Log.d(TAG, "loadAnlageFromAirtable alle Orte der Anlage lesen");

                anl = mAirtableService.loadAnlageFromAirtable("Bad Ragaz");
                maintext.append(anl.getOrte().toString());
                maintext.append(anl.getWege().toString());
                maintext.append(anl.getAktionen().toString());
            }

            //mAirtableService.test(mAirtableService.getActorTable());
            // es wird eine neue Anlage angelegt
            //Anlage ao = new Anlage("Test",null,null,1000);
            // gelesene ANlage als neue schreiben
            anl.setId(null);
            anl.setName("Ragaz Home");
            //mAirtableService.write(mAirtableService.getAnlageTable(), anl);
            AnlageObj ao = new AnlageObj(anl);
            //writeAnlageToAirtable(anl);
            Table anlageTable = mAirtableService.getAnlageTable();
            AnlageObj ao1 = (AnlageObj) mAirtableService.insertUpdateAirtableObject(ao,"{Name} = '" + anl.getName() + "'" );

        } else {
            Log.d(TAG, "alles schief gelaufen, keine Anlagen von Airtable gelesen");
        }


    }



}
