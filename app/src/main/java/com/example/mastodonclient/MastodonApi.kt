package com.example.mastodonclient

import retrofit2.http.GET

interface MastodonApi {
    @GET("api/v1/timelines/public")
    suspend fun fetchPublicTimeline(): List<Toot>
}
