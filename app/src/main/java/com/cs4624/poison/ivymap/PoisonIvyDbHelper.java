package com.cs4624.poison.ivymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cs4624.poison.ivymap.PoisonIvyContract.PoisonIvy;

public class PoisonIvyDbHelper extends SQLiteOpenHelper{
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "poison_ivy.db";

    /* Create the table */
    private static final String DOUBLE_TYPE = " decimal(16,13)";
    private static final String INT_TYPE = " int";
    private static final String BIGINT_TYPE = " bigint";
    private static final String CHAR_1 = " char(1)";
    private static final String NOT_NULL = " NOT NULL";
    private static final String NULL = " NULL";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + PoisonIvy.TABLE_NAME + " (" +
                    PoisonIvy.COLUMN_NAME_ID + INT_TYPE + NOT_NULL + " AUTO_INCREMENT" + COMMA_SEP +
                    PoisonIvy.COLUMN_NAME_lEAF_ID + BIGINT_TYPE + NULL + COMMA_SEP +
                    PoisonIvy.COLUMN_NAME_LEAF_TYPE + CHAR_1 + NULL + COMMA_SEP +
                    PoisonIvy.COLUMN_NAME_LATITUDE + DOUBLE_TYPE + NOT_NULL + COMMA_SEP +
                    PoisonIvy.COLUMN_NAME_LONGITUDE + DOUBLE_TYPE + NOT_NULL + COMMA_SEP +
                    "CONSTRAINT poison_ivy_pk PRIMARY KEY (id))";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + PoisonIvy.TABLE_NAME;

    public PoisonIvyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
