package com.creadto.creadto_aos.convert.network

import com.creadto.creadto_aos.convert.network.model.UserData
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ServerApi {
    @POST(".")
    @FormUrlEncoded
    @Headers("Content-Type:application/octet-stream")
    suspend fun sendDataCounter(
        @Field("Counter") plyCounter : Int
    ) : Response<UserData>

    @Multipart
    @POST(".")
    suspend fun fileUpload(
        @Part file : MultipartBody.Part
    ) : Response<UserData>

    @POST(".")
    @FormUrlEncoded
    @Headers("Content-Type:application/octet-stream")
    suspend fun observeStatus(
        @Field("Status") check : String
    ) : Response<UserData>

    @POST(".")
    @FormUrlEncoded
    @Headers("Content-Type:application/octet-stream")
    suspend fun fileDownload(
        @Field("mesh") request : String
    ) : Response<ResponseBody>

}