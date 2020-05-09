package com.example.mastodonclient

import com.example.mastodonclient.entity.Account
import com.example.mastodonclient.entity.Media
import com.example.mastodonclient.entity.ResponseToken
import com.example.mastodonclient.entity.Toot
import okhttp3.MultipartBody
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface MastodonApi {
    @GET("api/v1/timelines/public")
    suspend fun fetchPublicTimeline(
        // APIエンドポイントのURLの末尾にクエリを追加する. ?onlyMedia=trueを追加する
        // https://androidbook2020.keiji.io/api/v1/timelines/public?onlyMedia=true
        @Query("only_media") onlyMedia: Boolean = false,
        @Query("max_id") maxId: String? = null
    ): List<Toot>

    @GET("api/v1/timelines/home")
    suspend fun fetchHomeTimeline(
        @Header("Authorization") accessToken: String,
        @Query("max_id") maxId: String? = null,
        @Query("limit") limit: Int? = null
    ): List<Toot>

    @GET("api/v1/accounts/verify_credentials")
    suspend fun verifyAccountCredential(
        @Header("Authorization") accessToken: String
    ): Account

    @FormUrlEncoded
    @POST("api/v1/statuses")
    suspend fun postToot(
        @Header("Authorization") accessToken: String,
        @Field("status") status: String,
        @Field("media_ids[]") mediaIds: List<String>? = null
    ): Toot

    @Multipart
    @POST("api/v1/media")
    suspend fun postMedia(
        @Header("Authorization") accessToken: String,
        @Part file: MultipartBody.Part
    ): Media

    @DELETE("api/v1/statuses/{id}")
    suspend fun deleteToot(
        @Header("Authorization") accessToken: String,
        @Path("id") id: String
    )

    @FormUrlEncoded
    @POST("oauth/token")
    suspend fun token(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("scope") scope: String,
        @Field("code") code: String,
        @Field("grant_type") grantType: String
    ): ResponseToken
}
