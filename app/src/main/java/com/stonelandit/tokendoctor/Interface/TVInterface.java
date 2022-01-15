package com.stonelandit.tokendoctor.Interface;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface TVInterface {
    String JSONURL = "https://www.tokendoctor.com.mm/";
    @POST("/api/v1/tv")
    Call<String> getString();

}
