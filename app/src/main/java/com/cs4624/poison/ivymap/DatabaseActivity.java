package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.TableRow.LayoutParams;
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
    private TableLayout table_layout;
    private ProgressDialog PD;

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
        table_layout = (TableLayout) findViewById(R.id.tableLayout1);

        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

        database = new DatabaseHandler(getApplicationContext());
        BuildTable();

        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Confirmation box for deleting the most recent record
                if(database.getPICount() == 0)
                {
                    database.onUpgrade(database.getWritableDatabase(), 1, 1);
                    Toast.makeText(getApplicationContext(), "Database Reset", Toast.LENGTH_SHORT).show();
                }
                else {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Confirm")
                            .setMessage("Do you really want to do perform this action?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Delete the last record and display message confirmation
                                    database.deleteMostRecentPI();
                                    // Updates the table upon a delete
                                    new MyAsync().execute();
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
                }
            }
        });
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
                    break;
            }
        }
    };

    private void BuildTable() {
        Cursor c = database.readEntry();

        int rows = c.getCount();
        int cols = c.getColumnCount();

        TableRow header = new TableRow(this);
        header.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT));
        // outer for loop
        for (int i = rows - 1; i >= 0; i--) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT));

            // inner for loop
            for (int j = 0; j < cols; j++) {
                TextView tv = new TextView(this);
                if (i % 2 != 0) {
                    tv.setBackgroundResource(R.drawable.row_color);
                } else {
                    tv.setBackgroundResource(R.drawable.alt_row_color);
                }
                tv.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT));
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(16);

                tv.setText(c.getString(j));
                row.addView(tv);
            }
            c.moveToPrevious();
            table_layout.addView(row);
        }
        database.close();
    }

    private class MyAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();

            table_layout.removeAllViews();

            PD = new ProgressDialog(DatabaseActivity.this);
            PD.setTitle("Please Wait..");
            PD.setMessage("Loading...");
            PD.setCancelable(false);
            PD.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            BuildTable();
            PD.dismiss();
        }
    }

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