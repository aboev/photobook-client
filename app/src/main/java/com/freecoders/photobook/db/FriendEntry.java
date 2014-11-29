package com.freecoders.photobook.db;

/**
 * Created by Alex on 2014-11-27.
 */
public class FriendEntry {
    private long id;
    private String Name;
    private String ContactKey;
    private String UserId;
    private String Avatar;
    private int Status;
    public final static int INT_STATUS_NULL = 0;
    public final static int INT_STATUS_DEFAULT = 1;
    public final static int INT_STATUS_FRIEND = 2;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getContactKey() {
        return ContactKey;
    }

    public void setContactKey(String ContactKey) {
        this.ContactKey = ContactKey;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String UserId) {
        this.UserId = UserId;
    }

    public String getAvatar() {
        return Avatar;
    }

    public void setAvatar(String Avatar) {
        this.Avatar = Avatar;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

}