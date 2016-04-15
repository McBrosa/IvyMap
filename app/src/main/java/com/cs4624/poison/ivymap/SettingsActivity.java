package com.cs4624.poison.ivymap;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;


public class SettingsActivity extends Activity {
    private EditText username, password, team;
    private Button update;
    private DatabaseHandler dbHandler;
    private AppPreferences sharedpreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedpreferences = new AppPreferences(getApplicationContext());
        dbHandler = new DatabaseHandler(getApplicationContext());

        team = (EditText) findViewById(R.id.team);
        team.setHint("Team");
        username = (EditText) findViewById(R.id.username);
        username.setHint("Username");
        password = (EditText) findViewById(R.id.password);
        password.setHint("Password");
        update = (Button) findViewById(R.id.update);

        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

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

        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                sharedpreferences.saveUsername(username.getText().toString());
                sharedpreferences.savePassword(password.getText().toString());
                sharedpreferences.saveTeam(team.getText().toString());
                //exportDB();
                Toast.makeText(SettingsActivity.this, "Settings Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void exportDB(){
        File sd = getExternalFilesDir(null);
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
        String currentDBPath = "/data/" + "com.cs4624.poison.ivymap" + "/databases/"+dbHandler.getDatabaseName();
        String backupDBPath = dbHandler.getDatabaseName();
        File currentDB = new File(data, currentDBPath);
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
            }
        }
        else
        {
            Toast.makeText(this, "No SD Card Detected!", Toast.LENGTH_LONG).show();
        }
    }

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
