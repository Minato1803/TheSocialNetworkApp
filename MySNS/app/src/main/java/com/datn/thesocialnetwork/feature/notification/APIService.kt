package com.datn.thesocialnetwork.feature.notification

import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Call
import retrofit2.http.Body

interface APIService {
    @Headers(
        "Content-Type: application/json",
        "Authorization:key=AAAAS7UOhto:APA91bGYu2ZgJPE58THWS4ak2Ue2YXzlyKTgj__azhN_3FvqtZQSLIHs5E_6obtsV6BpENWeOVIvsTD68fAtJKY7evw7eg6F5r0ruadkR_u_NgxOBuUt-c2K-6sDRwLM3YYuFU-RZAtl"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: Sender): Call<ResponseNoti>
}