package com.cs4624.poison.ivymap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {

    private Button absent_button, present_button, sync_button, currentL_button;
    private GPSTracker location;
    private DatabaseHandler database;
    private AppPreferences sharedpreferences;

    private String state = "IDLE";
    private int counter_key = 0;
    private static final int VINE = 1;
    private static final int SHRUB = 2;
    private static final int CREPPING = 3;
    private int type_counter = 0;

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        // Create or get the table
        database = new DatabaseHandler(getApplicationContext());
        // Set up shared preferences if there are any.
        sharedpreferences = new AppPreferences(getApplicationContext());

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
        //inputManager = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        location = new GPSTracker(getApplicationContext(), this);
        if (location.canGetLocation()) {

            location.getLatitude();
            location.getLongitude();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            location.showSettingsAlert();
        }

        present_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Display dialog for user to choose type and put in optional plant id
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
                                // Add the poison ivy record to the local database
                                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), plantId, type, location.getLatitude(), location.getLongitude(), timeStamp, false);
                                database.addPI(poisonIvy);
                                // Update the sync button to show the new number of unsynced records
                                sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
                                Toast.makeText(getApplicationContext(), notifyType + " Recorded", Toast.LENGTH_SHORT).show();
                            }
                        });
                builder.show();
            }
        });

        absent_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Add an absent poison ivy record to local database
                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), null, "A", location.getLatitude(), location.getLongitude(), timeStamp, false);
                database.addPI(poisonIvy);

                sync_button.setText("Sync " + database.getAllUnsyncedCount() + " Records");
                Toast.makeText(getApplicationContext(), "Absence Recorded", Toast.LENGTH_SHORT).show();
            }
        });

        // Gets all the unsynced poison ivy records and inserts them into the database.
        sync_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String insertURL = "http://vtpiat.netau.net/insert.php";
                //String insertURL = "https://oak.ppws.vt.edu/~cs4624/post/insert.php";
                if (sharedpreferences.getUsername().isEmpty() || sharedpreferences.getPassword().isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please provide a username and password for MySQL database", Toast.LENGTH_SHORT).show();
                }
                else {
                    if (!database.getAllUnsyncedPIs().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Updating...", Toast.LENGTH_SHORT).show();
                        // Create the request que for the StringRequests to be executed
                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
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
                                    Toast.makeText(getApplicationContext(), error.getLocalizedMessage() + "\nError uploading... Please try again", Toast.LENGTH_SHORT).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> parameters = new HashMap<String, String>();
                                    parameters.put("username", sharedpreferences.getUsername());
                                    parameters.put("password", sharedpreferences.getPassword());
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
                        Toast.makeText(getApplicationContext(), "There are No New Records to Sync", Toast.LENGTH_SHORT).show();
                    }
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
    }



    private CompoundButton.OnCheckedChangeListener btnNavBarOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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

    /**
     * Once the user accepts the permission this method is called and will return the user to the
     * home screen
     *
     * @param requestCode The request code passed in
     * @param permissions The requested permissions
     * @param grantResults The granted results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            Intent mainIntent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(mainIntent);
        } else
        {
            Toast.makeText(this, "The app was not allowed to write to your storage or access location. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * The hardware input listener for the auxiliary port.
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //String counter_str = Integer.toString(type_counter);
        // Log.d("onKeyDown", "!on key  ");
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
                        } else if (type_counter == SHRUB) {
                            final MediaPlayer mp_shrub = MediaPlayer.create(this, R.raw.shrub);
                            mp_shrub.start();
                            Toast.makeText(this, "Shrub", Toast.LENGTH_SHORT).show();
                        } else if (type_counter == CREPPING) {
                            final MediaPlayer mp_creeping = MediaPlayer.create(this, R.raw.creeping);
                            mp_creeping.start();
                            Toast.makeText(this, "Creeping", Toast.LENGTH_SHORT).show();
                        }
                    } else if (state == "PSorAB") {
                        final MediaPlayer mp_choose = MediaPlayer.create(this, R.raw.choose_plant);
                        Toast.makeText(this, "Present", Toast.LENGTH_SHORT).show();
                        mp_choose.start();
                        state = "CHOOSE";
                    } else if (state == "VINE_COMFIRMED") {                                               // vine present
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Vine/Linnea present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                    } else if (state == "SHRUB_COMFIRMED") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Shrub present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                    } else if (state == "CREEPING_COMFIRMED") {                                               // vine present
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        // Insert Creeping Record
                        Toast.makeText(this, "Creeping present comfirmed", Toast.LENGTH_SHORT).show();
                        state = "IDLE";
                        type_counter = 0;
                    }
                    counter_key++;
                    return true;
                case KeyEvent.KEYCODE_VOLUME_DOWN:                                                      //*****Down button pressed
                    if (state == "IDLE") {
                        Toast.makeText(this, "Press small round button to start" + counter_key, Toast.LENGTH_SHORT).show();
                    }  else if (state == "PSorAB") {                                                        //absent
                        final MediaPlayer mp_absent = MediaPlayer.create(this, R.raw.absent);
                        // Absent Recorded
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), null, "A", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        Toast.makeText(this, "Absence comfirmed", Toast.LENGTH_SHORT).show();
                        mp_absent.start();
                        state = "IDLE";
                    }
                    return true;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (state == "IDLE") {
                        final MediaPlayer mp_prorab = MediaPlayer.create(this, R.raw.presentabsent);
                        Toast.makeText(this, "Present or Absent ? + for present, - for absent", Toast.LENGTH_SHORT).show();
                        mp_prorab.start();
                        state = "PSorAB";
                    } else if (type_counter == VINE && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Vine present comfirmed", Toast.LENGTH_SHORT).show();
                        // Vine Recorded
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), null, "V", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        state = "IDLE";
                        type_counter = 0;
                    } else if (type_counter == SHRUB && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Shrub present comfirmed", Toast.LENGTH_SHORT).show();
                        // Shrub Recorded
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), null, "S", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        state = "IDLE";
                        type_counter = 0;
                    } else if (type_counter == CREPPING && state == "CHOOSE") {
                        final MediaPlayer mp_present = MediaPlayer.create(this, R.raw.present);
                        mp_present.start();
                        Toast.makeText(this, "Creeping present comfirmed", Toast.LENGTH_SHORT).show();
                        // Creeping Recorded
                        final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                        PoisonIvy poisonIvy = new PoisonIvy(sharedpreferences.getTeam(), null, "C", location.getLatitude(), location.getLongitude(), timeStamp, false);
                        database.addPI(poisonIvy);
                        state = "IDLE";
                        type_counter = 0;
                    }
                    return true;
            }
            return false;
        }
        return onKeyDown(keyCode, event);
    }

//    @Override
//    public boolean onSingleTapConfirmed(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public boolean onDoubleTap(MotionEvent e) {
//
//        return false;
//    }
//
//    @Override
//    public boolean onDoubleTapEvent(MotionEvent e) {
//        Toast.makeText(this, "double tap", Toast.LENGTH_SHORT).show();
//        return false;
//    }
//
//    @Override
//    public boolean onDown(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public void onShowPress(MotionEvent e) {
//
//    }
//
//    @Override
//    public boolean onSingleTapUp(MotionEvent e) {
//        return false;
//    }
//
//    @Override
//    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//
//        return false;
//    }
//
//    @Override
//    public void onLongPress(MotionEvent e) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        float brightness = 0.001f;
//        lp.screenBrightness = brightness;
//        getWindow().setAttributes(lp);
//        Toast toast = new Toast(getApplicationContext());
//        toast.makeText(this, "long press", Toast.LENGTH_SHORT).show();
//        toast.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
//
//    }
//
//    @Override
//    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//        WindowManager.LayoutParams lp = getWindow().getAttributes();
//        float brightness = 1f;
//        lp.screenBrightness = brightness;
//        getWindow().setAttributes(lp);
//        Toast.makeText(this, "fling", Toast.LENGTH_SHORT).show();
//        return false;
//    }
}
