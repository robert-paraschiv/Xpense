package com.rokudo.xpense.data.retrofit;

import androidx.annotation.NonNull;

import com.rokudo.xpense.utils.NordigenUtils;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {

        //rewrite the request to add bearer token
        Request newRequest = chain.request().newBuilder()
                .header("Authorization", "Bearer " + NordigenUtils.token)
                .build();

        return chain.proceed(newRequest);
    }
}
