package com.freecoders.photobook.common;

import android.app.Application;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import com.freecoders.photobook.FriendsFragmentTab;
import com.freecoders.photobook.MainActivity;
import com.freecoders.photobook.R;
import com.freecoders.photobook.db.FriendsDataSource;
import com.freecoders.photobook.db.ImagesDataSource;
import com.freecoders.photobook.utils.DiskLruBitmapCache;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.IOException;

/**
 * Created by Alex on 2014-11-30.
 */
public class Photobook extends Application {
    private static FriendsDataSource friendsDataSource;
    private static ImagesDataSource imagesDataSource;
    private static FriendsFragmentTab mFriendsTab;
    private static MainActivity mActivity;
    private static Preferences mPreferences;
    private static DiskLruBitmapCache mAvatarDiskLruCache;
    private static DiskLruBitmapCache mImageDiskLruCache;

    @Override
    public void onCreate() {
        super.onCreate();

        friendsDataSource = new FriendsDataSource(this);
        friendsDataSource.open();
        imagesDataSource = new ImagesDataSource(this);
        imagesDataSource.open();
        mPreferences = new Preferences(this);
        mPreferences.loadPreferences();
        try {
            mAvatarDiskLruCache = new DiskLruBitmapCache(this, "AvatarsDiskCache",
                    2000000, Bitmap.CompressFormat.JPEG, 100);
            mImageDiskLruCache = new DiskLruBitmapCache(this, "ImageDiskCache",
                    20000000, Bitmap.CompressFormat.JPEG, 100);
        } catch (IOException e) {
            mAvatarDiskLruCache = null;
            mImageDiskLruCache = null;
            Log.d(Constants.LOG_TAG, "Failed to initialize disk cache");
        }
    }

    public final static FriendsDataSource getFriendsDataSource(){
        return friendsDataSource;
    }

    public final static ImagesDataSource getImagesDataSource(){
        return imagesDataSource;
    }

    public final static void setMainActivity(MainActivity activity){
        mActivity = activity;
    }

    public final static MainActivity getMainActivity(){
        return mActivity;
    }

    public final static void setFriendsFragmentTab(FriendsFragmentTab tab){
        mFriendsTab = tab;
    }

    public final static FriendsFragmentTab getFriendsFragmentTab(){
        return mFriendsTab;
    }

    public final static Preferences getPreferences() {return mPreferences;}

    public final static DiskLruBitmapCache getAvatarDiskLruCache() {return mAvatarDiskLruCache;}

    public final static DiskLruBitmapCache getImageDiskLruCache() {return mImageDiskLruCache;}
}
