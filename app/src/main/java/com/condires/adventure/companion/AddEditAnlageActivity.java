package com.condires.adventure.companion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.condires.adventure.companion.model.Anlage;


public class AddEditAnlageActivity extends AppCompatActivity {

    Anlage anlage;
    private static final int MODE_CREATE = 1;
    private static final int MODE_EDIT = 2;

    private int mode;
    private EditText textTitle;
    private EditText textContent;

    private boolean needRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_anlage);

        this.textTitle = (EditText)this.findViewById(R.id.text_note_title);
        this.textContent = (EditText)this.findViewById(R.id.text_note_content);

        Intent intent = this.getIntent();
        this.anlage = (Anlage) intent.getSerializableExtra("mAnlage");
        if(anlage == null)  {
            this.mode = MODE_CREATE;
        } else  {
            this.mode = MODE_EDIT;
            this.textTitle.setText(anlage.getName());
            this.textContent.setText(anlage.getAsJson());
        }


    }

    // User Click on the Save button.
    public void buttonSaveClicked(View view)  {
        DatabaseHelper db = new DatabaseHelper(this);

        String title = this.textTitle.getText().toString();
        String content = this.textContent.getText().toString();

        if(title.equals("") || content.equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Please enter title & content", Toast.LENGTH_LONG).show();
            return;
        }

        if(mode==MODE_CREATE ) {
            this.anlage = new Anlage();
            anlage.setName(title);
            anlage.setFromJson(content);
            db.addAnlage(anlage);
        } else  {
            this.anlage.setName(title);
            this.anlage.setFromJson(content);
            db.updateAnlage(anlage);
        }

        this.needRefresh = true;

        // Back to AirtableEditActivity.
        this.onBackPressed();
    }

    // User Click on the Cancel button.
    public void buttonCancelClicked(View view)  {
        // Do nothing, back AirtableEditActivity.
        this.onBackPressed();
    }

    // When completed this Activity,
    // Send feedback to the Activity called it.
    @Override
    public void finish() {

        // Create Intent
        Intent data = new Intent();

        // Request AirtableEditActivity refresh its ListView (or not).
        data.putExtra("needRefresh", needRefresh);

        // Set Result
        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }

}