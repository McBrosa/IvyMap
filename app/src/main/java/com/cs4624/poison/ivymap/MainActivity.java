package com.cs4624.poison.ivymap;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends Activity {
    private EditText leafId, leafType;
    private Button insert, delete, show, sync, localTable, backup, settings;
    private Button absent_button, present_button, sync_button;
    public GPSTracker location;
    public DatabaseHandler database;
    private InputMethodManager inputManager;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Username = "usernameKey";
    public static final String Password = "passwordKey";
    public static final String Team = "teamKey";
    SharedPreferences sharedpreferences;

    private static final String insertUrl = "teamKey";
    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int OVERLAY_PERMISSION_REQ_CODE = 1234;
    private static final int REQUEST_WRITE_STORAGE = 112;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Check to see if there is permission for location services
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        // Check to see if there is permission to save to external sdcard
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_WRITE_STORAGE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }

        // Create or get the table
        database = new DatabaseHandler(getApplicationContext());

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // Initialize buttons and views
        present_button = (Button) findViewById(R.id.present_button);
        absent_button = (Button) findViewById(R.id.absent_button);
        sync_button = (Button) findViewById(R.id.sync_button);
        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
//        settings = (Button) findViewById(R.id.settings);
//        absent_button = (Button) findViewById(R.id.absent_button);
//        present_button = (Button) findViewById(R.id.present_button);
//        sync_button= (Button) findViewById(R.id.sync_button);
//        insert = (Button) findViewById(R.id.insert);
//        show = (Button) findViewById(R.id.show);
//        delete =(Button) findViewById(R.id.delete);
//        localTable =(Button) findViewById(R.id.localTable);
//        sync =(Button) findViewById(R.id.sync);
//        backup =(Button) findViewById(R.id.backup);
//        records = (TextView) findViewById(R.id.records);
//        records.setMovementMethod(new ScrollingMovementMethod());
//        latText = (TextView) findViewById(R.id.latText);
//        longText = (TextView) findViewById(R.id.longText);

        // Initialize the GPS
        inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        location = new GPSTracker(this);
        if(!location.canGetLocation())
        {
            location.showSettingsAlert();
        }

//        settings.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View arg0) {
//                // Start SettingsActivity.class
//                Intent myIntent = new Intent(MainActivity.this,
//                        SettingsActivity.class);
//                startActivity(myIntent);
//            }
//        });

        present_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(arg0.getContext());
                builder.setTitle("Pick Type")
                        .setItems(R.array.poison_ivy_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                String type = "";
                                String notifyType = "";
                                switch (which) {
                                    case 0:
                                        type = "V";
                                        notifyType = "Vine/Linnea";
                                        break;
                                    case 1:
                                        type = "C";
                                        notifyType = "Creeping";
                                        break;
                                    case 2:
                                        type = "S";
                                        notifyType = "Shrub";
                                        break;
                                }
                                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, type, location.getLatitude(), location.getLongitude(), timeStamp, false);
                                database.addPI(poisonIvy);
                                Toast.makeText(getApplicationContext(), notifyType + " Recorded", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
               }
        });

        absent_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, "A", location.getLatitude(), location.getLongitude(), timeStamp, false);
                database.addPI(poisonIvy);
                Toast.makeText(getApplicationContext(), "Absence Recorded", Toast.LENGTH_SHORT).show();
            }
        });

//        show.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                records.setText("");
//                if (getUsername().isEmpty() || getPassword().isEmpty()) {
//                    Toast.makeText(getApplicationContext(), "Please provide a username and password for MySQL database", Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//                    String showURL = "https://oak.ppws.vt.edu/~cs4624/post/show.php";
//                    StringRequest jsonObjectRequest = new StringRequest(Request.Method.POST, showURL, new Response.Listener<String>() {
//                        @Override
//                        public void onResponse(String response) {
//                                records.append(prettyJson(response));
//                        }
//                    }, new Response.ErrorListener() {
//                        @Override
//                        public void onErrorResponse(VolleyError error) {
//                        }
//
//                    }){
//                        @Override
//                        protected Map<String, String> getParams() throws AuthFailureError {
//                            Map<String, String> parameters = new HashMap<String, String>();
//                            parameters.put("username", getUsername());
//                            parameters.put("password", getPassword());
//                            return parameters;
//                        }
//                    };
//                    requestQueue.add(jsonObjectRequest);
//                }
//            }
//        });
//
//        // Inserts records into the local SQLite database
//        insert.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//                PoisonIvy poisonIvy = new PoisonIvy(getTeam(), leafId.getText().toString(),leafType.getText().toString(),location.getLatitude(), location.getLongitude(),timeStamp,false);
//                database.addPI(poisonIvy);
//
//                Toast.makeText(getApplicationContext(), "Record Inserted", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        // Inserts records into the local SQLite database
//        absent_button.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//                PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null,"A",location.getLatitude(), location.getLongitude(),timeStamp,false);
//                database.addPI(poisonIvy);
//
//                Toast.makeText(getApplicationContext(), "Absence Recorded", Toast.LENGTH_SHORT).show();
//            }
//        });
//
        // Gets all the unsynced poison ivy records and inserts them into the database.
        sync_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (getUsername().isEmpty() || getPassword().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please provide a username and password for MySQL database", Toast.LENGTH_SHORT).show();
                }
                else {
                    //String insertURL = "https://oak.ppws.vt.edu/~cs4624/post/insert.php";
                    String insertURL = "http://vtpiat.netau.net/insert.php";
                    Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                    RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                    if (!database.getAllUnsyncedPIs().isEmpty()) {
                        for (final PoisonIvy pi : database.getAllUnsyncedPIs()) {
                            StringRequest request = new StringRequest(Request.Method.POST, insertURL, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    // Lets the local database know the record has been synced
                                    database.updateSyncStatus(pi, true);
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    database.updateSyncStatus(pi, false);
                                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage() + " Error uploading... Please try again", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> parameters = new HashMap<String, String>();
                                    parameters.put("username", getUsername());
                                    parameters.put("password", getPassword());
                                    parameters.put("team", pi.getTeam());
                                    parameters.put("plant_id", pi.getPlantId());
                                    parameters.put("plant_type", pi.getType());
                                    parameters.put("latitude", Double.toString(pi.getLatitude()));
                                    parameters.put("longitude", Double.toString(pi.getLongitude()));
                                    parameters.put("date_time", pi.getTimeStamp());
                                    return parameters;
                                }
                            };
                            requestQueue.add(request);
                        }
                        Toast.makeText(getApplicationContext(), "Complete", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "There is No New Records to Sync", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

//        delete.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                // Confirmation box for deleting the most recent record
//                new AlertDialog.Builder(view.getContext())
//                        .setTitle("Confirm")
//                        .setMessage("Do you really want to do perform this action?")
//                        .setIcon(android.R.drawable.ic_dialog_alert)
//                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int whichButton) {
//                                // Delete the last record and display message confirmation
//                                database.deleteMostRecentPI();
//                                Toast.makeText(getApplicationContext(), "Most Recent Record was Deleted", Toast.LENGTH_SHORT).show();
//                            }})
//                        .setNegativeButton(android.R.string.no,new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog,int id) {
//                                // if this button is clicked, just close
//                                // the dialog box and do nothing
//                                dialog.cancel();
//                            }
//                        }).show();
//            }
//        });
//
//        localTable.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                records.setText("");
//                records.append(database.getTableAsString());
//            }
//        });
//
//        backup.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view){
//                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
//                backUpDatabaseToSDCard();
//                Toast.makeText(getApplicationContext(), "Backup Complete", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                Toast.makeText(MainActivity.this, buttonView.getText(), Toast.LENGTH_SHORT).show();
            }
            switch (buttonView.getId()) {
                case R.id.btnHome:
                    break;

                case R.id.btnSettings:
                    Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.btnDatabase:
                    Intent tableIntent = new Intent(MainActivity.this, DatabaseActivity.class);
                    startActivity(tableIntent);
                    break;
            }
        }
    };

    private void backUpDatabaseToSDCard() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            File data = Environment.getDataDirectory();

            if (sd.canWrite()) {
                String currentDBPath = "/data/com.cs4624.poison.ivymap/databases/" + database.getDatabaseName();
                String backupDBPath = getExternalMounts() + "/" +database.getDatabaseName();
                File currentDB = new File(data, currentDBPath);
                File backupDB = new File(sd, backupDBPath);

                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
        }
    }

    private boolean isEmpty(EditText etText) {
        return etText.getText().toString().trim().length() == 0;
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


    private String prettyJson(String uglyStr){
        int spacesToIndentEachLevel = 2;
        try {
            return new JSONObject(uglyStr).toString(spacesToIndentEachLevel);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return "Error";
    }


    public static HashSet<String> getExternalMounts() {
        final HashSet<String> out = new HashSet<String>();
        String reg = "(?i).*vold.*(vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
        String s = "";
        try {
            final Process process = new ProcessBuilder().command("mount")
                    .redirectErrorStream(true).start();
            process.waitFor();
            final InputStream is = process.getInputStream();
            final byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                s = s + new String(buffer);
            }
            is.close();
        } catch (final Exception e) {
            e.printStackTrace();
        }

        // parse output
        final String[] lines = s.split("\n");
        for (String line : lines) {
            if (!line.toLowerCase(Locale.US).contains("asec")) {
                if (line.matches(reg)) {
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        if (part.startsWith("/"))
                            if (!part.toLowerCase(Locale.US).contains("vold"))
                                out.add(part);
                    }
                }
            }
        }
        return out;
    }
}
