package com.freecoders.photobook.network;

import com.android.volley.VolleyError;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public interface ServerErrorHandler {
    void onServerRequestError(String request, VolleyError error);
    void onServerRequestError(String request, Exception ex);
}
