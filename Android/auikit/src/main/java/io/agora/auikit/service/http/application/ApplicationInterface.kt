package io.agora.auikit.service.http.application

import io.agora.auikit.service.http.CommonResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApplicationInterface {

    @POST("application/create")
    fun createApplication(@Body req: ApplicationCreateReq): Call<CommonResp<Any>>

}