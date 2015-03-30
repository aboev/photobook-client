package com.freecoders.photobook.gson;

import com.freecoders.photobook.common.Constants;

/**
 * Created by aleksey.boev on 2015-03-25.
 */
public class ServerResponse<T> {
    public String result = "";
    public T data = null;
    public Integer code = 0;
    public String timestamp = "0";

    public Boolean isSuccess () {
        return ((result != null) && result.equals(Constants.RESPONSE_RESULT_OK));
    }
}
