package com.condires.adventure.companion.airtable;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.condires.adventure.companion.arrival.AirLogArrival;
import com.condires.adventure.companion.logwrapper.Log;
import com.condires.adventure.companion.model.Aktion;
import com.condires.adventure.companion.model.Anlage;
import com.condires.adventure.companion.model.AnlageObj;
import com.condires.adventure.companion.model.Ort;
import com.condires.adventure.companion.model.Weg;
import com.condires.adventure.companion.setting.ACSettings;

import com.sybit.airtableandroid.Airtable;
import com.sybit.airtableandroid.Base;
import com.sybit.airtableandroid.Query;
import com.sybit.airtableandroid.Sort;
import com.sybit.airtableandroid.Table;
import com.sybit.airtableandroid.exception.AirtableException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirtableService {
    private static String TAG = "AirtableService";
    Table<Anlage> mAnlageTable;
    Table mOrtTable;
    Table mWegTable;
    Table mAktionTable;
    Table mLogTable;
    Table mArrivalTable;
    Map<String, Table> mTableMap;

    Base base;

    AirtableService mAirtableService = this;


    // Eine (versteckte) Klassenvariable vom Typ der eigenen Klasse
    private static AirtableService instance;

    public static AirtableService getInstance () {
        if (AirtableService.instance == null) {
            AirtableService.instance = new AirtableService ();

        }
        return AirtableService.instance;
    }

    // Verhindere die Erzeugung des Objektes über andere Methoden
    private AirtableService () {
        if (instance == null) {
            instance = this;
        }
        onStart();
    }


    public void onStart() {
        String apiKey = "keyWCD7E0sT9IEdso";
        Airtable airtable = new Airtable();
        try {
            airtable.configure(apiKey);
        } catch (
                AirtableException e) {
            e.printStackTrace();
        }
        String airtableBase = "appwGx8Y1wzfgkR4H";

        base = null;
        try {
            base = airtable.base(airtableBase);
        } catch (AirtableException e) {
            e.printStackTrace();
        }

        mTableMap = new HashMap();

        /*
        mAnlageTable = base.table("Anlage", AnlageObj.class);
        mOrtTable = base.table("Ort", Ort.class);
        mWegTable = base.table("Weg", Weg.class);
        mAktionTable = base.table("Aktion", Aktion.class);
        mActorTable = base.table("Actor", Actor.class);
        mLogTable = base.table("Log", AirLogMsg.class);
        mArrivalTable = base.table("Position", AirLogArrival.class);
        */

        //TODO: kann durch getTableByClass eliminiert werden
        mAnlageTable = createTableObj(base, AnlageObj.class);
        mOrtTable = createTableObj(base, Ort.class);
        mWegTable = createTableObj(base, Weg.class);
        mAktionTable = createTableObj(base, Aktion.class);
        mLogTable = createTableObj(base, AirLogMsg.class);
        mArrivalTable = createTableObj(base, AirLogArrival.class);

        /*
        List<Anlage> anlageliste1 = (List<Anlage>) runThread((new AirtableCall(mAnlageTable, AirtableCall.SELECT_ID, "recREgsvjqYY4V2Ok")));
        System.out.println(anlageliste1);
        */
        //List<Anlage> anlageliste1 = (List<Anlage>) runThread((new AirtableCall(mAnlageTable, AirtableCall.SELECT_FILTER)));
        //System.out.println(anlageliste1);

    }
    public Table getTableByClass(Class c) {
        return createTableObj(base, c);
    }

    /*
     * erzeugt ein Table Objekt für die Klasse c, die Klasse c muss das Interface AirtableObject implementieren
     * Die Table wird in die TableMap eingefügt dadruch kann sie später mit dem Tabellen Namen geholt werden
     * Die Tabellennamen sind in den AirtableObjekten definiert.
     */
    public Table createTableObj(Base base, Class c){
        AirtableObject ob = null;
        Table myTable = null;
        try {
            ob = (AirtableObject) c.newInstance();
        } catch (Exception ex) {
            Log.e(TAG, "kann Objekt nicht erzeugen", ex);
        }
        if (ob != null) {
            myTable =  base.table(ob.getTableName(), c);
            mTableMap.put(ob.getTableName(),myTable);
        }
        return myTable;
    }

    //TODO auf deprecated setzen
    public Table getTableByName(String tableName) {
        return mTableMap.get(tableName);
    }

    // TODO diese können ersetzt werden durch getTableByClass, wobei das DatenObjekt den Namen kennt
    public Table getOrtTable() {return mOrtTable;}
    public Table getAnlageTable() {return mAnlageTable;}
    public Table getWegTable() {return mWegTable;}
    public Table getAktionTable() {return mAktionTable;}
    public Table getLogTable() {return mLogTable;}
    public Table getArrivalTable() {return mArrivalTable;}



    // wir können ACSettings von Airtable laden, ACSettings speichert sie direkt in den Preferences
    public ACSettings loadSettingFromAirtable(String anlageName) {
        Table table = mAirtableService.getTableByClass(ACSettings.class);
        ACSettings ACSettings = (ACSettings) mAirtableService.selectOneFiltered(table, "{Anlage} = '"+anlageName+"'");
        return ACSettings;
    }


    public Anlage loadAnlageFromAirtable(String anlageName) {
        Table table = mAirtableService.getTableByName(Anlage.tableName);
        AnlageObj a = (AnlageObj) mAirtableService.selectOneFiltered(table, "{Name} = '"+anlageName+"'");
        if (a == null) {
            Log.d(TAG, "anlage "+anlageName+ "kann nicht von Airtable geladen werden");
            return null;
        }


        Anlage anlage = new Anlage(a);

        List<Ort> orte = mAirtableService.selectFiltered(mAirtableService.getOrtTable(), "{Anlage} = '"+anlageName+"'");
        anlage.setOrte(orte);
        if (anlage.getStartOrtIds() != null) {
            anlage.setHome(anlage.getOrtById(anlage.getStartOrtId()));
        }

        List<Weg> wege = mAirtableService.selectFiltered(mAirtableService.getWegTable(), "{Anlage} = '"+anlageName+"'");
        anlage.setWege(wege);
        for (Weg weg : wege) {
            weg.setVonOrt(anlage.getOrtById(weg.getVonOrtId()));
            weg.setNachOrt(anlage.getOrtById(weg.getNachOrtId()));
            weg.setNextWeg(anlage.getWegById(weg.getNextWegId()));
        }

        List<Aktion> aktionen = mAirtableService.selectFiltered(mAirtableService.getAktionTable(), "{Anlage} = '"+anlageName+"'");
        anlage.setAktionen(aktionen);
        for (Aktion aktion : aktionen) {
            aktion.setWeg(anlage.getWegById(aktion.getWegId()));
            aktion.setOrt(anlage.getOrtById(aktion.getOrtId()));
            aktion.setNachOrt(anlage.getOrtById(aktion.getNachOrtId()));
            aktion.setFolgeAktion(anlage.getAktionById(aktion.getFolgeAktionId()));
        }

        return anlage;
    }
    /*
    Schreibt eine Anlage mit ihren Orten, Wegen und Aktionen in die Airtable Datenbank
     * TODO: wenn Namen geändert wurden, werden die alten Einträge nicht gelöscht, es entstehen Leichen
     * Problem tritt auf, wenn änderungen im Code genacht werden, und neue Soundfile geladen werden
     */
    public void writeAnlageToAirtable(Anlage anlage) {
        if (anlage == null) {
            Log.d(TAG, "Anlage "+anlage+" existiert nicht");
            return;
        }

        Log.d(TAG,"die Anlage "+anlage.getName()+" auf Airtable schreiben");
        AnlageObj a = new AnlageObj(anlage);
        //a = (AnlageObj) mAirtableService.write(mAirtableService.getAnlageTable(), a);
        a = (AnlageObj) insertUpdateAirtableObject(a,"{Name} = '" + anlage.getName() + "'" );

        //AirtableCall request =
        // jetzt brauche ich die Referenz zur Anlage oder reicht der Name?
        for (Ort ort : anlage.getOrte()) {
            Log.d(TAG,"Ort an die Anlage hängen");
            ort.setAnlageId(a.getId());
            Ort no = (Ort) insertUpdateAirtableObject(ort,"AND({Name} = '" + ort.getName() + "', {Anlage} ='"  + a.getName() + "')");
//
            /*

            ort.setId(null);
            Ort no = (Ort) mAirtableService.write(mAirtableService.getOrtTable(), ort);
            if (no != null) {
                ort.setId(no.getId());
            }
            */
        }
        for (Weg weg : anlage.getWege()) {
            Log.d(TAG,"Weg an die Anlage hängen");

            weg.setAnlageId(a.getId());
            //weg.setId(null);
            Ort vonOrt = weg.getVonOrt();
            if (vonOrt != null) {
                weg.setVonOrtId(vonOrt.getId());
            }
            Ort nachOrt = weg.getNachOrt();
            if (nachOrt != null) {
                weg.setNachOrtId(nachOrt.getId());
            }
            Weg nextWeg = weg.getNextWeg();
            if (nextWeg != null) {
                weg.setNextWegId(nextWeg.getId());
            }
            Weg wegNeu = (Weg) insertUpdateAirtableObject(weg,"AND({Name} = '" + weg.getName() + "', {Anlage} ='"  + a.getName() + "')" );
            /*
            // weg id werden als String Array gesetzt, parallel zum Objekt Array
            Weg wegNeu = (Weg) mAirtableService.write(mAirtableService.getWegTable(), weg);
            if (wegNeu != null) {
                weg.setId(wegNeu.getId());
            }
            */
        }
        for (Aktion aktion : anlage.getAktionen()) {
            Log.d(TAG, "Aktionen an die Anlage hängen");
            aktion.setAnlageId(a.getId());
            Ort nachOrt = aktion.getNachOrt();
            if (nachOrt != null) {
                aktion.setNachOrtId(nachOrt.getId());
            }
            Weg weg = aktion.getWeg();
            if (weg != null) {
                aktion.setWegId(weg.getId());
            }
            Ort ort = aktion.getOrt();
            if (ort != null) {
                aktion.setOrtId(ort.getId());
            }
            Aktion folgeAktion = aktion.getFolgeAktion();
            if (folgeAktion != null) {
                aktion.setFolgeAktionId(folgeAktion.getId());
            }

            Aktion aktionNeu = (Aktion) insertUpdateAirtableObject(aktion,"AND({Name} = '" + aktion.getName() + "', {Anlage} ='"  + a.getName() + "')");
        }
        Ort home = anlage.getHome();
        if (home != null) {
            a.setStartOrtId(home.getId());
            Table table = mAirtableService.getTableByName(a.getTableName());
            // TODO update funktioniert nicht
            mAirtableService.update(table, a);
        }
    }



    public AirtableObject loadAirtableObject(AirtableObject airtableObject, String filter, String[] fields) {
        Table table = mAirtableService.getTableByClass(airtableObject.getClass());
        AirtableObject resultObjekt = null;
        //if (mAirtableService.isInternetAvailable(MainActivity.getContext())) {
        if (mAirtableService.isInternetAvailable2()) {
            List<Object> result =  mAirtableService.selectNameList(table, filter, fields);
            if (!result.isEmpty()) {
                resultObjekt = (AirtableObject) result.get(0);
            }
        }
        return resultObjekt;

    }

    /*
     * schreibt den Datensatz für dieses Device auf airlog, falls der Record noch nicht existiert wird er erzeugt
     * "{Player} = '" + deviceName + "'"
     * Das ResultObject enthält keine Ids und ist ein neues Objekt
     * TODO: wir möchten wissen, ob das Schreiben erfolgreich war ist nicht
     * TODO Timeout einbauen
     */
    public AirtableObject insertUpdateAirtableObject(AirtableObject airtableObject, String filter) {
        Table table = mAirtableService.getTableByClass(airtableObject.getClass());
        AirtableObject resultObjekt = null;
        //if (mAirtableService.isInternetAvailable(MainActivity.getContext())) {
        if (mAirtableService.isInternetAvailable2()) {
            // wenn die id leer ist, haben wir noch nie geschrieben seit wir laufen
            if (airtableObject.getId() == null) {
                //mAirLogs = new ArrayList<String>();
                // testen ob es den Record schon gibt // "{Name} = 'Bad Ragaz'" TODO geht sehr lange
                List msgs = mAirtableService.selectFiltered(table, filter);
                if (msgs == null) {
                    android.util.Log.d(TAG, "wir haben keine Airtable Connection");
                    resultObjekt = (AirtableObject) mAirtableService.write(table, airtableObject);
                    if (resultObjekt != null) {
                        airtableObject.setId(resultObjekt.getId());
                    }
                } else if (msgs.size() == 0) {
                    // Datensatz schreiben, falls er noch nicht existiert
                    // Message in Record schreiben
                    resultObjekt = (AirtableObject) mAirtableService.write(table, airtableObject);
                    if (resultObjekt != null) {
                        airtableObject.setId(resultObjekt.getId());
                    }
                } else {
                    airtableObject.setId(((AirtableObject) msgs.get(0)).getId());
                    Log.d(TAG, "Update start");
                    resultObjekt = (AirtableObject) mAirtableService.update(table, airtableObject);
                    //airtableObject.setId(((AirtableObject) msgs.get(0)).getId());
                    Log.d(TAG, "Update End");
                }
            } else {
                resultObjekt = (AirtableObject) mAirtableService.update(table, airtableObject);
                if (resultObjekt != null) {
                    airtableObject.setId(((AirtableObject) resultObjekt).getId());
                } else {
                    Log.d(TAG,"ResultObject von Update ist null");
                }
            }
        }
        return airtableObject;
    }

    public AirtableObject selectOneFiltered(Table table, String filter) {
        List msgs = mAirtableService.selectFiltered(table, filter);
        if (msgs == null) {
            android.util.Log.d(TAG, "wir haben keine Airtable Connection");
        } else if (msgs.size() == 0) {
            android.util.Log.d(TAG, "wir haben kein Objekt auf " + table + " gefunden mit filter:" + filter);
        } else {
            return (AirtableObject) msgs.get(0);
        }
        return null;
    }

    public List selectAll(Table table) {
        // resultat ist nie null, kann einfach leer sein
        // anlageliste = new ArrayList<Anlage>();
        List<Object> anlageliste = (List<Object>) runThread((new AirtableCall(table, AirtableCall.SELECT_ALL)));
        return anlageliste;
    }

    public List selectOne(Table table, String recordId) {
        List<Object> anlageliste = (List<Object>) runThread((new AirtableCall(table, AirtableCall.SELECT_ID, recordId)));
        System.out.println(anlageliste);
        return anlageliste;
    }

    public List exists(Table table, String filter) {
        List<Object> anlageliste = (List<Object>) runThread((new AirtableCall(table, AirtableCall.EXITS, filter)));
        System.out.println(anlageliste);
        return anlageliste;
    }

    // "{Name} = 'Bad Ragaz'"
    public List selectFiltered(Table table, String filter) {
        List<Object> anlageliste = (List<Object>) runThread((new AirtableCall(table, AirtableCall.SELECT_FILTER, filter)));
        System.out.println(anlageliste);
        return anlageliste;
    }
    // Filter zB "{Name} = 'Wangs-1-2020-07-03'"
    public List selectNameList(Table table, String filter, String[] fields) {
        List<Object> result = (List<Object>) runThread((new AirtableCall(table, AirtableCall.NAMELIST, filter, fields)));
        System.out.println(result);
        return result;
    }

    public Object write(Table table, Object obj) {
        AirtableCall request = new AirtableCall(table, AirtableCall.CREATE_NEW, obj);
        runThread(request);
        System.out.println(request);
        return request.getOutputObject();
    }

    public Object update(Table table, Object obj) {
        AirtableCall request = new AirtableCall(table, AirtableCall.UPDATE, obj);
        runThread(request);
        System.out.println(request);
        return request.getOutputObject();
    }





    public List runThread(AirtableCall request) {
        class RunnableObject implements Runnable {

            private AirtableCall request;

            public RunnableObject(AirtableCall request) {
                this.request = request;
            }

            public void setRequest(AirtableCall request) {
                this.request = request;
            }


            public void run() {
                try {
                    switch(request.action){
                        case 99:

                            break;
                        case AirtableCall.CREATE_NEW:

                            try {
                                Object o = request.getTable().create(request.getInputObject());
                                request.setResult(o);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            break;
                        case AirtableCall.UPDATE:

                            try {
                                // id und createdDate werden im InputObjekt auf null gesetzt
                                request.setResult(request.getTable().update(request.getInputObject()));
                                String id = ((AirtableObject)request.getOutputObject ()).getId();
                                ((AirtableObject)request.getInputObject()).setId(id);
                                Log.d(TAG, "Update von:"+request.getInputObject()+" ist durch");
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            } catch (InvocationTargetException e) {
                                e.printStackTrace();
                            } catch (NoSuchMethodException e) {
                                e.printStackTrace();
                            }
                            break;

                        case AirtableCall.SELECT_ALL:
                            request.setResult(request.getTable().select());
                            break;
                        case AirtableCall.DELETE:
                            request.getTable().destroy(request.getId());
                            break;

                        case AirtableCall.SELECT_ID:
                            /*
                            Base base = new Airtable().base("AIRTABLE_BASE");
                            Table<Actor> actorTable = base.table("Actors", Actor.class);
                            Actor actor = actorTable.find("rec514228ed76ced1");

                             */
                            System.out.println(request);
                            request.setResult(request.getTable().find(request.getId()));
                            break;
                        case AirtableCall.NAMELIST:
                            Query queryN = new Query() {
                                @Override
                                public String[] getFields() {
                                    //String[] field = {"Name"};
                                    return request.getFields();
                                }
                                @Override
                                public Integer getPageSize() {
                                    return null;
                                }

                                @Override
                                public Integer getMaxRecords() {
                                    return null;
                                }

                                @Override
                                public String getView() {
                                    return null;
                                }

                                @Override
                                public List<Sort> getSort() {
                                    return null;
                                }

                                @Override
                                public String filterByFormula() {
                                    return request.getId();
                                }


                            };
                            try {
                                List rows = request.getTable().select(queryN);
                                request.setResult(rows);
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                            break;
                        case AirtableCall.EXITS:
                            Query query = new Query() {
                                @Override
                                public String[] getFields() {
                                    String[] field = {"Name"};
                                    return field;
                                }

                                @Override
                                public Integer getPageSize() {
                                    return null;
                                }

                                @Override
                                public Integer getMaxRecords() {
                                    return null;
                                }

                                @Override
                                public String getView() {
                                    return null;
                                }

                                @Override
                                public List<Sort> getSort() {
                                    return null;
                                }

                                @Override
                                public String filterByFormula() {
                                    return request.getId();
                                }


                            };
                            try {
                                List rows = request.getTable().select(query);
                                request.setResult(rows);
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage());
                            }
                            break;
                        case AirtableCall.SELECT_FILTER:
                            query = new Query() {
                                @Override
                                public String[] getFields() {
                                    return null;
                                }

                                @Override
                                public Integer getPageSize() {
                                    return null;
                                }

                                @Override
                                public Integer getMaxRecords() {
                                    return null;
                                }

                                @Override
                                public String getView() {
                                    return null;
                                }

                                @Override
                                public List<Sort> getSort() {
                                    return null;
                                }

                                @Override
                                public String filterByFormula() {
                                    return request.getId();
                                }


                            };
                            try {
                                List rows = request.getTable().select(query);
                                request.setResult(rows);
                            } catch (Exception e) {
                                Log.d(TAG, e.getMessage())
;                            }
                            break;


                    }

                } catch (AirtableException e) {
                    request.setAirtableException(e);
                    e.printStackTrace();
                }
            }
            public AirtableCall getResponse() {
                return request;
            }
        }

        Runnable runner;
        runner = new RunnableObject(request);
        Thread myThread = new Thread(runner);
        myThread.start();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        request = ((RunnableObject) runner).getResponse();
        System.out.println(request);
        return request.result;

    }

    //TODO: wie lange geht es ohne connection?
    public boolean isInternetAvailable(Context context) {
        try {
            ConnectivityManager connectivityManager
                    = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                String type = activeNetworkInfo.getTypeName();
                int networktype = activeNetworkInfo.getType();
                // ConnectivityManager.TYPE_WIFI
                if (type.equalsIgnoreCase("WIFI")) {
                    return true;
                }
                return false;
            }
        } catch (Exception e) {
            Log.e("isInternetAvailable:",e.toString());
            return false;
        }
        return false;
    }


    public boolean isInternetAvailable2() {
        boolean isavailable = isNetworkAvailable();
        return isavailable;
                // isInternetAvailable(MainActivity.getContext());
        //return isNetworkAvailable(MainActivity.getContext());
        /*
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() {
                return isNetworkAvailable(MainActivity.getContext());
                //return isInternetAvailable(MainActivity.getContext());
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            //Give the task 5 seconds to complete
            //if not it raises a timeout exception
            int timeout = ACSettings.getInstance().getInternetTimeout();
            Object result = future.get(timeout, TimeUnit.SECONDS);
            //finished in time
            return (boolean) result;
        } catch (
                TimeoutException | InterruptedException | ExecutionException ex) {
            //Didn't finish in time
            return false;
        }
        */
    }

    public static boolean isNetworkAvailable (Context context) {

        try {
            HttpURLConnection urlc = (HttpURLConnection)
                    (new URL("http://clients3.google.com/generate_204")
                            .openConnection());
            urlc.setRequestProperty("User-Agent", "Android");
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(2500);
            urlc.connect();
            return (urlc.getResponseCode() == 204 &&
                    urlc.getContentLength() == 0);
        } catch (IOException e) {
            Log.e(TAG, "Error checking internet connection", e);
        }
        return false;
    }

    class IsInternetActive extends AsyncTask<Void, Void, String> {
        InputStream is = null;
        String json = "Fail";

        @Override
        protected String doInBackground(Void... params) {
            try {
                URL strUrl = new URL("http://icons.iconarchive.com/icons/designbolts/handstitch-social/24/Android-icon.png");
                //Here I have taken one android small icon from server, you can put your own icon on server and access your URL, otherwise icon may removed from another server.

                URLConnection connection = strUrl.openConnection();
                connection.setDoOutput(true);
                is = connection.getInputStream();
                json = "Success";

            } catch (Exception e) {
                e.printStackTrace();
                json = "Fail";
            }
            return json;

        }
    }

    /*
     * bis jetzt die einzige Funktion, die auch funktioniert
     */
    public static boolean isNetworkAvailable () {
        Runtime runtime = Runtime.getRuntime();
        try {

            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);

        } catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return false;
    }

}
