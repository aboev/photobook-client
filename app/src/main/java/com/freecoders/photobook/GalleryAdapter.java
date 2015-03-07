package com.freecoders.photobook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
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
import com.freecoders.photobook.common.Photobook;
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

        holder.position = position;
        holder.imgView.setImageResource(android.R.color.transparent);
        new ImageLoadTask(holder, position, false).execute();
        if ((position + 10) < getCount() )
            new ImageLoadTask(holder, position + 10, true).execute();

        holder.shareImgView.setVisibility(View.GONE);
        if (imageEntry.getStatus() == imageEntry.INT_STATUS_SHARED) {
            holder.progressBar.setVisibility(View.GONE);
            holder.textView.setVisibility(View.INVISIBLE);
            holder.textView.setGravity(Gravity.CENTER);
        } else if (imageEntry.getStatus() ==  imageEntry.INT_STATUS_SHARING){
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.textView.setVisibility(View.INVISIBLE);
            holder.textView.setGravity(Gravity.LEFT);
        } else {
            holder.progressBar.setVisibility(View.GONE);
            holder.textView.setVisibility(View.GONE);
        }

        mLastPosition = position;

        return rowView;
    }

    public class ImageLoadTask extends AsyncTask<Integer, Integer, Boolean> {
        private int mPosition;
        private String mImgUri;
        private final ViewHolder mViewHolder;
        private final ImageEntry mImageEntry;
        private Boolean mBoolPrefetch;

        public ImageLoadTask(ViewHolder holder, int position, Boolean boolPrefetch){
            this.mViewHolder = holder;
            this.mPosition = position;
            this.mImageEntry = getItem(position);
            mImgUri = mImageEntry.getThumbUri();
            if (mImageEntry.getThumbUri().isEmpty() == true) {
                mImgUri = mImageEntry.getOrigUri();
                Log.d(Constants.LOG_TAG, "Not thumb for " + mImgUri);
            }
            mBoolPrefetch = boolPrefetch;

            if (!boolPrefetch) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mImgUri, options);
                double ratio = options.outHeight * 1.0 / options.outWidth;
                mViewHolder.imgView.setHeightRatio(ratio);
            }
        }

        protected Boolean doInBackground(Integer... position) {
            Bitmap b = null;
            if ((mBoolPrefetch == true) || (mViewHolder.position == mPosition)) {
                b = cache.getBitmap(mImgUri);
                if (b == null) {
                    b = ImageUtils.decodeSampledBitmap(mImgUri,
                            imageWidth, maxImageHeight);
                    if (b != null)
                        cache.putBitmap(mImgUri, b);
                }
            }
            if ((mViewHolder.position == mPosition) && (mBoolPrefetch == false)) {
                final Bitmap bitmap = b;
                Photobook.getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mViewHolder.imgView.setImageBitmap(bitmap);
                        mViewHolder.textView.setText(mImageEntry.getTitle());
                        if (mImageEntry.getStatus() == mImageEntry.INT_STATUS_SHARED) {
                            mViewHolder.textView.setVisibility(View.VISIBLE);
                        } else if (mImageEntry.getStatus() == mImageEntry.INT_STATUS_SHARING) {
                            mViewHolder.textView.setText("Uploading image");
                            mViewHolder.textView.setVisibility(View.VISIBLE);
                        } else {
                            mViewHolder.shareImgView.setVisibility(View.VISIBLE);
                            mViewHolder.textView.setVisibility(View.GONE);
                        }
                    }
                });
            }
            return true;
        }
    }
}
