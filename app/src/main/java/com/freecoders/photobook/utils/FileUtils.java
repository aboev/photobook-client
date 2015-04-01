package com.freecoders.photobook.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.freecoders.photobook.R;
import com.freecoders.photobook.classes.CallbackInterface;
import com.freecoders.photobook.common.Constants;
import com.freecoders.photobook.common.Photobook;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Alex on 2014-11-22.
 */
public class FileUtils {
    private static String LOG_TAG = "FileUtils";

    public final static Boolean copyFileFromUri(File srcFile, File dstFile) {
        if (srcFile.getAbsolutePath().equals(dstFile.getAbsolutePath())) {
            Log.d(LOG_TAG, "Skipping file copy, src = dest");
            return true;
        }
        try {
            InputStream in = new FileInputStream(srcFile);
            OutputStream out = new FileOutputStream(dstFile);
            byte[] buf = new byte[1024];
            int len;
            int size = 0;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
                size += len;
            }
            in.close();
            out.close();
            Log.d(LOG_TAG, "File copied from " + srcFile.getAbsolutePath() +
                " to " + dstFile.getAbsolutePath() + " size = " + size);
            return true;
        } catch (java.io.IOException e) {
            Log.d(LOG_TAG, "File access error: " + e.getLocalizedMessage());
            return false;
        }
    }

    public final static String getRealPathFromURI(Context context, Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public final static String makeSHA1Hash(String input)
    {
        try {
            MessageDigest md = null;
            md = MessageDigest.getInstance("SHA1");
            md.reset();
            byte[] buffer = input.getBytes();
            md.update(buffer);
            byte[] digest = md.digest();

            String hexStr = "";
            for (int i = 0; i < digest.length; i++) {
                hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
            }
            return hexStr;
        } catch (NoSuchAlgorithmException e) {
            Log.d(LOG_TAG, "Hash exception: " + e.getLocalizedMessage());
            return "";
        }
    }

    public final static class DownloadTask extends AsyncTask<String,Integer,Long> {
        String strURL = "";
        String strLocalFilename = "";
        CallbackInterface onResponse;
        ProgressDialog mProgressDialog = new ProgressDialog(Photobook.getMainActivity());

        public DownloadTask (String strURL, String strLocalFilename,
                             CallbackInterface onResponse) {
            this.strURL = strURL;
            this.strLocalFilename = strLocalFilename;
            this.onResponse = onResponse;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage(Photobook.getMainActivity().
                    getResources().getString(R.string.dialog_downloading));
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.show();
        }
        @Override
        protected Long doInBackground(String... arg) {
            int count;
            try {
                URL url = new URL(strURL);
                URLConnection connection = url.openConnection();
                connection.connect();
                String targetFileName =
                        strURL.substring(strURL.lastIndexOf('/')+1, strURL.length() );
                int fileLength = connection.getContentLength();
                if (strLocalFilename == null || strLocalFilename.isEmpty())
                    strLocalFilename = targetFileName;
                File file = new File(Photobook.getMainActivity().getFilesDir(),
                        Constants.APP_FOLDER +"/" + strLocalFilename);
                if(!file.exists()) {
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                }
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(file);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress ((int)(total*100/fileLength));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
                mProgressDialog.dismiss();
            }
            return null;
        }
        protected void onProgressUpdate(Integer... progress) {
            mProgressDialog.setProgress(progress[0]);
            if(mProgressDialog.getProgress()==mProgressDialog.getMax()) {
                mProgressDialog.dismiss();
                onResponse.onResponse();
            }
        }
    }
}
