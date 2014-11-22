package com.freecoders.photobook.network;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;

import org.apache.http.HttpEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Created by maximilian on 11/22/14.
 */

public class MultiPartRequest extends Request<String>{
    public static final String KEY_PICTURE = "image";
    //public static final String KEY_PICTURE_NAME = "image";

    private HttpEntity mHttpEntity;

    private String mRouteId;
    private Response.Listener mListener;

    public MultiPartRequest(String url, String filePath,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mHttpEntity = buildMultiPartEntity(filePath);
    }

    public MultiPartRequest(String url, File file,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mHttpEntity = buildMultiPartEntity(file);
    }

    private HttpEntity buildMultiPartEntity(String filePath) {
        File file = new File(filePath);
        return buildMultiPartEntity(file);
    }

    private HttpEntity buildMultiPartEntity(File file) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        String fileName = file.getName();
        FileBody fileBody = new FileBody(file);
        builder.addPart(KEY_PICTURE, fileBody);
        //builder.addTextBody(KEY_PICTURE_NAME, fileName);
        return builder.build();
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            mHttpEntity.writeTo(bos);
        } catch (IOException e) {
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        return Response.success("Uploaded", getCacheEntry());
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
