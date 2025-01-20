package com.example.dummyimageuploading

import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface CloudinaryApiService {
    @Multipart
    @POST("v1_1/dixcja6yr/image/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: String,
        @Part("timestamp") timestamp: String,
        @Part("signature") signature: String,
        @Part("upload_preset") uploadPreset: String
    ): CloudinaryResponse
}