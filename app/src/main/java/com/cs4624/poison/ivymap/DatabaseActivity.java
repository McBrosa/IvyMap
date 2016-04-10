package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class DatabaseActivity extends Activity {
    private TextView records;
    private Button button_delete;
    private DatabaseHandler database;
    //private InputMethodManager inputManager;


    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Username = "usernameKey";
    public static final String Password = "passwordKey";
    public static final String Team = "teamKey";
    SharedPreferences sharedpreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        button_delete = (Button) findViewById(R.id.button_delete);
        records = (TextView) findViewById(R.id.records);
        records.setMovementMethod(new ScrollingMovementMethod());

        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

        database = new DatabaseHandler(getApplicationContext());
        //showDatabase(database);

        //inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Confirmation box for deleting the most recent record
                new AlertDialog.Builder(view.getContext())
                        .setTitle("Confirm")
                        .setMessage("Do you really want to do perform this action?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Delete the last record and display message confirmation
                                database.deleteMostRecentPI();
                                showDatabase(database);
                                Toast.makeText(getApplicationContext(), "Most Recent Record was Deleted", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        }).show();
                // Show the updated database after a delete
            }
        });
    }

    private void showDatabase(DatabaseHandler db)
    {
        records.setText("");
        records.append(db.getTableAsString());
    }

    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Toast.makeText(DatabaseActivity.this, buttonView.getText(), Toast.LENGTH_SHORT).show();
            }
            switch (buttonView.getId()) {
                case R.id.btnHome:
                    Intent homeIntent = new Intent(DatabaseActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    break;

                case R.id.btnSettings:
                    Intent settingsIntent = new Intent(DatabaseActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.btnDatabase:
//                    Intent tableIntent = new Intent(DatabaseActivity.this, DatabaseActivity.class);
//                    startActivity(tableIntent);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
    }

    private String getUsername()
    {
        return sharedpreferences.getString(Username, "");
    }

    private String getPassword()
    {
        return sharedpreferences.getString(Password, "");
    }

    private String getTeam()
    {
        return sharedpreferences.getString(Team, "");
    }
}