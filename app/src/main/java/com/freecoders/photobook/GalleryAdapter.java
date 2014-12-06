package com.freecoders.photobook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.etsy.android.grid.util.DynamicHeightImageView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.utils.ImageUtils;
import com.freecoders.photobook.utils.MemoryLruCache;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Alex on 2014-12-03.
 */
public class GalleryAdapter extends ArrayAdapter<ImageEntry> {

    private int mLastPosition = -1;
    private int imageWidth = ImageUtils.dpToPx(100);
    private int maxImageHeight = ImageUtils.dpToPx(1000);
    private MemoryLruCache cache;

    public GalleryAdapter(Context context, int resource, List<ImageEntry> objects) {
        super(context, resource, objects);
        cache = new MemoryLruCache();
    }

    public class ViewHolder {
        int position;
        DynamicHeightImageView imgView;
        ImageView shareImgView;
        TextView textView;
        ProgressBar progressBar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout view;
        ImageEntry imageEntry = getItem(position);
        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.item_gallery, null);
            holder = new ViewHolder();
            holder.textView = (TextView)rowView.findViewById(R.id.txtView);
            holder.imgView = (DynamicHeightImageView)rowView.findViewById(R.id.imgView);
            holder.progressBar = (ProgressBar)rowView.findViewById(R.id.progressBar);
            holder.shareImgView = (ImageView)rowView.findViewById(R.id.imgViewShare);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        Bitmap b;
        if (imageEntry.getThumbUri().isEmpty() == false) {
           b = cache.getBitmap(imageEntry.getThumbUri());
            if (b == null){
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                b = BitmapFactory.decodeFile(imageEntry.getThumbUri(), options);
                cache.put(imageEntry.getThumbUri(), b);
            }
            Log.d(Constants.LOG_TAG, "Loaded image from thumbnail size " + b.getWidth() +
                    ":" + b.getHeight());
        } else {
            b = cache.getBitmap(imageEntry.getOrigUri());
            if (b == null){
                b = ImageUtils.decodeSampledBitmap(imageEntry.getOrigUri(), imageWidth,
                        maxImageHeight);
                cache.put(imageEntry.getOrigUri(), b);
                Log.d(Constants.LOG_TAG, "Loaded image from cache size " + b.getWidth() +
                        ":" + b.getHeight());
            }
            Log.d(Constants.LOG_TAG, "Loaded original image size " + b.getWidth() +
                    ":" + b.getHeight());
        }
        holder.position = position;
        holder.imgView.setHeightRatio(b.getHeight() * 1.00 / b.getWidth());
        holder.imgView.setImageBitmap(b);

        if (position > mLastPosition)
            new PrefetchTask().execute(position + 10);
        else
            new PrefetchTask().execute(position - 10);

        if (imageEntry.getStatus() == imageEntry.INT_STATUS_SHARED) {
            holder.textView.setText(imageEntry.getTitle());
            holder.textView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.shareImgView.setVisibility(View.GONE);
        } else if (imageEntry.getStatus() == imageEntry.INT_STATUS_SHARING){
            holder.textView.setText("Uploading image");
            holder.textView.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.shareImgView.setVisibility(View.VISIBLE);
        } else {
            holder.textView.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.shareImgView.setVisibility(View.VISIBLE);
        }

        mLastPosition = position;

        return rowView;
    }

    private class PrefetchTask extends AsyncTask<Integer, Integer, Boolean> {
        protected Boolean doInBackground(Integer... position) {
            prefetchItems(position[0]);
            return true;
        }
    }

    public void prefetchItems(int pos) {
        if ((pos >= 0) && (pos < this.getCount())) {
            ImageEntry imageEntry = getItem(pos);
            Bitmap b;
            if (imageEntry.getThumbUri().isEmpty() == false) {
                b = cache.getBitmap(imageEntry.getThumbUri());
                if (b == null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    b = BitmapFactory.decodeFile(imageEntry.getThumbUri(), options);
                    cache.put(imageEntry.getThumbUri(), b);
                }
            } else {
                b = cache.getBitmap(imageEntry.getOrigUri());
                if (b == null) {
                    b = ImageUtils.decodeSampledBitmap(imageEntry.getOrigUri(), imageWidth,
                            maxImageHeight);
                    cache.put(imageEntry.getOrigUri(), b);
                    Log.d(Constants.LOG_TAG, "Loaded image from cache size " + b.getWidth() +
                            ":" + b.getHeight());
                }
            }
        }
    }
}
