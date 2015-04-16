package com.freecoders.photobook.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Alex on 2014-11-27.
 */
public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_FRIENDS = "friends";
    public static final String TABLE_IMAGES = "images";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_NAME = "Name";
    public static final String COLUMN_CONTACT_KEY = "ContactKey";
    public static final String COLUMN_USER_ID = "UserId";
    public static final String COLUMN_AVATAR = "Avatar";
    public static final String COLUMN_STATUS = "Status";
    public static final String COLUMN_TYPE = "Type";

    public static final String COLUMN_MEDIASTORE_ID = "mediaStoreId";
    public static final String COLUMN_ORIG_URI = "origUri";
    public static final String COLUMN_THUMB_URI = "thumbUri";
    public static final String COLUMN_SERVER_ID = "serverId";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_BUCKET_ID = "bucketId";

    private static final String DATABASE_NAME = "photobook.db";
    private static final int DATABASE_VERSION = 6;

    // Database creation sql statement
    private static final String DATABASE_CREATE_FRIENDS_TABLE = "create table "
            + TABLE_FRIENDS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " varchar(1000), "
            + COLUMN_CONTACT_KEY + " varchar(500), "
            + COLUMN_USER_ID + " varchar(100), "
            + COLUMN_AVATAR + " varchar(3000), "
            + COLUMN_STATUS + " int, "
            + COLUMN_TYPE + " int);";

    private static final String DATABASE_CREATE_IMAGES_TABLE = "create table "
            + TABLE_IMAGES + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MEDIASTORE_ID + " integer, "
            + COLUMN_ORIG_URI + " text, "
            + COLUMN_THUMB_URI + " text, "
            + COLUMN_SERVER_ID + " text, "
            + COLUMN_TITLE + " text, "
            + COLUMN_BUCKET_ID + " text, "
            + COLUMN_STATUS + " int);";

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_FRIENDS_TABLE);
        database.execSQL(DATABASE_CREATE_IMAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(SQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
        onCreate(db);
    }

}
