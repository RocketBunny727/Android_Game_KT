package com.example.androidgamekt.network

import retrofit2.http.GET
import retrofit2.http.Query

interface CbrService {
    // Metals XML endpoint: requires date range (dd/MM/yyyy)
    @GET("scripts/XML_metall.asp")
    suspend fun getMetalsXml(
        @Query("date_req1") dateReq1: String,
        @Query("date_req2") dateReq2: String
    ): String
}


