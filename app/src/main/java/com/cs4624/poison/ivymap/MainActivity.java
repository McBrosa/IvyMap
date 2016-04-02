package com.cs4624.poison.ivymap;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.text.method.ScrollingMovementMethod;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends Activity {
    private EditText leafId, leafType;
    private Button insert, delete, show, sync, localTable;
    private TextView records, latText, longText;
    private RequestQueue requestQueue;
    private GPSTracker location;
    private double longitude;
    private double latitude;
    private DatabaseHandler database;

    private final static int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    private String insertURL = "http://vtpiat.netau.net/insert.php";
    private String showURL = "http://vtpiat.netau.net/show.php";

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        database = new DatabaseHandler(getApplicationContext());
        leafId = (EditText) findViewById(R.id.leafId);
        leafId.setHint("Leaf ID");
        leafType = (EditText) findViewById(R.id.leafType);
        leafType.setHint("Leaf Type");
        insert = (Button) findViewById(R.id.insert);
        show = (Button) findViewById(R.id.show);
        delete =(Button) findViewById(R.id.delete);
        localTable =(Button) findViewById(R.id.localTable);
        sync =(Button) findViewById(R.id.sync);
        records = (TextView) findViewById(R.id.records);
        records.setMovementMethod(new ScrollingMovementMethod());
        latText = (TextView) findViewById(R.id.latText);
        longText = (TextView) findViewById(R.id.longText);

        location = new GPSTracker(getApplicationContext());
        if(location.canGetLocation())
        {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }

        latText.setText("Lat: " + Double.toString(latitude));
        longText.setText("Long: " + Double.toString(longitude));

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                records.setText("");
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, showURL, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray poison_ivy = response.getJSONArray("poison_ivy");
                            for (int i = 0; i < poison_ivy.length(); i++) {
                                JSONObject pi = poison_ivy.getJSONObject(i);
                                String leaf_id = pi.getString("leaf_id");
                                String leaf_type = pi.getString("leaf_type");
                                String latitude = pi.getString("latitude");
                                String longitude = pi.getString("longitude");
                                String date_time = pi.getString("date_time");

                                records.append("Leaf id: " + leaf_id +  ", Type: " + leaf_type + "\nLat: " + latitude + ", Long: " + longitude
                                + "\nTimeStamp: " + date_time + "\n");
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error){ }
                });
                requestQueue.add(jsonObjectRequest);
            }
        });

        // Inserts records into the local SQLite database
        insert.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                final String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                PI poisonIvy = new PI(leafId.getText().toString(),leafType.getText().toString(),latitude,longitude,timeStamp,false);
                database.addPI(poisonIvy);

                Toast.makeText(getApplicationContext(), "Record Inserted", Toast.LENGTH_SHORT).show();
                leafType.setText("");
                leafId.setText("");
            }
        });

        // Gets all the unsynced poison ivy records and inserts them into the database.
        sync.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                for(final PI pi : database.getAllUnsyncedPIs())
                {
                    StringRequest request =  new StringRequest(Request.Method.POST, insertURL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Error uploading... Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError{
                            Map<String, String> parameters = new HashMap<String, String>();
                            parameters.put("leaf_id", pi.getLeaf_id());
                            parameters.put("leaf_type", pi.getType());
                            parameters.put("latitude", Double.toString(pi.getLatitude()));
                            parameters.put("longitude", Double.toString(pi.getLongitude()));
                            parameters.put("date_time", pi.getTimeStamp());
                            return parameters;
                        }
                    };
                    requestQueue.add(request);
                    // Lets the local database know the record has been synced to the server.
                    pi.setSync(true);
                    database.updateSyncStatus(pi);
                }
                Toast.makeText(getApplicationContext(), "Database updated", Toast.LENGTH_SHORT).show();
                leafType.setText("");
                leafId.setText("");
            }
        });
        delete.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                database.deleteMostRecentPI();
                Toast.makeText(getApplicationContext(), "Most Recent Record was Deleted", Toast.LENGTH_SHORT).show();
            }
        });

        localTable.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                records.setText("");
                records.append(database.getTableAsString());
            }
        });
    }
}
