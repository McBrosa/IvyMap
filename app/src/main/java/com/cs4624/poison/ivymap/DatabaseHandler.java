package com.cs4624.poison.ivymap;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "poison_ivy.db";

    // Contacts table name
    private static final String TABLE_NAME = "poison_ivy";

    private Context context;

    // Contacts Table Columns names
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_lEAF_ID = "leaf_id";
    private static final String COLUMN_NAME_LEAF_TYPE = "leaf_type";
    private static final String COLUMN_NAME_LATITUDE = "latitude";
    private static final String COLUMN_NAME_LONGITUDE = "longitude";
    private static final String COLUMN_NAME_TIMESTAMP = "date_time";
    private static final String COLUMN_NAME_SYNC = "sync";

    // Helpers for MySQL
    private static final String DOUBLE_TYPE = " DOUBLE";
    private static final String INT_TYPE = " INT";
    private static final String BIGINT_TYPE = " BIGINT";
    private static final String CHAR_1 = " NCHAR(1)";
    private static final String NOT_NULL = " NOT NULL";
    private static final String TIMESTAMP = " VARCHAR(25)";
    private static final String NULL = " NULL";
    private static final String COMMA_SEP = ",";
    String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_NAME_lEAF_ID + " TEXT" + NULL + COMMA_SEP +
                    COLUMN_NAME_LEAF_TYPE + CHAR_1 + NULL + COMMA_SEP +
                    COLUMN_NAME_LATITUDE + DOUBLE_TYPE + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_LONGITUDE + DOUBLE_TYPE + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_TIMESTAMP + TIMESTAMP + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_SYNC + INT_TYPE + NOT_NULL +
                    ");"; //CONSTRAINT poison_ivy_pk PRIMARY KEY (id)
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new poison ivy record
    void addPI(PI poisonIvy) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_lEAF_ID, poisonIvy.getLeaf_id());
        values.put(COLUMN_NAME_LEAF_TYPE, poisonIvy.getType());
        values.put(COLUMN_NAME_LATITUDE, poisonIvy.getLatitude());
        values.put(COLUMN_NAME_LONGITUDE, poisonIvy.getLongitude());
        values.put(COLUMN_NAME_TIMESTAMP, poisonIvy.getTimeStamp());
        values.put(COLUMN_NAME_SYNC, poisonIvy.getSync());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

    // Getting single contact
    PI getPI(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_NAME_ID,
                        COLUMN_NAME_LATITUDE, COLUMN_NAME_LONGITUDE }, COLUMN_NAME_ID + "=?",
                new String[] { String.valueOf(id) }, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

//        PI poisonIvy = new PI(cursor.getString(0), Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2));
        PI poisonIvy = new PI(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2)), cursor.getString(3), Boolean.parseBoolean(cursor.getString(4)));
        // return contact
        return poisonIvy;
    }

    // Getting All Contacts
    public List<PI> getAllUnsyncedPIs() {
        List<PI> piList = new ArrayList<PI>();
        // Select All Query
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " where sync=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PI poisonIvy = new PI();
                poisonIvy.setLeaf_id(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_lEAF_ID)));
                poisonIvy.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LEAF_TYPE)));
                poisonIvy.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LATITUDE))));
                poisonIvy.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LONGITUDE))));
                poisonIvy.setTimeStamp(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP)));
                // Lets the local database know the record is synced to server
                poisonIvy.setSync(true);
                // Adding contact to list
                piList.add(poisonIvy);
            } while (cursor.moveToNext());
        }

        // return contact list
        return piList;
    }

//    // Updating single contact
//    public int updateContact(PI contact) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_NAME, contact.getName());
//        values.put(KEY_PH_NO, contact.getPhoneNumber());
//
//        // updating row
//        return db.update(TABLE_NAME, values, KEY_ID + " = ?",
//                new String[] { String.valueOf(contact.getID()) });
//    }

    // Deleting single contact
//    public void deletePI(PI poisonIvy) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        db.delete(TABLE_NAME, COLUMN_NAME_ID + " = ?",
//                new String[] { String.valueOf(poisonIvy.getId()) });
//        db.close();
//    }


    // Getting contacts Count
    public int getPICount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}