package com.freecoders.photobook.network;

import com.freecoders.photobook.gson.UserProfile;

import java.util.Map;

/**
 * @author Andrei Alikov andrei.alikov@gmail.com
 */
public class DefaultServerResponseHandler implements ServerResponseHandler {
    @Override
    public void onFollowersResponse(Map<String, UserProfile> users) {

    }
}
