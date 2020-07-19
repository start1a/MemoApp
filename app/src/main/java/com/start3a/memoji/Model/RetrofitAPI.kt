package com.start3a.memoji.Model

import com.start3a.memoji.data.ImgObjFromNaver
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface RetrofitAPI {

    // 클라이언트 ID, PW는 업로드하지 않음

    @GET("search/{type}")
    fun requestSearchImage(
        @Header("X-Naver-Client-Id") clientId: String = "clientID",
        @Header("X-Naver-Client-Secret") clientSecret: String = "clientSecret",
        @Path("type") type: String,
        @Query("query") keyword: String,
        @Query("display") display: Int,
        @Query("start") page: Int,
        @Query("sort") sort: String = "sim"
    ): Observable<ImgObjFromNaver>

}
