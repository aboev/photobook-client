package com.freecoders.photobook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.freecoders.photobook.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Alex on 2014-11-27.
 */
public class FriendsDataSource {

    // Database fields
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = { SQLiteHelper.COLUMN_ID,SQLiteHelper.COLUMN_NAME,
            SQLiteHelper.COLUMN_CONTACT_KEY, SQLiteHelper.COLUMN_USER_ID,
            SQLiteHelper.COLUMN_AVATAR, SQLiteHelper.COLUMN_STATUS};

    public FriendsDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public FriendEntry createFriend(String Name, String ContactKey, String UserId,
                String Avatar, int Status) {
        //Add new FriendEntry
        return null;
    }

    public void deleteFriendEntry(FriendEntry friendEntry) {
        //Delete FriendEntry
    }

    public FriendEntry getFriendByContactKey(String ContactKey) {
        //Get FriendEntry using ContactKey
        return null;
    }

    public ArrayList<FriendEntry> getFriendsByStatus(int Status) {
        //Get friends list with given Status
        return null;
    }

    public ArrayList<FriendEntry> getAllFriends() {
        //Return all friends
        return null;
    }

    public int setUserId(String ContactKey, String UserId) {
        //Update UserId using ContactKey
        return 0;
    }

    public int updateFriend(FriendEntry friend) {
        //Update existing friend using _ID
        return 0;
    }

    private FriendEntry cursorToFriendEntry(Cursor cursor) {
        //Move cursor
        return null;
    }
}