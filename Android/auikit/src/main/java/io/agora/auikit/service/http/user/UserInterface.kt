package io.agora.auikit.service.http.user

import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.room.CreateUserReq
import io.agora.auikit.service.http.room.CreateUserRsp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserInterface {
    @POST("chatRoom/users/create")
    fun createUser(@Body req: CreateUserReq): Call<CommonResp<CreateUserRsp>>
}