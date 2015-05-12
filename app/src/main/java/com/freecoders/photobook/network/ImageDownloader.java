package com.freecoders.photobook.network;

import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.freecoders.photobook.R;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Alex on 2015-05-12.
 */
public class ImageDownloader extends AsyncTask<String,Integer,Long> {
    private static String LOG_TAG = "ImageDownloader";

    private File destFile = null;
    private String strFilename = "";
    private String strURL = "";
    private ProgressDialog mProgressDialog;
    private Boolean boolShowProgress;
    private CallbackInterface callback;

    public ImageDownloader(String strURL, File destFile, Boolean boolShowProgress,
                           CallbackInterface callback) {
        Log.d(LOG_TAG, "Starting image download for " + strURL);
        this.boolShowProgress = boolShowProgress;
        this.strURL = strURL;
        this.callback = callback;
        this.destFile = destFile;
        if (boolShowProgress)
            mProgressDialog = new ProgressDialog(Photobook.getImageDetailsActivity());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (boolShowProgress) {
            mProgressDialog.setMessage(Photobook.getImageDetailsActivity().
                    getResources().getString(R.string.dialog_downloading));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setCancelable(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
    }
    @Override
    protected Long doInBackground(String... aurl) {
        int count;
        try {
            URL url = new URL(strURL);
            URLConnection connection = url.openConnection();
            connection.connect();
            String targetFileName = strURL.substring(strURL.lastIndexOf('/') + 1, strURL.length());
            int length = connection.getContentLength();
            String type = Environment.DIRECTORY_PICTURES;
            File path = Environment.getExternalStoragePublicDirectory(type);
            path.mkdirs();
            File file = destFile;
            if (file == null) file = new File(path, Constants.APP_FOLDER +"/" + targetFileName);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            strFilename = file.toString();
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(file);
            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                publishProgress ((int)(total*100/length));
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            e.printStackTrace();
            if (boolShowProgress)
                mProgressDialog.dismiss();
        }
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
        if (boolShowProgress) {
            mProgressDialog.setProgress(progress[0]);
            if (mProgressDialog.getProgress() == mProgressDialog.getMax()) {
                mProgressDialog.dismiss();
                Toast.makeText(Photobook.getImageDetailsActivity(), Photobook.getImageDetailsActivity().
                        getResources().getString(R.string.dialog_download_complete)
                        , Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPostExecute(Long l) {
        Log.d(LOG_TAG, "Saved image to " + strFilename);
        if (callback != null) callback.onResponse(null);
        if (!strFilename.isEmpty())
            MediaScannerConnection.scanFile(Photobook.getImageDetailsActivity(),
                    new String[]{strFilename}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });
    }
}