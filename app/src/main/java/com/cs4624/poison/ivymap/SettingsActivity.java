package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;


public class SettingsActivity extends Activity {
    private EditText username, password, team;
    private Button update;

    public static final String MyPREFERENCES = "MyPrefs" ;
    public static final String Username = "usernameKey";
    public static final String Password = "passwordKey";
    public static final String Team = "teamKey";
    SharedPreferences sharedpreferences;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        team = (EditText) findViewById(R.id.team);
        team.setHint("Team");
        username = (EditText) findViewById(R.id.username);
        username.setHint("Username");
        password = (EditText) findViewById(R.id.password);
        password.setHint("Password");
        update = (Button) findViewById(R.id.update);

        //Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnAll);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnPicture);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnVideo);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnFile);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

        if(getTeam() != "")
        {
            team.setText(getTeam());
        }
        if(getUsername() != "")
        {
            username.setText(getUsername());
        }
        if(getPassword() != "")
        {
            password.setText(getPassword());
        }

        update.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                String un  = username.getText().toString();
                String p  = password.getText().toString();
                String t  = team.getText().toString();

                SharedPreferences.Editor editor = sharedpreferences.edit();

                editor.putString(Username, un);
                editor.putString(Password, p);
                editor.putString(Team, t);
                editor.commit();
                Toast.makeText(SettingsActivity.this, "Settings Updated", Toast.LENGTH_LONG).show();
            }
        });
    }

    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Toast.makeText(SettingsActivity.this, buttonView.getText(), Toast.LENGTH_SHORT).show();
            }
            switch (buttonView.getId()) {
                case R.id.btnAll:
                    Intent homeIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    break;

                case R.id.btnFile:
                    break;

                case R.id.btnPicture:
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
