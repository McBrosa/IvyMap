/**
 * This represents the users preferences that where inserted on the settings page.
 *
 * @author Nathan Rosa
 * @date 04/28/2016
 */

package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    // The name of the Preferences to be viewed and edited
    public static final String MyPREFERENCES = "MyPrefs";
    // The username key to view and edit
    public static final String Username = "usernameKey";
    // The password key to view and edit
    public static final String Password = "passwordKey";
    // The team key to view and edit
    public static final String Team = "teamKey";
    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor _prefsEditor;

    /**
     * // Gets the preferences from the context passed in.
     * @param context current activity context
     */
    public AppPreferences(Context context) {
        this.sharedpreferences = context.getSharedPreferences(MyPREFERENCES, Activity.MODE_PRIVATE);
        this._prefsEditor = sharedpreferences.edit();
    }

    /**
     * Saves the username to the preferences
     * @param text the new username
     */
    public void saveUsername(String text) {
        _prefsEditor.putString(Username, text);
        _prefsEditor.commit();
    }

    /**
     * Saves the password to the preferences
     * @param text the new password
     */
    public void savePassword(String text) {
        _prefsEditor.putString(Password, text);
        _prefsEditor.commit();
    }

    /**
     * Saves the team name to the preferences
     * @param text the new team name
     */
    public void saveTeam(String text) {
        _prefsEditor.putString(Team, text);
        _prefsEditor.commit();
    }

    /**
     * Returns the username stored in the preferences
     * @return username
     */
    public String getUsername()
    {
        return sharedpreferences.getString(Username, "");
    }

    /**
     * Returns the password stored in the preferences
     * @return password
     */
    public String getPassword()
    {
        return sharedpreferences.getString(Password, "");
    }

    /**
     * Returns the team stored in the preferences
     * @return team
     */
    public String getTeam()
    {
        return sharedpreferences.getString(Team, "");
    }
}
