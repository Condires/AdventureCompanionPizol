package com.condires.adventure.companion;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.condires.adventure.companion.model.Anlage;

import java.util.ArrayList;
import java.util.List;

public class AnlageListActivity extends AppCompatActivity {

        private ListView listView;

        private static final int MENU_ITEM_VIEW = 111;
        private static final int MENU_ITEM_SETTINGS = 222;
        private static final int MENU_ITEM_CREATE = 333;
        private static final int MENU_ITEM_DELETE = 444;


        private static final int MY_REQUEST_CODE = 1000;

        private final List<Anlage> anlageList = new ArrayList<Anlage>();
        private ArrayAdapter<Anlage> listViewAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_anlage_list);


            // Get ListView object from xml
            listView = (ListView) findViewById(R.id.listView);

            DatabaseHelper db = new DatabaseHelper(this);
            // TODO: das ist nicht nötig später
            //db.createDefaultNotesIfNeed();

            List<Anlage> list=  db.getAllAnlageNamen();
            this.anlageList.addAll(list);



            // Define a new Adapter
            // 1 - Context
            // 2 - Layout for the row
            // 3 - ID of the TextView to which the data is written
            // 4 - the List of data

            this.listViewAdapter = new ArrayAdapter<Anlage>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, this.anlageList);


            // Assign adapter to ListView
            this.listView.setAdapter(this.listViewAdapter);

            // Register the ListView for Context menu
            registerForContextMenu(this.listView);
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View view,
                                        ContextMenu.ContextMenuInfo menuInfo)    {

            super.onCreateContextMenu(menu, view, menuInfo);
            menu.setHeaderTitle("Select The Action");

            // groupId, itemId, order, title
            menu.add(0, MENU_ITEM_VIEW , 0, "View Anlage");
            menu.add(0, MENU_ITEM_CREATE , 1, "Load from Airtable");
            menu.add(0, MENU_ITEM_SETTINGS , 2, "Load ACSettings");
            menu.add(0, MENU_ITEM_DELETE, 4, "Delete Anlage");
        }

        @Override
        public boolean onContextItemSelected(MenuItem item){
            AdapterView.AdapterContextMenuInfo
                    info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            final Anlage selectedAnlage = (Anlage) this.listView.getItemAtPosition(info.position);

            if(item.getItemId() == MENU_ITEM_VIEW){


                // Load from Airtable
                Intent intent = new Intent(this, AnlageBrowseActivity.class);
                //Start AddEditAnlageActivity, (with feedback).
                this.startActivity(intent);
            }
            else if(item.getItemId() == MENU_ITEM_CREATE){
                // Load from Airtable
                //Intent intent = new Intent(this, AddEditAnlageActivity.class);
                // Start AddEditAnlageActivity, (with feedback).
                //this.startActivityForResult(intent, MY_REQUEST_CODE);

                Data.getInstance().loadAnlageFromAirtable(selectedAnlage.getName());
                Data.getInstance().saveAnlageToDb(selectedAnlage.getName());
                Toast.makeText(getApplicationContext(), "Anlage loaded from Airtable",Toast.LENGTH_LONG).show();

            }
            else if(item.getItemId() == MENU_ITEM_SETTINGS ){
                //Intent intent = new Intent(this, AddEditAnlageActivity.class);
                //intent.putExtra("mAnlage", selectedAnlage);
                // Start AddEditAnlageActivity, (with feedback).
                //this.startActivityForResult(intent,MY_REQUEST_CODE);
                Data.getInstance().loadSettingFromAirtable(selectedAnlage.getName());
                Toast.makeText(getApplicationContext(), "ACSettings loaded from Airtable",Toast.LENGTH_LONG).show();
            }
            else if(item.getItemId() == MENU_ITEM_DELETE){
                // Ask before deleting.
                new AlertDialog.Builder(this)
                        .setMessage(selectedAnlage.getName()+". Are you sure you want to delete?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                deleteNote(selectedAnlage);
                            }
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
            else {
                return false;
            }
            return true;
        }

        // Delete a record
        private void deleteNote(Anlage anlage)  {
            DatabaseHelper db = new DatabaseHelper(this);
            db.deleteAnlage(anlage);
            this.anlageList.remove(anlage);
            // Refresh ListView.
            this.listViewAdapter.notifyDataSetChanged();
        }

        // When AddEditAnlageActivity completed, it sends feedback.
        // (If you start it using startActivityForResult ())
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == Activity.RESULT_OK && requestCode == MY_REQUEST_CODE ) {
                boolean needRefresh = data.getBooleanExtra("needRefresh",true);
                // Refresh ListView
                if(needRefresh) {
                    this.anlageList.clear();
                    DatabaseHelper db = new DatabaseHelper(this);
                    List<Anlage> list=  db.getAllAnlagen();
                    this.anlageList.addAll(list);


                    // Notify the data change (To refresh the ListView).
                    this.listViewAdapter.notifyDataSetChanged();
                }
            }
        }

    }
