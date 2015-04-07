package com.freecoders.photobook.utils;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Constants;

/**
 * Created by Alex on 2014-11-28.
 */
public class MemoryLruCache
        extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {
    private static String LOG_TAG = "MemoryLruCache";

    public MemoryLruCache() {
        this(getDefaultLruCacheSize());
    }

    public MemoryLruCache(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight() / 1024;
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d(LOG_TAG, "Putting cache value for " + url + ", control pixel " +
                bitmap.getPixel(10,10));
        put(url, bitmap);
    }

    public static int getDefaultLruCacheSize() {
        final int maxMemory =
                (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;

        return cacheSize;
    }
}
