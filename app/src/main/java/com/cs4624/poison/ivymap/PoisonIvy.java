/*
* The PoisonIvy class stores all the information necessary, including Leaf ID, Leaf Type, Latitude,
* Longitude, Timestamp, and whether or not the record has been synced to the database.
*
* @author Nathan Rosa
* @date 4/3/16
* @version 1.0
*/

package com.cs4624.poison.ivymap;

public class PoisonIvy {
    private String type;
    private String plantId;
    private String team;
    private double latitude;
    private double longitude;
    private String timeStamp;
    private boolean sync;

    public PoisonIvy(){}

    public PoisonIvy(String team, String type, double latitude, double longitude, String timeStamp, boolean sync) {
        this.team = team;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.sync = sync;
    }

    public PoisonIvy(String team, String plantId, String type, double latitude, double longitude, String timeStamp, boolean sync) {
        this.team = team;
        this.plantId = plantId;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeStamp = timeStamp;
        this.sync = sync;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getPlantId() {
        if(plantId == null)
        {
            return "";
        }
        return plantId;
    }

    public void setPlantId(String plantId) {
        this.plantId = plantId;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public boolean getSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }
}
