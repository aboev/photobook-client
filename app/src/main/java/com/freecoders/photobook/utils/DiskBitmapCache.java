package com.freecoders.photobook.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Constants;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * Created by Alex on 2014-11-28.
 */
public class DiskBitmapCache extends DiskBasedCache implements ImageLoader.ImageCache {

    File cacheDir;

    public DiskBitmapCache(File rootDirectory, int maxCacheSizeInBytes) {
        super(rootDirectory, maxCacheSizeInBytes);
    }

    public DiskBitmapCache(File cacheDir) {
        super(cacheDir);
        this.cacheDir = cacheDir;
    }

    public Bitmap getBitmap(String url) {
        Log.d(Constants.LOG_TAG, "DiskBitmapCache getBitmap for " + url);
        final String volleyFileName = getFilenameForKey(url);

        if (cacheDir.listFiles() != null)
            for (File file : cacheDir.listFiles()) {
                if (file.getName().equals(volleyFileName))
                    return BitmapFactory.decodeFile(file.getName());
            }

        return null;
    }

    public void putBitmap(String url, Bitmap bitmap) {
        final Entry entry = new Entry();

        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(buffer);
        entry.data = buffer.array();

        put(url, entry);
    }

    // Volley creates a filename for the url with the following function, so we'll use the same function
    // for translating the url back to said filename
    private String getFilenameForKey(String key) {
        int firstHalfLength = key.length() / 2;
        String localFilename = String.valueOf(key.substring(0, firstHalfLength).hashCode());
        localFilename += String.valueOf(key.substring(firstHalfLength).hashCode());
        return localFilename;
    }
}