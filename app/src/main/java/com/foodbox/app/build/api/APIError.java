package com.foodbox.app.build.api;

/**
 * Created by Tamil on 9/25/2017.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class APIError {
    @SerializedName("type")
    @Expose
    private List<String> type = null;

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public APIError withType(List<String> type) {
        this.type = type;
        return this;
    }
}
