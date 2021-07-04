package com.condires.adventure.companion;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.condires.adventure.companion.model.Anlage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;



public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLite";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "AdvCompanion.db";

    // Table name: Anlage.
    private static final String TABLE_ANLAGE = "Anlage";

    private static final String COLUMN_ANLAGE_ID ="Anlage_Id";
    private static final String COLUMN_ANLAGE_NAME ="Anlage_Name";
    private static final String COLUMN_ANLAGE_CONTENT = "Anlage_Content";

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").create();

    public DatabaseHelper(Context context)  {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Create table
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, "DatabaseHelper.onCreate ... ");
        // Script.
        String script = "CREATE TABLE " + TABLE_ANLAGE + "("
                + COLUMN_ANLAGE_ID + " STRING PRIMARY KEY," + COLUMN_ANLAGE_NAME + " TEXT,"
                + COLUMN_ANLAGE_CONTENT + " TEXT" + ")";
        // Execute Script.
        db.execSQL(script);
    }

    private void createDB(SQLiteDatabase db) {
        String script = "CREATE TABLE " + TABLE_ANLAGE + "("
                + COLUMN_ANLAGE_ID + " STRING PRIMARY KEY," + COLUMN_ANLAGE_NAME + " TEXT,"
                + COLUMN_ANLAGE_CONTENT + " TEXT" + ")";
        // Execute Script.
        db.execSQL(script);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "DatabaseHelper.onUpgrade ... ");
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ANLAGE);

        // Create tables again
        onCreate(db);
    }


    // If Anlage table has no data
    // default, Insert 2 records.
    /*
    public void createDefaultNotesIfNeed()  {
        int count = this.getAnlageCount();
        if(count ==0 ) {
            Anlage anlage1 = new Anlage("Beispiel Anlage 1",
                    "0");
            Anlage anlage2 = new Anlage("Beispiel Anlöage 2",
                    "9999999");
            this.addAnlage(anlage1);
            this.addAnlage(anlage2);
        }
    }
    */


    public void addAnlage(Anlage anlage) {
        Log.i(TAG, "DatabaseHelper.addAnlage ... " + anlage.getName());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ANLAGE_ID, anlage.getDbId());
        values.put(COLUMN_ANLAGE_NAME, anlage.getName());
        values.put(COLUMN_ANLAGE_CONTENT, gson.toJson(anlage));

        // Inserting Row
        db.insert(TABLE_ANLAGE, null, values);

        // Closing database connection
        db.close();
    }

    public boolean existsAnlage(String dbId) {
        Log.i(TAG, "DatabaseHelper.ExistsAnlage ... " + dbId);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ANLAGE, new String[]{COLUMN_ANLAGE_ID,
                        COLUMN_ANLAGE_NAME, COLUMN_ANLAGE_CONTENT}, COLUMN_ANLAGE_ID + "=?",
                new String[]{dbId}, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {
                return true;
            }
        }
        return false;

    }

        public Anlage getAnlage(String dbId) {
        Log.i(TAG, "DatabaseHelper.getNote ... " + dbId);

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_ANLAGE, new String[] {COLUMN_ANLAGE_ID,
                        COLUMN_ANLAGE_NAME, COLUMN_ANLAGE_CONTENT}, COLUMN_ANLAGE_ID + "=?",
                new String[] { String.valueOf(dbId) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Anlage anlage = null;
        if (cursor.getCount() > 0) {
            String name = cursor.getString(1);
            String js = cursor.getString(2);

            if (js != null) {
                try {
                    anlage = gson.fromJson(js, Anlage.class);
                    anlage.rebuildReferences();
                } catch (Exception e) {
                    Log.d(TAG, "Die Anlage "+name+" kann nicht aus dem json gelesen werden");
                }
            }
        }

        /*
        Anlage mAnlage = new Anlage();
        mAnlage.setDbId(cursor.getString(0));
        mAnlage.setName(cursor.getString(1));
        mAnlage.setFromJson(cursor.getString(2));
        */
        // return mAnlage

        return anlage;
    }

    public List<Anlage> getAllAnlageNamen() {
        Log.i(TAG, "DatabaseHelper.getAlleAnlageNamen ... " );

        List<Anlage> anlageList = new ArrayList<Anlage>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ANLAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Anlage anlage = new Anlage();
                anlage.setDbId(cursor.getString(0));
                anlage.setName(cursor.getString(1));
                anlageList.add(anlage);
            } while (cursor.moveToNext());
        }
        return anlageList;
    }

    public List<Anlage> getAllAnlagen() {
        Log.i(TAG, "DatabaseHelper.getAll Anlagen ... " );

        List<Anlage> anlageList = new ArrayList<Anlage>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_ANLAGE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);


        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {

                String js = cursor.getString(2);
                Anlage anlage = null;
                if (js != null) {
                    anlage = gson.fromJson(js, Anlage.class);
                    anlage.rebuildReferences();
                }
                /*
                Anlage mAnlage = new Anlage();
                mAnlage.setDbId(cursor.getString(0));
                mAnlage.setName(cursor.getString(1));
                mAnlage.setFromJson(cursor.getString(2));
                */
                // Adding mAnlage to list
                anlageList.add(anlage);
            } while (cursor.moveToNext());
        }

        // return mAnlage list
        return anlageList;
    }

    public int getAnlageCount() {
        Log.i(TAG, "DatabaseHelper.get Anlage Count ... " );

        String countQuery = "SELECT  * FROM " + TABLE_ANLAGE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();

        cursor.close();

        // return count
        return count;
    }


    public int updateAnlage(Anlage anlage) {
        Log.i(TAG, "DatabaseHelper.update Anlage ... "  + anlage.getName());

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ANLAGE_NAME, anlage.getName());
        String js = gson.toJson(anlage);
        values.put(COLUMN_ANLAGE_CONTENT,js);

        // updating row
        return db.update(TABLE_ANLAGE, values, COLUMN_ANLAGE_ID + " = ?",
                new String[]{String.valueOf(anlage.getDbId())});
    }

    public void deleteAnlage(String name) {
        Log.i(TAG, "DatabaseHelper.delete Anlage ... " + name );

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ANLAGE, COLUMN_ANLAGE_NAME + " = ?",
                new String[] { name });
        db.close();
    }

    public void deleteAnlage(Anlage anlage) {
        Log.i(TAG, "DatabaseHelper.update Anlage ... " + anlage.getName() );

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ANLAGE, COLUMN_ANLAGE_ID + " = ?",
                new String[] { String.valueOf(anlage.getDbId()) });
        db.close();
    }

}