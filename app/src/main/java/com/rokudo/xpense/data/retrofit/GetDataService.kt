package com.rokudo.xpense.data.retrofit

import com.rokudo.xpense.data.retrofit.models.*
import retrofit2.Call
import retrofit2.http.*

interface GetDataService {

    @GET("/api/v2/institutions/?country=RO")
    fun getAllInstitutions(): Call<List<Institution>>

    @FormUrlEncoded
    @POST("/api/v2/token/new/")
    fun getToken(
        @Field("secret_id") secretId: String,
        @Field("secret_key") secretKey: String
    ): Call<Token>

    @FormUrlEncoded
    @POST("/api/v2/token/refresh/")
    fun refreshToken(@Field("refresh") refresh: String): Call<String>

    @FormUrlEncoded
    @POST("/api/v2/agreements/enduser/")
    fun createEUA(
        @Field("institution_id") institutionId: String,
        @Field("access_scope") accessScope: List<String>
    ): Call<EndUserAgreement>

    @FormUrlEncoded
    @POST("/api/v2/requisitions/")
    fun createRequisition(
        @Field("institution_id") institutionId: String,
        @Field("redirect") redirect: String,
        @Field("agreement") agreement: String,
        @Field("user_language") userLanguage: String,
        @Field("reference") reference: String,
        @Field("account_selection") accountSelection: Boolean?,
        @Field("redirect_immediate") redirectImmediate: Boolean?
    ): Call<Requisition>

    @GET("/api/v2/requisitions/{id}/")
    fun getRequisitionById(@Path("id") id: String): Call<Requisition>

    @GET("/api/v2/requisitions/")
    fun getRequisitions(): Call<RequisitionsResult>

    @DELETE("/api/v2/requisitions/{id}/")
    fun deleteRequisition(@Path("id") id: String): Call<DeleteResponse>

    @DELETE("/api/v2/agreements/enduser/{id}/")
    fun deleteEua(@Path("id") id: String): Call<DeleteResponse>

    @GET("/api/v2/accounts/{id}/transactions/")
    fun getAccountTransactions(
        @Path("id") id: String,
        @Query("date_from") dateFrom: String
    ): Call<TransactionsResponse>

    @GET("/api/v2/accounts/{id}/details/")
    fun getAccountDetails(@Path("id") id: String): Call<AccountDetails>

    @GET("/api/v2/accounts/{id}/balances/")
    fun getAccountBalances(@Path("id") id: String): Call<Balances>
}

