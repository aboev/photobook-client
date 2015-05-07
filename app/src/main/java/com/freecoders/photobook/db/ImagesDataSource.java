package com.freecoders.photobook.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.utils.ImageUtils;

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
        cursor.close();
    }

    public void close() {
        dbHelper.close();
    }

    // Save new ImageEntry
    //public ImageEntry saveImage(String MediaStoreID, String OrigURI, String ThumbURI,
    //        String ServerID, String Title, long Status) {return new ImageEntry();}

    public ImageEntry saveImage(ImageEntry imageEntry) {
        if (imageEntry.getId() != -1) {
            updateImage(imageEntry);
            return imageEntry;
        }
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

    public ImageEntry getImageByID(String strID) {
        String selection = dbHelper.COLUMN_ID + " = ?";
        String orderBy = dbHelper.COLUMN_MEDIASTORE_ID + " DESC";
        Cursor cursor = database.query(dbHelper.TABLE_IMAGES,
                null, selection,new String[]{strID} ,
                null, null, orderBy);

        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        ImageEntry image = cursorToImageEntry(cursor);
        cursor.close();
        return image;
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
        ImageEntry image = cursorToImageEntry(cursor);
        cursor.close();
        return image;
    }

    //Implement requesting all shared images
    public ArrayList<ImageEntry> getSharedImages(String strBucketId, Integer status) {
        if (status == null) status = ImageEntry.INT_STATUS_SHARED;
        String[] selectionArgs = new String[]{String.valueOf(status)};
        String selection = dbHelper.COLUMN_STATUS + " = ?";
        if (strBucketId != null) {
            selection = selection + " and " + dbHelper.COLUMN_BUCKET_ID + " = ?";
            selectionArgs = new String[]{String.valueOf(status), strBucketId};
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
        cursor.close();
        return images;
    }

    public ArrayList<ImageEntry> getLocalImages(String strBucketID, Integer status){
        if (status != null) return new ArrayList<ImageEntry>();
        ArrayList<ImageEntry> res = new ArrayList<ImageEntry>();
        ContentResolver cr = mContext.getContentResolver();
        String orderBy = MediaStore.Images.Media._ID + " DESC";
        String strSelection = null;
        if (strBucketID != null)
            strSelection = MediaStore.Images.ImageColumns.BUCKET_ID
                    + " = '" + strBucketID + "'";
        String[] projection = new String[]{
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.ImageColumns.BUCKET_ID};
        Cursor cursorImg = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, strSelection, null, orderBy);
        int count = cursorImg.getCount();
        for (int i = 0; i < count; i++) {
            cursorImg.moveToPosition(i);
            String strMediaStoreID = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media._ID));
            String strOrigUri = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.Media.DATA));
            String strBucketId = cursorImg.getString(cursorImg.getColumnIndex(
                    MediaStore.Images.ImageColumns.BUCKET_ID));
            ImageEntry imageEntry = new ImageEntry();
            imageEntry.setMediaStoreID(strMediaStoreID);
            imageEntry.setOrigUri(strOrigUri);
            imageEntry.setBucketId(strBucketId);
            imageEntry.setThumbUri(null);
            res.add(imageEntry);
        }
        cursorImg.close();
        new ThumbLoaderTask(res).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return res;
    }

    public String getThumbURI(String strMediaStoreID) {
        ContentResolver cr = mContext.getContentResolver();
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
        cursorThumb.close();
        return strThumbUri;
    }

    public class ThumbLoaderTask extends AsyncTask<Integer, Integer, Boolean> {
        private ArrayList<ImageEntry> imageList;

        public ThumbLoaderTask(ArrayList<ImageEntry> imageList) {
            this.imageList = imageList;
        }

        protected Boolean doInBackground(Integer... position) {
            if (imageList != null)
                for (int i = 0; i < imageList.size(); i++) {
                    String strThumbURI = getThumbURI(imageList.get(i).getMediaStoreID());
                    imageList.get(i).setThumbUri(strThumbURI);
                }
            return true;
        }
    }

    public ArrayList<ImageEntry> getImageList(String strBucketID, Integer status) {
        ArrayList<ImageEntry> resList = getSharedImages(strBucketID, status);
        ArrayList<ImageEntry> localList = getLocalImages(strBucketID, status);
        int pos = 0;
        for (int i = 0; i < localList.size(); i++) {
            while ((pos < resList.size()) && (localList.get(i).getMediaStoreID().
                    compareTo(resList.get(pos).getMediaStoreID()) < 0)) {
                pos++;
            }
            if ((pos < resList.size()) && !localList.get(i).getMediaStoreID().
                    equals(resList.get(pos).getMediaStoreID())) {
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
            res.add(b);
        }
        imageCursor.close();
        return res;
    }

    public class BucketEntry {
        public String strBucketName;
        public String strBucketId;
        public String strTitleImageUrl;
    }

}
