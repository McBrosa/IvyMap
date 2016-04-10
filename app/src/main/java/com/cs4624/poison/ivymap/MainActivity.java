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
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
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
    private Button absent_button, present_button, sync_button, currentL_button;
    public GPSTracker location;
    public DatabaseHandler database;
    private InputMethodManager inputManager;

    public static final String MyPREFERENCES = "MyPrefs";
    public static final String Username = "usernameKey";
    public static final String Password = "passwordKey";
    public static final String Team = "teamKey";
    SharedPreferences sharedpreferences;

    private String state = "IDLE";
    private int counter_key = 0;
    private static final int VINE = 1;
    private static final int SHRUB = 2;
    private static final int CREPPING = 3;
    private int type_counter = 0;

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
        sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
        currentL_button = (Button) findViewById(R.id.currentL_button);
        // Navbar
        RadioButton radioButton;
        radioButton = (RadioButton) findViewById(R.id.btnHome);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnDatabase);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);
        radioButton = (RadioButton) findViewById(R.id.btnSettings);
        radioButton.setOnCheckedChangeListener(btnNavBarOnCheckedChangeListener);

        // Initialize the GPS
        inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        location = new GPSTracker(this);
        if(!location.canGetLocation())
        {
            location.showSettingsAlert();
        }

        present_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(arg0.getContext());

                // Plant Id input
                final EditText input = new EditText(arg0.getContext());
                input.setHint("Plant ID (Optional)");
                input.setGravity(Gravity.CENTER);
                builder.setView(input);
                builder.setTitle("Pick Type")
                        .setItems(R.array.poison_ivy_array, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                String type = "";
                                String notifyType = "";
                                String plantId = input.getText().toString().trim();
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
                                PoisonIvy poisonIvy = new PoisonIvy(getTeam(), plantId, type, location.getLatitude(), location.getLongitude(), timeStamp, false);
                                database.addPI(poisonIvy);
                                sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
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
                sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
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
                                    sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    database.updateSyncStatus(pi, false);
                                    sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
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
                    //sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
                }
            }
        });

        currentL_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(arg0.getContext())
                        .setTitle("Coordinates:")
                        .setMessage("Lat: " + Double.toString(location.getLatitude()) + "\nLong: " +
                                Double.toString(location.getLongitude()));
                builder.show();
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


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //String counter_str = Integer.toString(type_counter);
        Log.d("onKeyDown", "!on key  ");
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_VOLUME_UP:                                                            //*****Up button pressed
                    if (state == "IDLE") {
                        Toast.makeText(this, "Press small round button to start" + counter_key, Toast.LENGTH_SHORT).show();
                    } else if (state == "CHOOSE") {
                        type_counter = type_counter % 3;
                        type_counter++;
                        if (type_counter == VINE) {          //vine
                            final MediaPlayer mp_vine = MediaPlayer.create(this, R.raw.vine);
                            mp_vine.start();
                            Toast.makeText(this, "Vine", Toast.LENGTH_SHORT).show();
                            //mp_vine.stop();
                        } else if (type_counter == SHRUB) {
                            final MediaPlayer mp_shrub = MediaPlayer.create(this, R.raw.shrub);
                            mp_shrub.start();
                            Toast.makeText(this, "Shrub", Toast.LENGTH_SHORT).show();
                            //mp_shrub.stop();
                        } else if (type_counter == CREPPING) {
                            final MediaPlayer mp_creeping = MediaPlayer.create(this, R.raw.creeping);
                            mp_creeping.start();
                            Toast.makeText(this, "Creeping", Toast.LENGTH_SHORT).show();
                            //mp_creeping.stop();
                        }
                    } else if (state == "PSorAB") {
                        final MediaPlayer mp_choose = MediaPlayer.create(this, R.raw.choose_plant);
                        Toast.makeText(this, "Present", Toast.LENGTH_SHORT).show();
                        mp_choose.start();
                        state = "CHOOSE";
                        //mp_choose.stop();
                    } else if (state == "VINE_COMFIRMED") {                                               // vine present
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        // Insert Vine Record
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, "V", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        Toast.makeText(this, "Vine/Linnea present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    } else if (state == "SHRUB_COMFIRMED") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        // Insert Shrub Record
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, "S", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        Toast.makeText(this, "Shrub present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    } else if (state == "CREEPING_COMFIRMED") {                                               // vine present
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        // Insert Creeping Record
                        Toast.makeText(this, "Creeping present comfirmed", Toast.LENGTH_SHORT).show();
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, "C", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        Toast.makeText(this, "Creeping present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    }
                    counter_key++;
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:                                                      //*****Down button pressed
                    if (state == "IDLE") {
                        Toast.makeText(this, "Press small round button to start" + counter_key, Toast.LENGTH_SHORT).show();
                    } else if (state == "ABSENT") {                                                  // vine absent
                        final MediaPlayer mp_absent = MediaPlayer.create(this, R.raw.absent);
                        mp_absent.start();
                        // Insert Absence Record
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(getTeam(), null, "A", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        Toast.makeText(this, "Absence comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_absent.stop();
                    } else if (state == "PSorAB") {                                                        //absent
                        final MediaPlayer mp_absent = MediaPlayer.create(this, R.raw.absent);
                        Toast.makeText(this, "Absent", Toast.LENGTH_SHORT).show();
                        mp_absent.start();
                        state = "IDLE";
                        //mp_absent.stop();
                    }
                    return true;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    //            case KeyEvent.KEYCODE_MEDIA_PLAY:
                    //            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (state == "IDLE") {
                        final MediaPlayer mp_prorab = MediaPlayer.create(this, R.raw.presentabsent);
                        Toast.makeText(this, "Present or Absent ? + for present, - for absent", Toast.LENGTH_SHORT).show();
                        mp_prorab.start();
                        state = "PSorAB";
                        //mp_prorab.stop();
                    } else if (type_counter == VINE && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Vine present comfirmed", Toast.LENGTH_SHORT).show();
                        //setCurrentMaker_in(counter_key);        //maker down on the map
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    } else if (type_counter == SHRUB && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Shrub present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    } else if (type_counter == CREPPING && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Creeping present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                        //mp_present.stop();
                    }
                    return true;
            }
            return false;
        }
        return onKeyDown(keyCode, event);
    }

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
