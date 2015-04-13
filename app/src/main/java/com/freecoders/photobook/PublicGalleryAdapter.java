package com.freecoders.photobook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.util.DynamicHeightImageView;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.ImageUtils;
import com.freecoders.photobook.utils.MemoryLruCache;

import java.util.List;

/**
 * Created by Alex on 2014-12-03.
 */
public class PublicGalleryAdapter extends ArrayAdapter<ImageJson> {
    private static String LOG_TAG = "PublicGalleryAdapter";

    private MemoryLruCache cache;
    private ImageLoader mImageLoader;

    public PublicGalleryAdapter(Context context, int resource, List<ImageJson> objects) {
        super(context, resource, objects);
        cache = new MemoryLruCache();
        if (Photobook.getAvatarDiskLruCache() != null) {
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    Photobook.getImageDiskLruCache());
        } else {
            ImageLoader.ImageCache memoryCache = new MemoryLruCache();
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    memoryCache);
        }
    }

    public class ViewHolder {
        int position = -1;
        DynamicHeightImageView imgView;
        TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ImageJson image = getItem(position);

        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.item_gallery_public, null);
            holder = new ViewHolder();
            holder.textView = (TextView)rowView.findViewById(R.id.txtView);
            holder.imgView = (DynamicHeightImageView)rowView.findViewById(R.id.imgView);
            rowView.setTag(holder);
        } else
            holder = (ViewHolder) rowView.getTag();

        final TextView txtView = holder.textView;
        txtView.setVisibility(View.INVISIBLE);
        if (image.title != null) txtView.setText(image.title);
        else txtView.setText("");
        holder.position = position;
        holder.imgView.setImageResource(android.R.color.transparent);
        holder.imgView.setTag(position);
        holder.imgView.setHeightRatio(1.0);

        if (image.url_small != null)
            mImageLoader.get(image.url_small,
                    new FeedAdapter.ImageListener(position, holder.imgView,
                        new CallbackInterface() {
                            @Override
                            public void onResponse(Object obj) {
                                txtView.setVisibility(View.VISIBLE);
                            }
                    }));

        return rowView;
    }

}
