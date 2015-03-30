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
    private static String LOG_TAG = "ImagesDataSource";
    
    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;
    private String[] allColumns = {SQLiteHelper.COLUMN_ID, SQLiteHelper.COLUMN_MEDIASTORE_ID,
            SQLiteHelper.COLUMN_ORIG_URI, SQLiteHelper.COLUMN_THUMB_URI,
            SQLiteHelper.COLUMN_SERVER_ID, SQLiteHelper.COLUMN_TITLE,
            SQLiteHelper.COLUMN_BUCKET_ID,
            SQLiteHelper.COLUMN_STATUS};
    private Context mContext;

    int idColIndex;
    int mediaIdColIndex;
    int oriUriColIndex;
    int thumbUriColIndex;
    int serverIdColIndex;
    int bucketIdColIndex;
    int titleColIndex;
    int statusColIndex;

    public ImagesDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
        this.mContext = context;
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();

        Cursor cursor = database.query(dbHelper.TABLE_IMAGES, null, null, null, null, null, null);
        idColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ID);
        mediaIdColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_MEDIASTORE_ID);
        oriUriColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_ORIG_URI);
        thumbUriColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_THUMB_URI);
        serverIdColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_SERVER_ID);
        bucketIdColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_BUCKET_ID);
        titleColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_TITLE);
        statusColIndex = cursor.getColumnIndex(SQLiteHelper.COLUMN_STATUS);
    }

    public void close() {
        dbHelper.close();
    }

    // Save new ImageEntry
    //public ImageEntry saveImage(String MediaStoreID, String OrigURI, String ThumbURI,
    //        String ServerID, String Title, long Status) {return new ImageEntry();}

    public ImageEntry saveImage(ImageEntry imageEntry) {
        ContentValues cv = new ContentValues();

        cv.put(dbHelper.COLUMN_MEDIASTORE_ID,imageEntry.getMediaStoreID());
        cv.put(dbHelper.COLUMN_ORIG_URI,imageEntry.getOrigUri());
        cv.put(dbHelper.COLUMN_THUMB_URI,imageEntry.getThumbUri());
        cv.put(dbHelper.COLUMN_SERVER_ID,imageEntry.getServerId());
        cv.put(dbHelper.COLUMN_BUCKET_ID,imageEntry.getBucketId());
        cv.put(dbHelper.COLUMN_TITLE,imageEntry.getTitle());
        cv.put(dbHelper.COLUMN_STATUS,imageEntry.getStatus());

        imageEntry.setId(database.insert(dbHelper.TABLE_IMAGES, null, cv));
        Log.d(LOG_TAG,"Last entry ID: "+imageEntry.getId());

        return imageEntry;
    }

    //Implement updating existing entry with COLUMN_ID
    public Boolean updateImage(ImageEntry image) {

        ContentValues cv = new ContentValues();
        cv.put(dbHelper.COLUMN_MEDIASTORE_ID,image.getMediaStoreID());
        cv.put(dbHelper.COLUMN_ORIG_URI,image.getOrigUri());
        cv.put(dbHelper.COLUMN_THUMB_URI,image.getThumbUri());
        cv.put(dbHelper.COLUMN_SERVER_ID,image.getServerId());
        cv.put(dbHelper.COLUMN_BUCKET_ID,image.getBucketId());
        cv.put(dbHelper.COLUMN_TITLE,image.getTitle());
        cv.put(dbHelper.COLUMN_STATUS,image.getStatus());
        // обновляем по id
        int updCount = database.update(dbHelper.TABLE_IMAGES, cv, "_id = ?",new String[] { String.valueOf(image.getId())});
        return true;
    }

    //Implement deleting entry
    public void deleteImageEntry(ImageEntry imageEntry) {
        database.delete(dbHelper.TABLE_IMAGES,"_id = ?",new String[] {String.valueOf(imageEntry.getId())});
    }

    //Implement requesting all shared images
    public ArrayList<ImageEntry> getSharedImages(String strBucketId) {
        String selection = dbHelper.COLUMN_STATUS + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(ImageEntry.INT_STATUS_SHARED)};
        if (strBucketId != null) {
            selection = selection + " and " + dbHelper.COLUMN_BUCKET_ID + " = ?";
            selectionArgs = new String[]{String.valueOf(ImageEntry.INT_STATUS_SHARED),
                strBucketId};
        }
        String orderBy = dbHelper.COLUMN_MEDIASTORE_ID + " DESC";
        Cursor cursor = database.query(dbHelper.TABLE_IMAGES,
                null, selection, selectionArgs,
                null, null, orderBy);

        ArrayList<ImageEntry> images = new ArrayList<ImageEntry>();

        if (cursor == null) {
            return images;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return images;
        }

        do{
            images.add(cursorToImageEntry(cursor));
        }while (cursor.moveToNext());

        return images;
    }

    public ImageEntry getImageByServerID(String strServerID) {

        String selection = dbHelper.COLUMN_SERVER_ID + " = ?";
        String orderBy = dbHelper.COLUMN_MEDIASTORE_ID + " DESC";
        Cursor cursor = database.query(dbHelper.TABLE_IMAGES,
                null, selection,new String[]{strServerID} ,
                null, null, orderBy);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }

        return cursorToImageEntry(cursor);
    }

    public ArrayList<ImageEntry> getLocalImages(String strBucketID){
        ArrayList<ImageEntry> res = new ArrayList<ImageEntry>();
        ContentResolver cr = mContext.getContentResolver();
        String orderBy = MediaStore.Images.Media._ID + " DESC";
        String strSelection = null;
        if (strBucketID != null)
            strSelection = MediaStore.Images.ImageColumns.BUCKET_ID
                    + " = '" + strBucketID + "'";
        Cursor cursorImg = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, strSelection, null, orderBy);
        int count = cursorImg.getCount();
        for (int i = 0; i < count; i++) {
            cursorImg.moveToPosition(i);
            String strMediaStoreID = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media._ID));
            String strOrigUri = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media.DATA));
            String strBucketId = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.ImageColumns.BUCKET_ID));
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
            imageEntry.setBucketId(strBucketId);
            imageEntry.setThumbUri(strThumbUri);
            res.add(imageEntry);
            //Log.d(LOG_TAG, "Loaded image _ID = " + strMediaStoreID + ", " +
            //        "origUri = " + strOrigUri + ", thumbUri = " + strThumbUri);
        }
        return res;
    }

    public ArrayList<ImageEntry> getAllImages(String strBucketID) {
        ArrayList<ImageEntry> resList = getSharedImages(strBucketID);
        ArrayList<ImageEntry> localList = getLocalImages(strBucketID);
        int pos = 0;
        for (int i = 0; i < localList.size(); i++) {
            while ((pos < resList.size()) && (localList.get(i).getMediaStoreID().
                    compareTo(resList.get(pos).getMediaStoreID()) < 0)) {
                pos++;
            }
            if ((pos < resList.size()) && (localList.get(i).getMediaStoreID().
                    equals(resList.get(pos).getMediaStoreID()) == false)) {
                resList.add(pos, localList.get(i));
            } else if (pos == resList.size()){
                resList.add(localList.get(i));
            }
        }
        return resList;
    }

    private ImageEntry cursorToImageEntry(Cursor cursor) {

        //Log.d(LOG_TAG,""+cursor.getString(ContactKeyColIndex));
        ImageEntry image = new ImageEntry();
        image.setId(cursor.getInt(idColIndex));
        image.setMediaStoreID(cursor.getString(mediaIdColIndex));
        image.setOrigUri(cursor.getString(oriUriColIndex));
        image.setThumbUri(cursor.getString(thumbUriColIndex));
        image.setServerId(cursor.getString(serverIdColIndex));
        image.setBucketId(cursor.getString(bucketIdColIndex));
        image.setTitle(cursor.getString(titleColIndex));
        image.setStatus(cursor.getInt(statusColIndex));

        return image;
    }

    public ArrayList<BucketEntry> getBuckets() {
        ArrayList<BucketEntry> res = new ArrayList<BucketEntry>();
        ContentResolver cr = mContext.getContentResolver();
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String[] projection = new String[]{
                MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATA
        };

        String BUCKET_ORDER_BY = MediaStore.Images.Media.DATE_MODIFIED + " DESC";
        String BUCKET_GROUP_BY = "1) GROUP BY 1,(2";

        Cursor imageCursor = cr.query(images,
                projection, // Which columns to return
                BUCKET_GROUP_BY,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                BUCKET_ORDER_BY        // Ordering
        );

        for (int i = 0; i < imageCursor.getCount(); i++)
        {
            BucketEntry b = new BucketEntry();
            imageCursor.moveToPosition(i);
            int bucketColumnIndex = imageCursor.
                    getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int bucketIdColumnIndex = imageCursor.
                    getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            int dataColumnIndex = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
            b.strBucketName = imageCursor.getString(bucketColumnIndex);
            b.strTitleImageUrl = imageCursor.getString(dataColumnIndex);
            b.strBucketId = imageCursor.getString(bucketIdColumnIndex);

        }
        return res;
    }

    public class BucketEntry {
        public String strBucketName;
        public String strBucketId;
        public String strTitleImageUrl;
    }

}
