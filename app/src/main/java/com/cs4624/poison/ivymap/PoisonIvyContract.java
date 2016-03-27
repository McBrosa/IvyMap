package com.cs4624.poison.ivymap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public final class PoisonIvyContract {
    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public PoisonIvyContract() {}

    /* Inner class that defines the table contents */
    public static abstract class PoisonIvy implements BaseColumns {
        public static final String TABLE_NAME = "poison_ivy";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_lEAF_ID = "leaf_id";
        public static final String COLUMN_NAME_LEAF_TYPE = "leaf_type";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
    }
}