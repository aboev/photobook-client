package com.freecoders.photobook.gson;

/**
 * Created by Alex on 2014-11-27.
 */
public class UserProfile {
    public String name = "";
    public String phone = "";
    public String email = "";
    public String avatar = "";
    public String id = "";

    public void setNullFields(){
        if (name.isEmpty()) name = null;
        if (phone.isEmpty()) phone = null;
        if (email.isEmpty()) email = null;
        if (avatar.isEmpty()) avatar = null;
        if (id.isEmpty()) id = null;
    }
}

