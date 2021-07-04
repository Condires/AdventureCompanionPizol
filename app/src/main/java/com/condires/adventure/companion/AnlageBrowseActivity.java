package com.condires.adventure.companion;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.util.Log;

import com.condires.adventure.companion.model.Anlage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AnlageBrowseActivity extends AppCompatActivity {
    private TextView maintext;

    private String TAG = this.getClass().getSimpleName();
    private Data data;
    private Anlage anlage;
    Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anlage_browse);

        final Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int i = getIntent().getIntExtra("AnlageIndex", 0);
            data = Data.getInstance();
            if (data == null) {
                data = new Data(getApplicationContext());
            }
            // holt die Anlage am Index i und stellt sicher, dass sie geladen ist
            anlage = data.getAnlage(i);
            //String json = gson.toJsonTree(anlage);
            String json = gson.toJson(anlage);

            maintext = findViewById(R.id.maintext);
            maintext.setText(json.toString());
        } else {
            Log.d(TAG, "keine Anlage ausgewählt");
        }

    }

}
