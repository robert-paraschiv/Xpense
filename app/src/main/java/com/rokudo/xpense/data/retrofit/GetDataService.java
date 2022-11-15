package com.rokudo.xpense.data.retrofit;

import com.rokudo.xpense.data.retrofit.models.AccountDetails;
import com.rokudo.xpense.data.retrofit.models.EndUserAgreement;
import com.rokudo.xpense.data.retrofit.models.Institution;
import com.rokudo.xpense.data.retrofit.models.Requisition;
import com.rokudo.xpense.data.retrofit.models.Token;
import com.rokudo.xpense.data.retrofit.models.Transactions;
import com.rokudo.xpense.data.retrofit.models.TransactionsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface GetDataService {

    @GET("institutions/?country=RO")
    Call<List<Institution>> getAllInstitutions();

    @FormUrlEncoded
    @POST("token/new/")
    Call<Token> getToken(@Field("secret_id") String secret_id, @Field("secret_key") String secret_key);

    @FormUrlEncoded
    @POST("agreements/enduser/")
    Call<EndUserAgreement> createEUA(@Field("institution_id") String institution_id,
                                     @Field("access_scope") List<String> access_scope);


    @FormUrlEncoded
    @POST("requisitions/")
    Call<Requisition> createRequisition(@Field("institution_id") String institution_id,
                                        @Field("redirect") String redirect,
                                        @Field("agreement") String agreement,
                                        @Field("user_language") String user_language,
                                        @Field("reference") String reference,
                                        @Field("account_selection") Boolean account_selection,
                                        @Field("redirect_immediate") Boolean redirect_immediate);


    @GET("requisitions/{id}")
    Call<Requisition> getRequisitionById(@Path("id") String id);

    @GET("accounts/{id}/transactions")
    Call<TransactionsResponse> getAccountTransactions(@Path("id") String id);

    @GET("accounts/{id}/details")
    Call<AccountDetails> getAccountDetails(@Path("id") String id);

    @GET("accounts/{id}/balances")
    Call<Object> getAccountBalances(@Path("id") String id);

}
