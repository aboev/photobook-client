package com.freecoders.photobook.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.freecoders.photobook.common.Constants;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by maximilian on 11/22/14.
 */

public class MultiPartRequest extends Request<String>{
    public static final String KEY_PICTURE = "image";
    public static final String KEY_PICTURE_NAME = "name";

    private HttpEntity mHttpEntity;

    private String mRouteId;
    private Response.Listener mListener;

    private HashMap<String, String> mRequestHeaders = null;

    public MultiPartRequest(String url, String filePath,
                            HashMap<String, String> requestHeaders,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mRequestHeaders = requestHeaders;
        mHttpEntity = buildMultiPartEntity(filePath);
    }

    public MultiPartRequest(String url, File file,
                            HashMap<String, String> requestHeaders,
                            Response.Listener<String> listener,
                            Response.ErrorListener errorListener) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mRequestHeaders = requestHeaders;
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
        builder.addTextBody(KEY_PICTURE_NAME, fileName);
        return builder.build();
    }

    @Override
    public String getBodyContentType() {
        return mHttpEntity.getContentType().getValue();
    }

    @Override
    public Map<String, String> getHeaders() {
        return ((mRequestHeaders == null) ? Collections.<String, String>emptyMap() : mRequestHeaders);
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
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    protected void deliverResponse(String response) {
        mListener.onResponse(response);
    }
}
