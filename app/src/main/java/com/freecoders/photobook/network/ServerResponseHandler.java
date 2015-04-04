package com.freecoders.photobook.network;

import com.freecoders.photobook.gson.UserProfile;

import java.util.Map;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public interface ServerResponseHandler {
    void onFollowersResponse(Map<String, UserProfile> users);
}
