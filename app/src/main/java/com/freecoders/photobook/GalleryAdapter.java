package com.freecoders.photobook;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.Image;
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
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.db.ImageEntry;
import com.freecoders.photobook.utils.ImageUtils;
import com.freecoders.photobook.utils.MemoryLruCache;

import org.w3c.dom.Text;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.freecoders.photobook.utils.FileUtils.getRealPathFromURI;

/**
 * Created by Alex on 2014-12-03.
 */
public class GalleryAdapter extends ArrayAdapter<ImageEntry> {
    private static String LOG_TAG = "GalleryAdapter";    

    private int imageWidth = 100;
    private int maxImageHeight = 1000;
    private MemoryLruCache cache;

    public GalleryAdapter(Context context, int resource, List<ImageEntry> objects) {
        super(context, resource, objects);
        cache = new MemoryLruCache();
    }

    public class ViewHolder {
        int position = -1;
        DynamicHeightImageView imgView;
        ImageView shareImgView;
        TextView textView;
        ImageView newCommentImgView;
        TextView newCommentTextView;
        ProgressBar progressBar;
        int status = ImageEntry.INT_STATUS_DEFAULT;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout view;
        ImageEntry imageEntry = getItem(position);
        if (imageEntry.getStatus() == ImageEntry.INT_STATUS_BUCKET)
            return getViewForBucket(position, convertView, parent);
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
            holder.newCommentImgView = (ImageView)rowView.findViewById(R.id.imgNewComment);
            holder.newCommentTextView =
                    (TextView)rowView.findViewById(R.id.txtViewNewCommentCount);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
            if (checkHolderFresh(holder, position))
                return convertView;
        }

        // Prepare image frame size, but set invisible until image is loaded
        initImageFrame(holder, position);
        if (holder.position != position) {
            // ViewHolder contains stale image - need to refresh
            holder.position = position;
            holder.imgView.setImageResource(android.R.color.transparent);
            int orientation = ImageUtils.getExifOrientation(imageEntry.getOrigUri());
            new ImageLoadTask(holder, position, orientation,
                false, null).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            // ViewHolder contains actual image - only need to make image frame visible
            holder.position = position;
            populateImageFrame(holder, position);
        }

        if ((position + 5) < getCount() ) {
            int orientation = ImageUtils.getExifOrientation(getItem(position + 5).getOrigUri());
            new ImageLoadTask(holder, position + 5, orientation, true, null).
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        if ((position - 5) >= 0 ) {
            int orientation = ImageUtils.getExifOrientation(getItem(position - 5).getOrigUri());
            new ImageLoadTask(holder, position - 5, orientation, true, null).
                    executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        return rowView;
    }

    public View getViewForBucket(int position, View convertView, ViewGroup parent)
    {
        View rowView = convertView;
        ViewHolder holder = null;
        ImageEntry imageEntry = getItem(position);

        if(rowView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.item_gallery_bucket, null);
            holder = new ViewHolder();
            holder.textView = (TextView)rowView.findViewById(R.id.txtView);
            holder.imgView = (DynamicHeightImageView)rowView.findViewById(R.id.imgView);
            rowView.setTag(holder);
        } else
            holder = (ViewHolder) rowView.getTag();

        final TextView txtView = holder.textView;
        txtView.setVisibility(View.INVISIBLE);
        txtView.setText(imageEntry.getTitle());
        holder.position = position;
        holder.imgView.setImageResource(android.R.color.transparent);
        int orientation = ImageUtils.getExifOrientation(imageEntry.getOrigUri());

        new ImageLoadTask(holder, position, orientation,
                    false, new CallbackInterface() {
            @Override
            public void onResponse(Object obj) {
                Photobook.getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        txtView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return rowView;
    }

    public class ImageLoadTask extends AsyncTask<Integer, Integer, Boolean> {
        private int mPosition;
        private String mImgUri;
        private final ViewHolder mViewHolder;
        private final ImageEntry mImageEntry;
        private Boolean mBoolPrefetch;
        private int orientation = 0;
        private CallbackInterface onFinish;

        public ImageLoadTask(ViewHolder holder, int position, int orientation,
                Boolean boolPrefetch, CallbackInterface onFinish){
            this.mViewHolder = holder;
            this.mPosition = position;
            this.mImageEntry = getItem(position);
            this.orientation = orientation;
            this.onFinish = onFinish;
            mImgUri = mImageEntry.getThumbUri();
            if (mImageEntry.getThumbUri().isEmpty() == true) {
                mImgUri = mImageEntry.getOrigUri();
                Log.d(LOG_TAG, "No thumb for " + mImgUri);
            }
            mBoolPrefetch = boolPrefetch;

            if (!boolPrefetch) {
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mImgUri, options);
                double ratio;
                Log.d(LOG_TAG, "Orientation = " + orientation + " for image " + mImgUri );
                if ((orientation == 90) || (orientation == 270))
                    ratio = options.outWidth * 1.0 / options.outHeight;
                else
                    ratio = options.outHeight * 1.0 / options.outWidth;
                if (mImageEntry.getRatio() != -1)
                    mViewHolder.imgView.setHeightRatio(mImageEntry.getRatio());
                else
                    mViewHolder.imgView.setHeightRatio(ratio);
            }
        }

        protected Boolean doInBackground(Integer... position) {
            Bitmap b = null;
            if ((mBoolPrefetch == true) || (mViewHolder.position == mPosition)) {
                // Prefetch image or load current image
                b = cache.getBitmap(mImgUri);
                if (b == null) {
                    b = ImageUtils.decodeSampledBitmap(mImgUri,
                            ImageUtils.dpToPx(imageWidth), ImageUtils.dpToPx(maxImageHeight));
                    if (b != null)
                        cache.putBitmap(mImgUri, b);
                }
            }
            if ((mViewHolder.position == mPosition) && (mBoolPrefetch == false)) {
                // Set rotated image and populate image frame
                final Bitmap bitmap = b;
                Photobook.getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((orientation == 90) || (orientation == 270)) {
                            Matrix matrix = new Matrix();
                            matrix.postRotate(orientation);
                            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            mViewHolder.imgView.setImageBitmap(rotatedBitmap);
                        } else
                            mViewHolder.imgView.setImageBitmap(bitmap);
                    }
                });
                if (onFinish == null) populateImageFrame(mViewHolder, mPosition);
            }
            if (onFinish != null) onFinish.onResponse(null);
            return true;
        }
    }

    private void initImageFrame(ViewHolder holder, int position) {
        // Set the title, progress bar(for sharing in-progress), comment count, etc.
        ImageEntry imageEntry = getItem(position);
        holder.shareImgView.setVisibility(View.GONE);
        holder.textView.setText(imageEntry.getTitle());
        holder.status = imageEntry.getStatus();
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
        // Check new comments count
        String strCommentCount = getUnreadCommentCount(imageEntry.getServerId());
        holder.newCommentTextView.setText(strCommentCount);
        if (!strCommentCount.isEmpty()) {
            holder.newCommentImgView.setVisibility(View.INVISIBLE);
            holder.newCommentTextView.setVisibility(View.INVISIBLE);
        } else {
            holder.newCommentImgView.setVisibility(View.GONE);
            holder.newCommentTextView.setVisibility(View.GONE);
        }
    }

    private void populateImageFrame (ViewHolder holder, int position) {
        // Adjust the visibility of title, progress bar, comment count, etc.
        if (getCount() <= position) return;
        final ImageEntry mImageEntry = getItem(position);
        final ViewHolder mViewHolder = holder;
        Photobook.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mImageEntry.getStatus() == mImageEntry.INT_STATUS_SHARED) {
                    mViewHolder.textView.setVisibility(View.VISIBLE);
                    mViewHolder.shareImgView.setVisibility(View.VISIBLE);
                } else if (mImageEntry.getStatus() == mImageEntry.INT_STATUS_SHARING) {
                    mViewHolder.textView.setText(R.string.edit_uploading_image);
                    mViewHolder.textView.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.textView.setVisibility(View.GONE);
                }
                if (!mViewHolder.newCommentTextView.getText().toString().isEmpty()) {
                    mViewHolder.newCommentImgView.setVisibility(View.VISIBLE);
                    mViewHolder.newCommentTextView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private String getUnreadCommentCount (String strImageID) {
        String strCount = "";
        if (Photobook.getPreferences().unreadImagesMap.containsKey(strImageID) &&
                Photobook.getPreferences().unreadImagesMap.get(strImageID)>0) {
            int intCommentCount = Photobook.getPreferences().
                    unreadImagesMap.get(strImageID);
            if (intCommentCount < 10)
                strCount = Photobook.getPreferences().unreadImagesMap.get(strImageID).toString();
            else
                strCount = ">9";
        }
        return strCount;
    }

    private Boolean checkHolderFresh (ViewHolder holder, int position){
        ImageEntry image = getItem(position);
        Boolean boolFresh = holder.textView.getText().toString().equals(image.getTitle())
                && holder.position == position
                && holder.newCommentTextView.getText().toString().equals(
                    getUnreadCommentCount(image.getServerId()))
                && holder.status == image.getStatus();
        return boolFresh;
    }
}
