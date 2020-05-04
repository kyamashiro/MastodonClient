package com.example.mastodonclient

import retrofit2.http.GET
import retrofit2.http.Query

interface MastodonApi {
    @GET("api/v1/timelines/public")
    suspend fun fetchPublicTimeline(
        // APIエンドポイントのURLの末尾にクエリを追加する. ?onlyMedia=trueを追加する
        // https://androidbook2020.keiji.io/api/v1/timelines/public?onlyMedia=true
        @Query("only_media") onlyMedia: Boolean = false
    ): List<Toot>
}
