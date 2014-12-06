package com.freecoders.photobook.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.freecoders.photobook.common.Constants;

import java.util.ArrayList;

/**
 * Created by Alex on 2014-12-03.
 */
public class ImagesDataSource {
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_MEDIASTORE_ID,
            SQLiteHelper.COLUMN_ORIG_URI, SQLiteHelper.COLUMN_THUMB_URI,
            SQLiteHelper.COLUMN_SERVER_ID, SQLiteHelper.COLUMN_TITLE,
            SQLiteHelper.COLUMN_STATUS};
    private Context mContext;

    public ImagesDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
        this.mContext = context;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Save new ImageEntry
    public ImageEntry saveImage(String MediaStoreID, String OrigURI, String ThumbURI,
            String ServerID, String Title, long Status) {
        /*
        Code here
         */
        return new ImageEntry();
    }

    public ImageEntry saveImage(ImageEntry imageEntry) {
        return saveImage(imageEntry.getMediaStoreID(), imageEntry.getOrigUri(),
                imageEntry.getThumbUri(), imageEntry.getServerId(), imageEntry.getTitle(),
                imageEntry.getStatus());
    }

    //Implement updating existing entry with COLUMN_ID
    public Boolean updateImage(ImageEntry image) {
        /*
        Code here
         */
        return true;
    }

    //Implement deleting entry
    public void deleteImageEntry(ImageEntry imageEntry) {
        /*
        Code here
         */
    }

    //Implement requesting all shared images
    public ArrayList<ImageEntry> getSharedImages() {
        ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();
        /*
        Code here
         */
        return images;
    }

    public ArrayList<ImageEntry> getLocalImages(){
        ArrayList<ImageEntry> res = new ArrayList<ImageEntry>();
        ContentResolver cr = mContext.getContentResolver();
        String orderBy = MediaStore.Images.Media._ID + " DESC";
        Cursor cursorImg = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, null, null, orderBy);
        int count = cursorImg.getCount();
        for (int i = 0; i < count; i++) {
            cursorImg.moveToPosition(i);
            String strMediaStoreID = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media._ID));
            String strOrigUri = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media.DATA));
            String strThumbUri = "";
            Cursor cursorThumb = cr.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.Thumbnails.DATA},
                    MediaStore.Images.Thumbnails.IMAGE_ID + "= ?",
                    new String[]{strMediaStoreID}, null);
            if( cursorThumb != null && cursorThumb.getCount() > 0 ) {
                cursorThumb.moveToFirst();
                strThumbUri = cursorThumb.getString(
                        cursorThumb.getColumnIndex( MediaStore.Images.Thumbnails.DATA ));
            }
            ImageEntry imageEntry = new ImageEntry();
            imageEntry.setMediaStoreID(strMediaStoreID);
            imageEntry.setOrigUri(strOrigUri);
            imageEntry.setThumbUri(strThumbUri);
            res.add(imageEntry);
            Log.d(Constants.LOG_TAG, "Loaded image _ID = " + strMediaStoreID + ", " +
                    "origUri = " + strOrigUri + ", thumbUri = " + strThumbUri);
        }
        return res;
    }

    public ArrayList<ImageEntry> getAllImages() {
        ArrayList<ImageEntry> resList = getSharedImages();
        ArrayList<ImageEntry> localList = getLocalImages();
        int pos = 0;
        for (int i = 0; i < localList.size(); i++) {
            while ((pos < resList.size()) && (localList.get(i).getMediaStoreID().
                    compareTo(resList.get(pos).getMediaStoreID()) < 0)) {
                pos++;
            }
            if ((pos < resList.size()) && (localList.get(i).getMediaStoreID().
                    compareTo(resList.get(pos).getMediaStoreID()) != 0)) {
                resList.add(pos, localList.get(i));
            } else if (pos == resList.size()){
                resList.add(localList.get(i));
            }
        }
        return resList;
    }

    private ImageEntry cursorToImageEntry(Cursor cursor) {
        /*
        Code here
         */
        return new ImageEntry();
    }

}
