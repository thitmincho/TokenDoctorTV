package com.stonelandit.tokendoctor.helper;

import com.stonelandit.tokendoctor.Interface.TVInterface;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitInstance {
    public static final String BASE_URL = "https://www.tokendoctor.com.mm/";
    public static Retrofit retrofit;

    public static Retrofit getRetrofitInstance(String token) {
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tvid", "1")
                .addFormDataPart("hospital_id", "1")
                .build();


        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
//                String token = "470|Me9mAXg54JbxNNaQQ5plzj9bbEugstYKZDVj2HmI";
                Request newRequest = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer " + token)
                        .post(requestBody)
                        .build();
                return chain.proceed(newRequest);
            }
        }).build();

        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl(TVInterface.JSONURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        return retrofit;
    }
}
