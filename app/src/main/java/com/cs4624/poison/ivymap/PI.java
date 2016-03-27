package com.cs4624.poison.ivymap;

/**
 * Created by Nathan on 3/21/2016.
 */
public class PI {
    private String type;
    private long leaf_id;
    private int id;
    double latitude;
    double longitude;

    public PI(){}


    public PI(String type, double latitude, double longitude) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public PI(long leaf_id, String type, double latitude, double longitude) {
        this.leaf_id = leaf_id;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public long getLeaf_id() {
        return leaf_id;
    }

    public void setLeaf_id(long leaf_id) {
        this.leaf_id = leaf_id;
    }
}
