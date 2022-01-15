
package com.stonelandit.tokendoctor.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

//    @SerializedName("current")
//    @Expose
//    private Current current;
//    @SerializedName("next")
//    @Expose
//    private Next next;
    @SerializedName("doctor")
    @Expose
    private Doctor doctor;
    @SerializedName("banner")
    @Expose
    private List<Banner> banner = null;

//    public Current getCurrent() {
//        return current;
//    }
//
//    public void setCurrent(Current current) {
//        this.current = current;
//    }
//
//    public Next getNext() {
//        return next;
//    }
//
//    public void setNext(Next next) {
//        this.next = next;
//    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public List<Banner> getBanner() {
        return banner;
    }

    public void setBanner(List<Banner> banner) {
        this.banner = banner;
    }

}
