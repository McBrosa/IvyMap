package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Nathan on 4/15/2016.
 */
public class AppPreferences {
    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Username = "usernameKey";
    public static final String Password = "passwordKey";
    public static final String Team = "teamKey";
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor _prefsEditor;

    public AppPreferences(Context context) {
        this.sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Activity.MODE_PRIVATE);
        this._prefsEditor = sharedpreferences.edit();
    }

    public void saveUsername(String text) {
        _prefsEditor.putString(Username, text);
        _prefsEditor.commit();
    }

    public void savePassword(String text) {
        _prefsEditor.putString(Password, text);
        _prefsEditor.commit();
    }

    public void saveTeam(String text) {
        _prefsEditor.putString(Team, text);
        _prefsEditor.commit();
    }

    public String getUsername()
    {
        return sharedpreferences.getString(Username, "");
    }

    public String getPassword()
    {
        return sharedpreferences.getString(Password, "");
    }

    public String getTeam()
    {
        return sharedpreferences.getString(Team, "");
    }
}
