
package com.stonelandit.tokendoctor.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Doctor {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("myanmar_name")
    @Expose
    private String myanmarName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMyanmarName() {
        return myanmarName;
    }

    public void setMyanmarName(String myanmarName) {
        this.myanmarName = myanmarName;
    }

}
