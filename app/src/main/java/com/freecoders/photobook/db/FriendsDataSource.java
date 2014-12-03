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
    int idColIndex;
    int nameColIndex;
    int userIdColIndex;
    int avatarColIndex;
    int statusColIndex;
    int ContactKeyColIndex;

    public FriendsDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
        Cursor cursor = database.query(dbHelper.TABLE_FRIENDS, null, null, null, null, null, null);
        idColIndex = cursor.getColumnIndex("_id");
        nameColIndex = cursor.getColumnIndex("Name");
        userIdColIndex = cursor.getColumnIndex("UserId");
        avatarColIndex = cursor.getColumnIndex("Avatar");
        statusColIndex = cursor.getColumnIndex("Status");
        ContactKeyColIndex = cursor.getColumnIndex("ContactKey");

    }

    public void close() {
        dbHelper.close();
    }

    public FriendEntry createFriend(String Name, String ContactKey, String UserId,
                String Avatar, int Status) {
        //Add new FriendEntry
        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COLUMN_CONTACT_KEY,ContactKey);
        cv.put(dbHelper.COLUMN_NAME,Name);
        cv.put(dbHelper.COLUMN_USER_ID,UserId);
        cv.put(dbHelper.COLUMN_AVATAR,Avatar);
        cv.put(dbHelper.COLUMN_STATUS,Status);

        database.insert(dbHelper.TABLE_FRIENDS, null, cv);
        return null;
    }

    public void deleteFriendEntry(FriendEntry friendEntry) {
        //Delete FriendEntry
    }

    public FriendEntry getFriendByContactKey(String ContactKey) {
        //Cursor c = database.query(dbHelper.TABLE_FRIENDS, null, null, null, null, null, null);
        //String selection = dbHelper.COLUMN_CONTACT_KEY + " = ?";
        String selection = dbHelper.COLUMN_CONTACT_KEY + " = ?";

        Cursor cursor = database.query(dbHelper.TABLE_FRIENDS,
                null, selection,new String[]{ContactKey} , null, null, null);

        //cursor.moveToFirst();
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        //Get FriendEntry using ContactKey
        return cursorToFriendEntry(cursor);
    }

    public ArrayList<FriendEntry> getFriendsByStatus(int Status) {
        String selection = dbHelper.COLUMN_STATUS + " = ?";

        String orderBy =  SQLiteHelper.COLUMN_NAME + " ASC";

        Cursor cursor = database.query(dbHelper.TABLE_FRIENDS,
                null, selection,new String[]{String.valueOf(Status)} , null, null, orderBy);

        ArrayList<FriendEntry> listFriends = new ArrayList<FriendEntry>();

        if (cursor == null) {
            return listFriends;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return listFriends;
        }

        do{
            listFriends.add(cursorToFriendEntry(cursor));
        }while (cursor.moveToNext());


        return listFriends;
    }

    public ArrayList<FriendEntry> getAllFriends() {

        String orderBy =  SQLiteHelper.COLUMN_NAME + " ASC";

        Cursor cursor = database.query(dbHelper.TABLE_FRIENDS, null, null,null , null, null,
                orderBy);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        ArrayList<FriendEntry> listFriends = new ArrayList<FriendEntry>();

        do{
            listFriends.add(cursorToFriendEntry(cursor));
        }while (cursor.moveToNext());


        return listFriends;
    }

    public int setUserId(String ContactKey, String UserId) {
        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COLUMN_USER_ID,UserId);

        int updCount = database.update(dbHelper.TABLE_FRIENDS, cv, "_id = ?",new String[] {ContactKey});
        //Update UserId using ContactKey
        return 0;
    }

    public int updateFriend(FriendEntry friend) {
        //Update existing friend using _ID

        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COLUMN_CONTACT_KEY,friend.getContactKey());
        cv.put(dbHelper.COLUMN_NAME,friend.getName());
        cv.put(dbHelper.COLUMN_USER_ID,friend.getUserId());
        cv.put(dbHelper.COLUMN_AVATAR,friend.getAvatar());
        cv.put(dbHelper.COLUMN_STATUS,friend.getStatus());
        // обновляем по id
        int updCount = database.update(dbHelper.TABLE_FRIENDS, cv, "_id = ?",new String[] { String.valueOf(friend.getId())});
        //Log.d(Constants.LOG_TAG, "Updated friend with number "+friend.getContactKey());
        return updCount;
    }

    private FriendEntry cursorToFriendEntry(Cursor cursor) {

        //Log.d(Constants.LOG_TAG,""+cursor.getString(ContactKeyColIndex));
        FriendEntry friend = new FriendEntry();
        friend.setId(cursor.getInt(idColIndex));
        friend.setName(cursor.getString(nameColIndex));
        friend.setUserId(cursor.getString(userIdColIndex));
        friend.setAvatar(cursor.getString(avatarColIndex));
        friend.setStatus(cursor.getInt(statusColIndex));
        friend.setContactKey(cursor.getString(ContactKeyColIndex));

        return friend;
    }
}