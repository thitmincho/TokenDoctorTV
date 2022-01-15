package com.stonelandit.tokendoctor.Interface;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiInterface {

    String JSONURL = "https://demonuts.com/Demonuts/JsonTest/Tennis/";

    @GET("json_parsing.php")
    Call<String> getString();

}