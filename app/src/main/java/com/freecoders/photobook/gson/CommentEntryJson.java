package com.freecoders.photobook.gson;

import com.android.volley.Request;

import org.apache.http.HttpRequest;

/**
 * Created by Alex on 2015-02-15.
 */
public class CommentEntryJson {
    public UserProfile author;
    public long timestamp = 0;
    public String text = "";
    public String author_id = "";
    public String image_id = "";
    public long id = 0;
    public long reply_to = 0;

    public CommentEntryJson() {
    }

    public CommentEntryJson(int method) {
        if (method == Request.Method.POST) {
            author = null;
            author_id = null;
        }
    }

    public void setNullFields(){
        if (text.isEmpty()) text = null;
        if (author_id.isEmpty()) author_id = null;
        if (image_id.isEmpty()) image_id = null;
    }
}
