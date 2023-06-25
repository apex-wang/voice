package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.*
import io.agora.auikit.service.IAUiInvitationService
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.apply.ApplyAcceptReq
import io.agora.auikit.service.http.apply.ApplyCancelReq
import io.agora.auikit.service.http.apply.ApplyCreateReq
import io.agora.auikit.service.http.apply.ApplyInterface
import io.agora.auikit.service.http.invitation.*
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val RoomApplyKey = "application"
private const val RoomInvitationKey = "invitation"
class AUiInvitationServiceImpl(
    private val roomContext: AUiRoomContext,
    private val channelName: String,
    private val rtmManager: AUiRtmManager
) : IAUiInvitationService, AUiRtmMsgProxyDelegate {

    init {
        rtmManager.subscribeMsg(channelName, RoomApplyKey,this)
        rtmManager.subscribeMsg(channelName, RoomInvitationKey,this)
    }

    private val delegateHelper = DelegateHelper<IAUiInvitationService.AUiInvitationRespDelegate>()

    override fun bindRespDelegate(delegate: IAUiInvitationService.AUiInvitationRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiInvitationService.AUiInvitationRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun sendInvitation(
        userId: String,
        seatIndex: Int,
        callback: AUiCallback?
    ) {
        HttpManager.getService(InvitationInterface::class.java)
            .initiateCreate(
                InvitationCreateReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId,
                    InvitationPayload("",seatIndex))
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    Log.d("apex"," ${response.body()}")
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback?.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun acceptInvitation(userId: String, seatIndex: Int, callback: AUiCallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptInitiate(
                InvitationAcceptReq(
                    channelName,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback?.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun rejectInvitation(userId: String, callback: AUiCallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptCancel(
                RejectInvitationAccept(
                    channelName,
                    userId,
                    ""
                )
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback?.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun cancelInvitation(userId: String, callback: AUiCallback?) {
        HttpManager.getService(InvitationInterface::class.java)
            .acceptCancel(
                RejectInvitationAccept(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId
                )
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback?.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun sendApply(seatIndex: Int, callback: AUiCallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCreate(
                ApplyCreateReq(
                channelName,
                roomContext.currentUserInfo.userId,
                InvitationPayload("",seatIndex))
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun cancelApply(callback: AUiCallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCancel(
                ApplyCancelReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    roomContext.currentUserInfo.userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun acceptApply(userId: String, seatIndex: Int, callback: AUiCallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyAccept(
                ApplyAcceptReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun rejectApply(userId: String, callback: AUiCallback) {
        HttpManager.getService(ApplyInterface::class.java)
            .applyCancel(
                ApplyCancelReq(
                    channelName,
                    roomContext.currentUserInfo.userId,
                    userId)
            )
            .enqueue(object : Callback<CommonResp<Any>>{
                override fun onResponse(
                    call: Call<CommonResp<Any>>,
                    response: Response<CommonResp<Any>>
                ) {
                    if (response.body()?.code == 0 && response.body()?.message == "Success"){
                        callback.onResult(null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<Any>>, t: Throwable) {
                    callback.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun getContext() = roomContext

    override fun getChannelName() = channelName

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        Log.e("apex","AUiServiceImpl key: $key")
        if (key == RoomApplyKey){ //申请
            delegateHelper.notifyDelegate {
                val list = paresData(value)
                if (list.size > 0){
                    it.onApplyListUpdate(paresData(value))
                }
            }
        }else if (key == RoomInvitationKey){//邀请
            delegateHelper.notifyDelegate {
                val list = paresData(value)
                if (list.size > 0 && list[list.lastIndex].userId == roomContext.currentUserInfo.userId){
                    it.onReceiveInvitation(list[list.lastIndex].userId,list[list.lastIndex].micIndex)
                }
            }
        }
    }

    private fun paresData(value: Any):ArrayList<AUiUserInfo>{
        val userList = ArrayList<AUiUserInfo>()
        val map: Map<String, Any> = HashMap()
        val micSeat = GsonTools.toBean(value as String, map.javaClass)
        val s = micSeat?.get("micSeat") as Map<String,Any>
        val queue = s["queue"] as ArrayList<Map<String,Any>>
        queue.forEach {
            val payload = it["payload"] as Map<String,Any>
            val seatNo = payload["seatNo"] as Long
            val applyBean = AUiUserInfo()
            applyBean.userId = it["userId"].toString()
            applyBean.micIndex = seatNo.toInt()
            Log.d("apex","${it["userId"]} - ${seatNo.toInt()} -- $it")
            userList.add(applyBean)
        }
        return userList
    }
}