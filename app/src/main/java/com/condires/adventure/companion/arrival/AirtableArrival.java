package com.condires.adventure.companion.arrival;

import android.content.Context;
import android.util.Log;

import com.condires.adventure.companion.airtable.AirtableService;
import com.sybit.airtableandroid.Table;

import java.util.Date;
import java.util.List;

public class AirtableArrival {

    private String TAG = this.getClass().getSimpleName();
    // Wird benötigt um Log Meldungen an Airtable zu schicken
    // Auf Airtable werden Logmeldungen abgelegt.
    private AirtableService airtableService;
    private String          deviceName;  // der name des Gerätes
    private static AirLogArrival   airLogArrival; // die id des Datensatzes auf Airlog
    private Context context;
    private Table mTable;

    public AirtableArrival(Context context, String deviceName, AirtableService airtableService) {
        this.context = context;
        this.deviceName = deviceName;
        this.airtableService = airtableService;
        mTable = airtableService.getArrivalTable();
    }

    public void write(float speed, String zielOrtName, double distToZiel) {
        if (airLogArrival == null) {
            airLogArrival = new AirLogArrival();
        }
        airLogArrival.setSpeed(speed*3.6f);
        airLogArrival.setLocation(zielOrtName);
        airLogArrival.setDistance(distToZiel);
        airLogArrival.setNow(new Date());
        Log.d("XXX","Now new Date() ist:"+new Date());
        airLogArrival.setDevice(deviceName);
        writeArrivalInfo(airLogArrival);
    }

    /*
     * schreibt den Datensatz für dieses Device auf airlog, falls der Record noch nicht existiert wird er erzeugt
     */
    private void writeArrivalInfo(AirLogArrival airLogArrival) {

        //if (airtableService.isInternetAvailable(this.context)) {
        if (airtableService.isInternetAvailable2()) {
            // wenn die id leer ist, haben wir noch nie geschrieben seit wir laufen
            if (airLogArrival.getId() == null) {
                //mAirLogs = new ArrayList<String>();
                // testen ob es den Record schon gibt // "{Name} = 'Bad Ragaz'" TODO geht sehr lange
                List msgs = airtableService.selectFiltered(mTable, "{Player} = '" + deviceName + "'");
                if (msgs == null) {
                    Log.d(TAG, "wir haben keine Airtable Connection");
                    airLogArrival = (AirLogArrival) airtableService.write(mTable, airLogArrival);
                } else if (msgs.size() == 0) {
                    // Datensatz schreiben, falls er noch nicht existiert
                    // Message in Record schreiben
                    airLogArrival = (AirLogArrival) airtableService.write(mTable, airLogArrival);
                } else {
                    airLogArrival.setId(((AirLogArrival) msgs.get(0)).getId());
                    airLogArrival = (AirLogArrival) airtableService.update(mTable, airLogArrival);
                }
                if (airLogArrival != null) {
                    airLogArrival.setId(airLogArrival.getId());
                }
            } else {
                airLogArrival = (AirLogArrival) airtableService.update(mTable, airLogArrival);
            }
        }
    }

}
