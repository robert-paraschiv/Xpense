package com.rokudo.xpense.data.retrofit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstance {
    private static Retrofit retrofit;

    private static final String BASE_URL = "https://ob.nordigen.com";

    public static Retrofit geInstance() {
        if (retrofit == null) {
            TokenInterceptor tokenInterceptor = new TokenInterceptor();
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(tokenInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .client(client)
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

}

