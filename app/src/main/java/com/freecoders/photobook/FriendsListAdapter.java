package com.freecoders.photobook;

import android.content.Context;
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
import com.freecoders.photobook.utils.BitmapLruCache;
import com.freecoders.photobook.utils.DiskBitmapCache;

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
        ImageLoader.ImageCache imageCache = new BitmapLruCache();
        imageLoader = new ImageLoader(VolleySingleton.getInstance(context).getRequestQueue(),
                imageCache);
                //new DiskBitmapCache(context.getCacheDir()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LinearLayout view;
        FriendEntry profile = getItem(position);
        View rowView = convertView;

        if(rowView == null)
        {
            String inflater = Context.LAYOUT_INFLATER_SERVICE;
            LayoutInflater vi = (LayoutInflater)getContext().getSystemService(inflater);
            rowView = vi.inflate(R.layout.row_friend_list, null);
        }

        final CircleImageView avatar = (CircleImageView)rowView.findViewById(R.id.imgAvatar);
        TextView nameText =(TextView)rowView.findViewById(R.id.txtName);
        avatar.setImageResource(R.drawable.avatar);

        nameText.setText(profile.getName());

        if ((profile.getAvatar().isEmpty() == false)
                && (URLUtil.isValidUrl(profile.getAvatar()))) {
            Log.d(Constants.LOG_TAG, "Requesting avatar = " + profile.getAvatar());
            imageLoader.get(profile.getAvatar().toString(),
                    new ImageLoader.ImageListener() {
                public void onErrorResponse(VolleyError error) {
                }

                public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
                    if (response.getBitmap() != null) {
                        avatar.setImageResource(0);
                        avatar.setImageBitmap(response.getBitmap());
                    }
                }
            });
        }

        return rowView;
    }

}