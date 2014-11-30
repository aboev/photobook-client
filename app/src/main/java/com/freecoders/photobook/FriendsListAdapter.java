package com.freecoders.photobook;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.db.FriendEntry;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.MemoryLruCache;
import com.freecoders.photobook.utils.DiskLruBitmapCache;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Alex on 2014-11-28.
 */
public class FriendsListAdapter extends ArrayAdapter<FriendEntry> {

    int resource;
    String response;
    Context context;
    ImageLoader imageLoader;
    //Initialize adapter
    public FriendsListAdapter(Context context, int resource, List<FriendEntry> items) {
        super(context, resource, items);
        this.resource=resource;
        ImageLoader.ImageCache memoryCache = new MemoryLruCache();
        try {
            DiskLruBitmapCache diskCache = new DiskLruBitmapCache(context, "DiskCache",
                2000000, Bitmap.CompressFormat.JPEG, 100);
            imageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    diskCache);
        } catch (Exception e) {
            imageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                    memoryCache);
            Log.d(Constants.LOG_TAG, "Failed to initialize disk cache");
        }
    }

    static class ViewHolder {
        TextView nameText;
        CircleImageView imgAvatar;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout view;
        FriendEntry profile = getItem(position);
        View rowView = convertView;
        ViewHolder holder = null;

        if(rowView == null) {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.row_friend_list, null);
            holder = new ViewHolder();
            holder.nameText = (TextView)rowView.findViewById(R.id.txtName);
            holder.imgAvatar = (CircleImageView)rowView.findViewById(R.id.imgAvatar);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
        }

        holder.imgAvatar.setImageResource(R.drawable.avatar);
        holder.nameText.setText(profile.getName());
        holder.imgAvatar.setTag(position);

        if ((profile.getAvatar().isEmpty() == false)
                && (URLUtil.isValidUrl(profile.getAvatar()))) {
            Log.d(Constants.LOG_TAG, "Requesting avatar for " + profile.getName() +
                            " " + profile.getAvatar());
            imageLoader.get(profile.getAvatar().toString(),
                    new ImageListener(position, holder.imgAvatar));
        }

        return rowView;
    }

    private class ImageListener implements ImageLoader.ImageListener {
        Integer pos = 0;
        CircleImageView imgAvatar;

        public ImageListener(Integer position, CircleImageView imgAvatar){
            this.pos = position;
            this.imgAvatar = imgAvatar;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
            if ((response.getBitmap() != null) && ((Integer) imgAvatar.getTag() == pos)) {
                Log.d(Constants.LOG_TAG, "Setting bitmap for pos = " + pos);
                imgAvatar.setImageResource(0);
                imgAvatar.setImageBitmap(response.getBitmap());
            }
        }
    }
}