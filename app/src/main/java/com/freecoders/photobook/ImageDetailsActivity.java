package com.freecoders.photobook;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.freecoders.photobook.common.Photobook;
import com.freecoders.photobook.gson.CommentEntryJson;
import com.freecoders.photobook.gson.ImageJson;
import com.freecoders.photobook.gson.UserProfile;
import com.freecoders.photobook.network.ServerInterface;
import com.freecoders.photobook.network.VolleySingleton;
import com.freecoders.photobook.utils.MemoryLruCache;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;


public class ImageDetailsActivity extends ActionBarActivity {

    CircleImageView mAvatarImageView;
    TextView mImageTitleTextView;
    ImageView mImageView;
    TextView mLikeCountTextView;
    ImageView mLikeImageView;
    ImageView mDownloadImageView;
    LinearLayout mHeaderLayout;

    UserProfile mAuthor;
    ImageJson mImage;

    ListView mCommentsList;
    CommentListAdapter mCommentListAdapter;

    ImageLoader mAvatarLoader;
    ImageLoader mImageLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_details);

        View view = View.inflate(this, R.layout.activity_image_details_header, null);

        mAvatarImageView =   (CircleImageView) view.findViewById(R.id.imgAvatarDetails);
        mImageTitleTextView = (TextView) view.findViewById(R.id.textImageTitleDetails);
        mImageView = (ImageView) view.findViewById(R.id.imgViewDetails);
        mLikeCountTextView = (TextView) view.findViewById(R.id.textLikeCountDetails);
        mLikeImageView = (ImageView) view.findViewById(R.id.imgViewLike);
        mDownloadImageView = (ImageView) view.findViewById(R.id.imgViewDownload);
        mHeaderLayout = (LinearLayout) view.findViewById(R.id.detailsHeaderLayout);
        mCommentsList = (ListView) findViewById(R.id.commentsListDetails);

        mAuthor = Photobook.getImageDetails().author;
        mImage = Photobook.getImageDetails().image;

        if (Photobook.getAvatarDiskLruCache() != null) {
            mAvatarLoader = new ImageLoader(VolleySingleton.getInstance(
                    Photobook.getMainActivity()).getRequestQueue(),
                    Photobook.getAvatarDiskLruCache());
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(
                    Photobook.getMainActivity()).getRequestQueue(),
                    Photobook.getImageDiskLruCache());
        } else {
            ImageLoader.ImageCache memoryCache = new MemoryLruCache();
            mAvatarLoader = new ImageLoader(VolleySingleton.getInstance(
                    Photobook.getMainActivity()).getRequestQueue(),
                    memoryCache);
            mImageLoader = new ImageLoader(VolleySingleton.getInstance(
                    Photobook.getMainActivity()).getRequestQueue(),
                    memoryCache);
        }

        mCommentListAdapter = new CommentListAdapter(this,
                R.layout.item_comment, new ArrayList<CommentEntryJson>(),
                mAvatarLoader);
        mCommentsList.setAdapter(mCommentListAdapter);
        mCommentsList.addHeaderView(view);

        populateView();
    }

    public void populateView(){

        ViewTreeObserver viewTree = mHeaderLayout.getViewTreeObserver();
        viewTree.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                int imgWidth = mHeaderLayout.getMeasuredWidth();
                int imgHeight = (int) (mImage.ratio * 1.0 * imgWidth);
                mImageView.getLayoutParams().height = imgHeight;
                return true;
            }
        });

        if ((mImage != null) && (mImage.url_medium != null)
                && (!mImage.url_medium.isEmpty())
                && (URLUtil.isValidUrl(mImage.url_medium))) {
            mImageView.setTag(0);
            mImageLoader.get(mImage.url_medium, new ImageListener(mImageView));
        }

        if ((mAuthor != null) && (mAuthor.avatar != null) && (!mAuthor.avatar.isEmpty())
                && (URLUtil.isValidUrl(mAuthor.avatar))) {
            mAvatarImageView.setTag(0);
            mImageLoader.get(mAuthor.avatar, new ImageListener(mAvatarImageView));
        }

        if (mAuthor.name != null)
            mImageTitleTextView.setText(mImage.title);

        if (mImage.likes != null)
            mLikeCountTextView.setText(String.valueOf(mImage.likes.length));

        ServerInterface.getComments(this, mImage.image_id, mCommentListAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_image_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class ImageListener implements ImageLoader.ImageListener {
        ImageView imgView;

        public ImageListener(ImageView imgView) {
            this.imgView = imgView;
        }

        @Override
        public void onErrorResponse(VolleyError error) {
        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {
            if (response.getBitmap() != null) {
                imgView.setImageResource(0);
                imgView.setImageBitmap(response.getBitmap());
            }
        }
    }
}
