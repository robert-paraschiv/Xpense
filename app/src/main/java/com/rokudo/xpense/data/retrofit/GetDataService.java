package com.rokudo.xpense.data.retrofit;

import com.rokudo.xpense.data.retrofit.models.EUAResponse;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Token;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface GetDataService {

    @GET("institutions/?country=RO")
    Call<List<Institution>> getAllInstitutions();

    @FormUrlEncoded
    @POST("token/new/")
    Call<Token> getToken(@Field("secret_id") String secret_id, @Field("secret_key") String secret_key);

    @GET("agreements/enduser/")
    Call<EUAResponse> getAllAgreements();

    @FormUrlEncoded
    @POST("agreements/enduser/")
    Call<EndUserAgreement> createEUA(@Field("institution_id") String institution_id);

}
