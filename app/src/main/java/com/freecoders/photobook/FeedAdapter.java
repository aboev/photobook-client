package com.freecoders.photobook;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.etsy.android.grid.util.DynamicHeightImageView;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.FeedEntryJson;
import com.freecoders.photobook.network.FeedLoader;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.MemoryLruCache;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Alex on 2014-12-10.
 */
public class FeedAdapter  extends ArrayAdapter<FeedEntryJson> {

    private ImageLoader mImageLoader;
    private ImageLoader mAvatarLoader;
    private ArrayList<FeedEntryJson> mFeedList;

    public FeedAdapter(Context context, int resource, ArrayList<FeedEntryJson> feedList) {
        super(context, resource, feedList);
        this.mFeedList = feedList;

        if (Photobook.getAvatarDiskLruCache() != null) {
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    Photobook.getImageDiskLruCache());
            mAvatarLoader= new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    Photobook.getAvatarDiskLruCache());
        } else {
            ImageLoader.ImageCache memoryCache = new MemoryLruCache();
            ImageLoader.ImageCache memoryCacheAvatar = new MemoryLruCache();
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    memoryCache);
            mAvatarLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    memoryCacheAvatar);
        }
    }

    public class ViewHolder {
        DynamicHeightImageView imgView;
        CircleImageView imgViewAvatar;
        TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        FeedEntryJson feedEntry = getItem(position);
        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.item_feed, null);
            holder = new ViewHolder();
            holder.textView = (TextView)rowView.findViewById(R.id.txtViewFeed);
            holder.imgView = (DynamicHeightImageView)rowView.findViewById(R.id.imgViewFeed);
            holder.imgViewAvatar = (CircleImageView)rowView.findViewById(R.id.imgAvatarFeed);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        if ((feedEntry.image != null) && (feedEntry.image.title != null) ) {

            holder.textView.setText(feedEntry.image.title);
        } else holder.textView.setText("");

        //double width = holder.imgView.getWidth();
        holder.imgView.setImageResource(android.R.color.transparent);
        if ((feedEntry.image != null) && (feedEntry.image.url_medium != null) &&
                URLUtil.isValidUrl(feedEntry.image.url_medium)) {
            //double height = width * feedEntry.image.ratio;
            //holder.imgView.getLayoutParams().height = (int) height;
            holder.imgView.setHeightRatio(feedEntry.image.ratio);
            //Log.d(Constants.LOG_TAG, "Setting height to " + width + " * " +
            //        feedEntry.image.ratio + " = " + (int) height);
            final int pos = position;
            holder.imgView.setTag(pos);
            mImageLoader.get(feedEntry.image.url_medium,
                    new ImageListener(pos, holder.imgView));
        }

        holder.imgViewAvatar.setImageResource(android.R.color.transparent);
        if ((feedEntry.author != null) && (feedEntry.author.avatar != null) &&
                URLUtil.isValidUrl(feedEntry.author.avatar)) {
            final int pos = position;
            holder.imgViewAvatar.setTag(pos);
            mAvatarLoader.get(feedEntry.author.avatar,
                    new ImageListener(pos, holder.imgViewAvatar));
        } else {
            holder.imgViewAvatar.setImageResource(R.drawable.avatar);
        }

        return rowView;
    }

    private class ImageListener implements ImageLoader.ImageListener {
        Integer pos = 0;
        ImageView imgView;

        public ImageListener(Integer position, ImageView imgView) {
            this.pos = position;
            this.imgView = imgView;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
            if ((response.getBitmap() != null) && ((Integer) imgView.getTag() == pos)) {
                imgView.setImageResource(0);
                imgView.setImageBitmap(response.getBitmap());
            }
        }
    }
}
