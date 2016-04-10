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

    // Contacts Table Columns names
    private static final String COLUMN_NAME_ID = "id";
    private static final String COLUMN_NAME_TEAM = "team";
    private static final String COLUMN_NAME_PLANT_ID = "plant_id";
    private static final String COLUMN_NAME_PLANT_TYPE = "plant_type";
    private static final String COLUMN_NAME_LATITUDE = "latitude";
    private static final String COLUMN_NAME_LONGITUDE = "longitude";
    private static final String COLUMN_NAME_TIMESTAMP = "date_time";
    private static final String COLUMN_NAME_SYNC = "sync";

    // Helpers for MySQL
    private static final String TEXT = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String CHAR_1 = " NCHAR(1)";
    private static final String NOT_NULL = " NOT NULL";
    private static final String TIMESTAMP = " VARCHAR(25)";
    private static final String NULL = " NULL";
    private static final String COMMA_SEP = ",";
    String SQL_CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT" + COMMA_SEP +
                    COLUMN_NAME_PLANT_ID + " TEXT" + NULL + COMMA_SEP +
                    COLUMN_NAME_TEAM + " TEXT" + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_PLANT_TYPE + CHAR_1 + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_LATITUDE + TEXT + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_LONGITUDE + TEXT + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_TIMESTAMP + TIMESTAMP + NOT_NULL + COMMA_SEP +
                    COLUMN_NAME_SYNC + INT_TYPE + NOT_NULL +
                    ");";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;


    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Table
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
     * Inserts the PoisonIvy record in to the database
     *
     *@param poisonIvy The PoisonIvy record to be inserted into the local SQLite
     *                 database table.
     */
    void addPI(PoisonIvy poisonIvy) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_PLANT_ID, poisonIvy.getPlantId());
        values.put(COLUMN_NAME_TEAM, poisonIvy.getTeam());
        values.put(COLUMN_NAME_PLANT_TYPE, poisonIvy.getType());
        values.put(COLUMN_NAME_LATITUDE, poisonIvy.getLatitude());
        values.put(COLUMN_NAME_LONGITUDE, poisonIvy.getLongitude());
        values.put(COLUMN_NAME_TIMESTAMP, poisonIvy.getTimeStamp());
        values.put(COLUMN_NAME_SYNC, poisonIvy.getSync());

        // Inserting Row
        db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
    }

//    PoisonIvy getPI(int id) {
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.query(TABLE_NAME, new String[] { COLUMN_NAME_ID,
//                        COLUMN_NAME_LATITUDE, COLUMN_NAME_LONGITUDE }, COLUMN_NAME_ID + "=?",
//                new String[] { String.valueOf(id) }, null, null, null, null);
//        if (cursor != null)
//            cursor.moveToFirst();
//
//        PI poisonIvy = new PI(cursor.getString(0), Integer.parseInt(cursor.getString(0)),
//                cursor.getString(1), cursor.getString(2));
//        PoisonIvy poisonIvy = new PoisonIvy(cursor.getString(0),Double.parseDouble(cursor.getString(1)),Double.parseDouble(cursor.getString(2)), cursor.getString(3), Boolean.parseBoolean(cursor.getString(4)));
//        // return contact
//        return poisonIvy;
//    }

    /**
     * Gets a list of all the PoisonIvy Objects who have a sync value of
     * false.
     *
     *@return List of all PoisonIvy records that need to be Synced
     */
    public List<PoisonIvy> getAllUnsyncedPIs() {
        List<PoisonIvy> piList = new ArrayList<PoisonIvy>();
        // Select query where sync column is false
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " where sync=0";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                PoisonIvy poisonIvy = new PoisonIvy();
                poisonIvy.setPlantId(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLANT_ID)));
                poisonIvy.setTeam(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TEAM)));
                poisonIvy.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PLANT_TYPE)));
                poisonIvy.setLatitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LATITUDE))));
                poisonIvy.setLongitude(Double.parseDouble(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_LONGITUDE))));
                poisonIvy.setTimeStamp(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TIMESTAMP)));
                // Lets the local database know the record is synced to server
                poisonIvy.setSync(true);
                // Adding contact to list
                piList.add(poisonIvy);
            } while (cursor.moveToNext());
        }
        // return PoisonIvy list
        return piList;
    }

    /**
     * Updates the current status of the PoisonIvy record's field sync to true in the local
     * local database.
     *
     * @param contact The PoisonIvy record that is being updated
     */
    public int updateSyncStatus(PoisonIvy contact, boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        contact.setSync(status);
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_SYNC, contact.getSync());

        // updating row
        return db.update(TABLE_NAME, values, COLUMN_NAME_TIMESTAMP + " = ?",
                new String[] { String.valueOf(contact.getTimeStamp()) });
    }

    /**
     * Deletes the most recent record insert into the table.
     */
    public void deleteMostRecentPI() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE id = (SELECT MAX(id) FROM " + TABLE_NAME +");");
        db.close();
    }


    // Getting contacts Count
    public int getPICount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }


    /**
     * Helper function that parses a given table into a string
     * and returns it for easy printing. The string consists of
     * the table name and then each row is iterated through with
     * column_name: value pairs printed out.
     *
     * @return the table tableName as a string
     */
    public String getTableAsString() {
        SQLiteDatabase db = this.getReadableDatabase();
        String tableString = String.format("Table %s:\n", TABLE_NAME);
        Cursor allRows = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (allRows.moveToLast()) {
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name : columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToPrevious());
        }

        return tableString;
    }
}