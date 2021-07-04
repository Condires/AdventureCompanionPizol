package com.condires.adventure.companion.recorder;

import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.condires.adventure.companion.Data;
import com.condires.adventure.companion.GpsManager;
import com.condires.adventure.companion.R;
import com.condires.adventure.companion.model.Aktion;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Weg;
import com.google.android.gms.location.LocationResult;

import static android.widget.Toast.makeText;

public class AdventureRecorderActivity extends AppCompatActivity implements OnItemSelectedListener  {
    private String TAG = this.getClass().getSimpleName();
    Data data = Data.getInstance();
    RecorderStatus recorderStatus;
    GpsManager gpsManager = new GpsManager();
    Anlage anlage;
    int anlageIndex = 0;

    private TextView nachOrt;
    private TextView vonOrt;
    private TextView currentOrt;

    private TextView mHello;
    private TextView mGoodby;
    private TextView mStory;

    private SeekBar mSeekbarTraveller;
    private boolean mUserIsSeeking = false;

    ArrayAdapter<Anlage> mAnlageArrayAdapter;

    Button mNewAnlageButton;
    Button mStartButton;
    Button mVonOrtButton;
    Button mNachOrtButton;
    Button mNextWegButton;
    Button mNewHelloActionButton;
    Button mNewGoodByActionButton;
    Button mNewStoryActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        recorderStatus = new RecorderStatus();

        setContentView(R.layout.activity_adventure_recorder);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initializeSeekbar();  // Achtung seekbar muss vor dem UI erstellt werden
        initializeUI();


        GpsManager.Callback mLocationCallback = new GpsManager.Callback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    recorderStatus.setLatitude(location.getLatitude());
                    recorderStatus.setLongitude(location.getLongitude());
                    float speed = location.getSpeed();
                    // Richtung nur nehmen wen wir uns bewegen
                    if (speed > 0.9) {
                        float direction = location.getBearing();
                        recorderStatus.setDirection(direction);
                    }
                    recorderStatus.setSpeed(speed);
                    recorderStatus.setAccuracy(location.getAccuracy());
                    Ort myOrt = new Ort(location.getLatitude(), location.getLongitude());
                    //myOrt.setName(ortName.getText());
                    myOrt.setName("Name noch holen");
                    recorderStatus.setCurrentOrt(myOrt);
                    displayUI();
                }
            }
        };

        gpsManager.start(this, mLocationCallback);

    }

    private void initializeSeekbar() {
        mSeekbarTraveller = (SeekBar) findViewById(R.id.seekBar_weg);
        mSeekbarTraveller.setOnSeekBarChangeListener(
                new SeekBar.OnSeekBarChangeListener() {
                    int userSelectedPosition = 0;

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = true;
                    }

                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            userSelectedPosition = progress;
                        }
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mUserIsSeeking = false;
                        // TODO MyPosition entsprechend bewegen (lat und long berechnen
                        // Distanz weglänge
                        Weg weg = recorderStatus.getCurrentWeg();
                        if (weg != null) {
                            Ort vonOrt = weg.getVonOrt();
                            if (vonOrt != null) {
                                vonOrt.calculateDistance(weg.getNachOrt());
                            }
                        }
                        //mPlayerAdapter.seekTo(userSelectedPosition);
                    }
                });
    }

    private void initializeUI() {
        //TODO text_weg_name Text Field erstellen
        final EditText ortName = findViewById(R.id.text_ort_name);
        ortName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                recorderStatus.setOrtName(ortName.getText().toString());
            }
        });


        currentOrt = findViewById(R.id.text_view_current_ort);
        nachOrt = findViewById(R.id.text_view_nachOrt);
        vonOrt = findViewById(R.id.text_view_von_ort);
        mHello = findViewById(R.id.text_view_m_hello);
        mGoodby = findViewById(R.id.text_view_m_goodby);
        mStory = findViewById(R.id.text_view_m_story);

        // anlagespinner
        final Spinner mAnlageSpinner = (Spinner) findViewById(R.id.spinner_anlage);
        // Spinner click listener
        mAnlageSpinner.setOnItemSelectedListener(this);
        // Creating adapter for spinner
        mAnlageArrayAdapter = new ArrayAdapter<Anlage>(this,
                android.R.layout.simple_spinner_item, data.getAnlagen());
        // Drop down layout style - list view with radio button
        mAnlageArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // attaching data adapter to spinner
        mAnlageSpinner.setAdapter(mAnlageArrayAdapter);

        mNewAnlageButton = (Button) findViewById(R.id.button_new_anlage);
        mStartButton = (Button) findViewById(R.id.button_start);
        mVonOrtButton = (Button) findViewById(R.id.button_set_von_ort);
        mNachOrtButton = (Button) findViewById(R.id.button_set_nach_ort);
        mNextWegButton = (Button) findViewById(R.id.button_start_next_weg);
        mNewHelloActionButton = (Button) findViewById(R.id.button_new_hello_action);
        mNewGoodByActionButton = (Button) findViewById(R.id.button_new_goodby_action);
        mNewStoryActionButton = (Button) findViewById(R.id.button_new_story_action);


        //mTextDebug = (TextView) findViewById(R.id.text_debug);

        mNewAnlageButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (recorderStatus.getStatus() == 1) {
                            //TODO prüfen, ob schon etwas bearbietet wurde, dass noch gespeichert wreden muss
                        }
                        Anlage anl = new Anlage();
                        recorderStatus.setAnlage(anl);
                        anl.setName(recorderStatus.getOrtName());
                        recorderStatus.setStatus(1);  // 0 = nichts gespeichert
                        data.getAnlagen().add(recorderStatus.getAnlage());
                        // neue Anlage im Drop Down Spinner anzeigen
                        //mAnlageArrayAdapter.add(recorderStatus.getAnlage());
                        mAnlageSpinner.setSelection(mAnlageArrayAdapter.getPosition(recorderStatus.getAnlage()));
                        mNachOrtButton.setActivated(false);
                    }
                });

        // eine neue Anlage startet hier
        mStartButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: wenn die Anlage schon mal gestartet wurde, fragen, ob man nochmals von Vorne beginnen will
                        Ort myPos = recorderStatus.getCurrentOrt();
                        if (myPos != null) {
                            myPos.setName(recorderStatus.getOrtName());
                        }
                        recorderStatus.setHomeOrt(recorderStatus.getCurrentOrt());
                        recorderStatus.setVonOrt(recorderStatus.getCurrentOrt());
                        anlage.getOrte().add(recorderStatus.getCurrentOrt());
                        anlage.setHome(recorderStatus.getCurrentOrt());
                    }
                });

        // der ganze Weg wird angelegt, von, nach und Weg
        mVonOrtButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // nimmt die aktuelle Position als Weg Anfang
                        Ort myPos = recorderStatus.getCurrentOrt();
                        if (myPos != null) {
                            myPos.setName(recorderStatus.getOrtName());
                        }
                        recorderStatus.setVonOrt(recorderStatus.getCurrentOrt());

                        // prüfen ob der Ort schon existiert
                        if (!anlage.existsOrt(recorderStatus.getCurrentOrt())) {
                            anlage.getOrte().add(recorderStatus.getCurrentOrt());
                        }
                        Ort nachOrt = new Ort("noname");
                        if (!anlage.existsOrt(nachOrt)) {
                            anlage.getOrte().add(nachOrt);
                        }
                        recorderStatus.setNachOrt(nachOrt);

                        // TODO den nachort einfügen, wegen actions auf dem Weg zum nachort
                        Weg weg = new Weg(recorderStatus.getVonOrt(), nachOrt);
                        recorderStatus.setCurrentWeg(weg);
                        anlage.getWege().add(weg);
                        mNachOrtButton.setActivated(true);
                    }
                });

        // Nachort heisst, wir sind am Ende eines Weges angekommen, wir müssen festlegenb, ob wir jetzt im Zentrum oder am Radius des Nachortes sind.
        // Der Weg wird erzeugt und als Weg eingetragen.
        // der Nachort wird jetzt automatisch zum vonOrt für den nächsten Weg, ausser wenn wir am Ziel angekommen sind.
        mNachOrtButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // nimmt die aktuelle Position als Weg Ende und erzeugt den Weg
                        Ort hier = recorderStatus.getCurrentOrt();
                        if (hier != null) {
                            hier.setName(recorderStatus.getOrtName());
                        }
                        Ort nachOrt = recorderStatus.getNachOrt();
                        // wenn vonOrt nicht angeklickt wurde, existiert der Weg nicht
                        // es macht keinen Sinn, den Weg Rückwärts aufzuzeichnen
                        if (nachOrt != null) {
                            //recorderStatus.setNachOrt(nachOrt);
                            nachOrt.setLatitude(recorderStatus.getCurrentOrt().getLatitude());
                            nachOrt.setLongitude(recorderStatus.getCurrentOrt().getLongitude());
                            nachOrt.setName(recorderStatus.getCurrentOrt().getName());
                            Weg weg = recorderStatus.getCurrentWeg();
                            weg.resetWegName();
                            weg.setDirection(recorderStatus.getDirection());
                            mSeekbarTraveller.setMax((int) weg.calculateLength());
                        } else {
                            Toast.makeText(AdventureRecorderActivity.this, "Zuerst Von Ort festlegen: " , Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        // TODO brauche einen Button "Weiter", damit ich einfach vom nachOrt aus einen neuen Weg anhängen kann
        mNextWegButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // nachOrt wird zu vonort, der weg wird neu gemacht
                        recorderStatus.setVonOrt(recorderStatus.getNachOrt());
                        Ort nachOrt = new Ort("noname");
                        if (!anlage.existsOrt(nachOrt)) {
                            anlage.getOrte().add(nachOrt);
                        }
                        recorderStatus.setNachOrt(nachOrt);

                        // TODO den nachort einfügen, wegen actions auf dem Weg zum nachort
                        Weg weg = new Weg(recorderStatus.getVonOrt(), nachOrt);
                        recorderStatus.setCurrentWeg(weg);
                        anlage.getWege().add(weg);
                    }
                });


        mNewHelloActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // als Begrüssung beim Verlassen des Startortes anhängen
                        // TODO: den sound oder das Bild ab Microfon oder aus der Mediathek holen

                        Weg weg = recorderStatus.getCurrentWeg();
                        Ort nachOrt = weg.getNachOrt();

                        long dist = recorderStatus.getCurrentOrt().calculateDistance(nachOrt);
                        mHello.setText(dist + "m");
                        Aktion helloAction = new Aktion(1, "Hello "+ weg.getNachOrt().getName(), weg, R.raw.hello_unten, 0, false, dist, Aktion.HELLO,0);
                        anlage.getAktionen().add(helloAction);

                    }
                });

        mNewGoodByActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: den sound oder das Bild ab Microfon oder aus der Mediathek holen
                        Weg weg = recorderStatus.getCurrentWeg();
                        Ort vonOrt = weg.getVonOrt();
                        long dist = recorderStatus.getCurrentOrt().calculateDistance(vonOrt);
                        mGoodby.setText(dist +" m");
                        Aktion byAction = new Aktion(3, "Goodby "+ weg.getVonOrt().getName(), weg, R.raw.goodbyoben, 0, false, dist, Aktion.GOODBY,0);
                        anlage.getAktionen().add(byAction);
                        // als Goodby beim verlassen eines Ortes anhängen
                    }
                });

        mNewStoryActionButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO: den sound oder das Bild ab Microfon oder aus der Mediathek holen
                        Weg weg = recorderStatus.getCurrentWeg();
                        Ort vonOrt = weg.getVonOrt();
                        String ortName = "";
                        if (vonOrt != null) {
                            ortName = weg.getVonOrt().getName();
                        }
                        long dist = recorderStatus.getCurrentOrt().calculateDistance(vonOrt);
                        mStory.setText(dist+" m");
                        Aktion storyAction = new Aktion(2, "Story "+ ortName, weg, R.raw.musik6, 0, false, dist, Aktion.STORY,0);
                        anlage.getAktionen().add(storyAction);
                        // als Goodby beim verlassen eines Ortes anhängen
                    }
                });


    }

    public void displayUI () {
        displayLocation(currentOrt, "MyPos", recorderStatus.getCurrentOrt());
        currentOrt.append("\nAcc : " + (int) recorderStatus.getAccuracy());
        currentOrt.append(" Spe : " + (int) recorderStatus.getSpeed());

        displayLocation( vonOrt,"VonOrt", recorderStatus.getVonOrt());
        displayLocation(nachOrt, "NachOrt", recorderStatus.getNachOrt());

    }

    private void displayLocation(TextView mTextView, String label, Ort ort) {

        if (ort != null) {
            mTextView.setText(
                    label +" : " + ort.getName()
                    + "\nLat : " + ort.getLatitude()
                            + "\nLat : " + ort.getLongitude()
            );
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // On selecting a spinner item
        Object o = parent.getItemAtPosition(position);
        if (o instanceof Anlage) {
            Anlage anl = (Anlage) parent.getItemAtPosition(position);
            anlage = anl;
            anlageIndex = data.getAnlageIndex(anlage.getName());
            // Showing selected spinner item
            makeText(parent.getContext(), "Selected: " + anl, Toast.LENGTH_SHORT).show();
        }


    }
    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onBackPressed() {
        if (gpsManager != null) gpsManager.stop();
    }


}
