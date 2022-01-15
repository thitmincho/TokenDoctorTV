
package com.stonelandit.tokendoctor.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Next {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("token")
    @Expose
    private Integer token;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getToken() {
        return token;
    }

    public void setToken(Integer token) {
        this.token = token;
    }

}
