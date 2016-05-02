/**
 * The settings page that gets the current inputted text and saves it to the shared preferences.
 *
 * @author Nathan Rosa
 * @date 4/28/2016
 */

package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class SettingsActivity extends Activity {
    // EditText fields
    private EditText username, password, team;
    // Update button
    private Button update;
    private DatabaseHandler dbHandler;
    // The shared preferences to update
    private AppPreferences sharedpreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Initialize the shared preferences
        sharedpreferences = new AppPreferences(getApplicationContext());
        // Initialize the database handler
        dbHandler = new DatabaseHandler(getApplicationContext());

        // Set up the input text fields and provide hints
        team = (EditText) findViewById(R.id.team);
        team.setHint("Team");
        username = (EditText) findViewById(R.id.username);
        username.setHint("Database Username");
        password = (EditText) findViewById(R.id.password);
        password.setHint("Database Password");
        update = (Button) findViewById(R.id.update);

        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

        // Check to make sure the fields are not empty before trying to populate
        if(sharedpreferences.getTeam() != "")
        {
            team.setText(sharedpreferences.getTeam());
        }
        if(sharedpreferences.getUsername() != "")
        {
            username.setText(sharedpreferences.getUsername());
        }
        if(sharedpreferences.getPassword() != "")
        {
            password.setText(sharedpreferences.getPassword());
        }

        // On click update the preferences
        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                sharedpreferences.saveUsername(username.getText().toString());
                sharedpreferences.savePassword(password.getText().toString());
                sharedpreferences.saveTeam(team.getText().toString());
//                copyDBtoSD();
                // export DB as CSV file
//                exportDBtoCSV();
                Toast.makeText(SettingsActivity.this, "Settings Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    // This functionality is not fully operational but did export on a rooted phone
    private void exportDB(){
        File sd = getExternalFilesDir(null);
//        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        //File data = dbHandler.getReadableDatabase().getPath();
        FileChannel source=null;
        FileChannel destination=null;
//        String currentDBPath = "/data/" + "com.cs4624.poison.ivymap" + "/databases/"+dbHandler.getDatabaseName();
        File currentDB = getDatabasePath(dbHandler.getDatabaseName());
        String backupDBPath = dbHandler.getDatabaseName();
        //File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            try {
                source = new FileInputStream(currentDB).getChannel();
                destination = new FileOutputStream(backupDB).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.close();
                Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                Log.w("ExternalStorage", "Error writing " + data, e);
            }
        }
        else
        {
            Toast.makeText(this, "No SD Card Detected!", Toast.LENGTH_LONG).show();
        }
    }

    private void exportDBtoCSV() {
        File dbFile = getDatabasePath(dbHandler.getDatabaseName());
        File exportDir = new File(getExternalFilesDir(null), "");
        if (!exportDir.exists())
        {
            exportDir.mkdirs();
        }

        File file = new File(exportDir, "poison_ivy.csv");
        try
        {
            file.createNewFile();
            CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
            SQLiteDatabase db = dbHandler.getReadableDatabase();
            Cursor curCSV = db.rawQuery("SELECT * FROM poison_ivy" , null);
            csvWrite.writeNext(curCSV.getColumnNames());
            while(curCSV.moveToNext())
            {
                //Which column you want to export
                String arrStr[] ={curCSV.getString(0),curCSV.getString(1), curCSV.getString(2), curCSV.getString(3), curCSV.getString(4), curCSV.getString(5), curCSV.getString(6), curCSV.getString(7)};
                csvWrite.writeNext(arrStr);
            }
            csvWrite.close();
            curCSV.close();
            Toast.makeText(this, "Export", Toast.LENGTH_LONG).show();

        }
        catch(Exception sqlEx)
        {
            Toast.makeText(this, "No SD Card Detected!", Toast.LENGTH_LONG).show();
        }
    }


    private boolean copyDBtoSD(){
        File dbFile =
                new File(Environment.getDataDirectory() + "/data/com.cs4624.poison.ivymap/databases/"+dbHandler.getDatabaseName());

        File exportDir = new File(getExternalFilesDir(null), "");
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
        File file = new File(exportDir, dbFile.getName());

        try {
            file.createNewFile();
            this.copyFile(dbFile, file);
            return true;
        } catch (IOException e) {
            Log.e("poison_ivy", e.getMessage(), e);
            return false;
        }
    }

    void copyFile(File src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    /**
     * Listener for the nav bar that will change screens on click
     */
    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.btnHome:
                    Intent homeIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    break;

                case R.id.btnSettings:
                    break;

                case R.id.btnDatabase:
                    Intent tableIntent = new Intent(SettingsActivity.this, DatabaseActivity.class);
                    startActivity(tableIntent);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
    }
}
