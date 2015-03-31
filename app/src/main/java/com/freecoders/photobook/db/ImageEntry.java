package com.freecoders.photobook.db;

/**
 * Created by Alex on 2014-12-03.
 */
public class ImageEntry {
    private long id = -1;
    private String MediaStoreID = "";
    private String OrigUri = "";
    private String ThumbUri = "";
    private String ServerId = "";
    private String Title = "";
    private String BucketId = "";
    private int Status = INT_STATUS_DEFAULT;
    public final static int INT_STATUS_DEFAULT = 0;
    public final static int INT_STATUS_SHARING = 1;
    public final static int INT_STATUS_SHARED = 2;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMediaStoreID() {
        return MediaStoreID;
    }

    public void setMediaStoreID(String MediaStoreID) {
        this.MediaStoreID = MediaStoreID;
    }

    public String getOrigUri() {
        return OrigUri;
    }

    public void setOrigUri(String OrigUri) {
        this.OrigUri = OrigUri;
    }

    public String getThumbUri() {
        return ThumbUri;
    }

    public void setThumbUri(String ThumbUri) {
        this.ThumbUri = ThumbUri;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getServerId() {
        return ServerId;
    }

    public void setServerId(String ServerId) {
        this.ServerId = ServerId;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int Status) {
        this.Status = Status;
    }

    public String getBucketId() {
        return BucketId;
    }

    public void setBucketId(String BucketId) {
        this.BucketId = BucketId;
    }
}
