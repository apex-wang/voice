package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUiCreateRoomInfo
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.service.IAUiRoomManager
import io.agora.auikit.service.callback.*
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.*
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.AUiLogger
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.MapperUtils
import io.agora.rtm.RtmConstants
import retrofit2.Call
import retrofit2.Response
import java.util.concurrent.atomic.AtomicBoolean

private const val kRoomInfoKey = "roomInfo"
class AUiRoomManagerImpl(
    private val roomContext: AUiRoomContext,
    private val rtmManager: AUiRtmManager,
    private val chatManager: AUIChatManager
) : IAUiRoomManager, AUiRtmMsgProxyDelegate {

    private val TAG = "AUiRoomManagerImpl"

    var chatRoomId:String?=""

    private val subChannelMsg = AtomicBoolean(false)
    private val subChannelStream = AtomicBoolean(false)

    private val delegateHelper = DelegateHelper<IAUiRoomManager.AUiRoomRespDelegate>()

    private var mChannelName: String? = null

    override fun bindRespDelegate(delegate: IAUiRoomManager.AUiRoomRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUiRoomManager.AUiRoomRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun createRoom(
        createRoomInfo: AUiCreateRoomInfo,
        callback: AUiCreateRoomCallback?
    ) {
        HttpManager.getService(RoomInterface::class.java)
            .createRoom(CreateRoomReq(createRoomInfo.roomName,
                roomContext.currentUserInfo.userId,
                roomContext.currentUserInfo.userName,
                roomContext.currentUserInfo.userAvatar,
                createRoomInfo.micSeatCount,
                createRoomInfo.micSeatStyle
                ))
            .enqueue(object : retrofit2.Callback<CommonResp<CreateRoomResp>> {
                override fun onResponse(call: Call<CommonResp<CreateRoomResp>>, response: Response<CommonResp<CreateRoomResp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val info = AUiRoomInfo().apply {
                            this.roomId = rsp.roomId
                            this.roomName = rsp.roomName
                            this.roomOwner = context.currentUserInfo
                            this.micSeatCount = createRoomInfo.micSeatCount
                            this.micSeatStyle = createRoomInfo.micSeatStyle
                        }
                        context.insertRoomInfo(info)
                        // success
                        callback?.onResult(null, info)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message), null)
                }
            })
    }

    override fun destroyRoom(roomId: String, callback: AUiCallback?) {
        rtmManager.unSubscribe(roomId)
        HttpManager.getService(RoomInterface::class.java)
            .destroyRoom(RoomUserReq(roomId, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<DestroyRoomResp>> {
                override fun onResponse(call: Call<CommonResp<DestroyRoomResp>>, response: Response<CommonResp<DestroyRoomResp>>) {
                    if (response.code() == 200) {
                        // success
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response))
                    }
                }
                override fun onFailure(call: Call<CommonResp<DestroyRoomResp>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message))
                }
            })
    }

    override fun enterRoom(roomId: String, token: String, callback: AUiCallback?) {
        subChannelStream.set(false)
        subChannelMsg.set(false)
        val user = MapperUtils.model2Map(roomContext.currentUserInfo) as? Map<String, String>
        if (user == null) {
            AUiLogger.logger().d("EnterRoom", "user == null")
            callback?.onResult(AUiException(-1, ""))
            return
        }

        AUiLogger.logger().d("EnterRoom", "subscribe room roomId=$roomId token=$token")
        rtmManager.kChannelType = RtmConstants.RtmChannelType.MESSAGE
        rtmManager.subscribe(roomId, token) { subscribeError ->
            AUiLogger.logger().d("EnterRoom", "subscribe room result : $subscribeError")
            if (subscribeError != null) {
                Log.e("apex","102 ${subscribeError.code}  ${subscribeError.message}")
                callback?.onResult(AUiException(subscribeError.code, subscribeError.message))
            }else{
                subChannelMsg.set(true)
                checkSubChannel(callback)
            }
        }
        rtmManager.kChannelType = RtmConstants.RtmChannelType.STREAM
        rtmManager.subscribe(roomId, token) { subscribeError ->
            AUiLogger.logger().d("EnterRoom", "subscribe room result : $subscribeError")
            if (subscribeError != null) {
                Log.e("apex","132 ${subscribeError.code}  ${subscribeError.message}")
                callback?.onResult(AUiException(subscribeError.code, subscribeError.message))
            } else {
                mChannelName = roomId
                rtmManager.subscribeMsg(roomId, "", this)
                Log.e("apex","123")
                subChannelStream.set(true)
                checkSubChannel(callback)
            }
        }
    }

    private fun checkSubChannel(callback: AUiCallback?){
        if (subChannelMsg.get()  && subChannelStream.get()){
            callback?.onResult(null)
        }
    }

    override fun exitRoom(roomId: String, callback: AUiCallback?) {
        rtmManager.unSubscribe(roomId)
        callback?.onResult(null)
        HttpManager.getService(RoomInterface::class.java)
            .leaveRoom(RoomUserReq(roomId, roomContext.currentUserInfo.userId))
            .enqueue(object : retrofit2.Callback<CommonResp<String>> {
                override fun onResponse(call: Call<CommonResp<String>>, response: Response<CommonResp<String>>) {
                }
                override fun onFailure(call: Call<CommonResp<String>>, t: Throwable) {
                }
            })
    }

    override fun getRoomInfoList(lastCreateTime: Long?, pageSize: Int, callback: AUiRoomListCallback?) {
        HttpManager.getService(RoomInterface::class.java)
            .fetchRoomList(RoomListReq(pageSize, lastCreateTime))
            .enqueue(object : retrofit2.Callback<CommonResp<RoomListResp>> {
                override fun onResponse(call: Call<CommonResp<RoomListResp>>, response: Response<CommonResp<RoomListResp>>) {
                    val roomList = response.body()?.data?.list
                    if (roomList != null) {
                        roomList.forEach {
                            Log.e("apex","getRoomInfoList: ${it.micSeatStyle}")
                        }
                        context.resetRoomMap(roomList)
                        callback?.onResult(null, roomList)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<RoomListResp>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message), null)
                }
            })
    }

    override fun getContext() = roomContext
    override fun getChannelName() = mChannelName ?: ""
    fun getChatManager() = chatManager

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        Log.e("apex","onMsgDidChanged $key")
       if (key == kRoomInfoKey){

            delegateHelper.notifyDelegate {

            }
       }

    }

    override fun onMsgRecvEmpty(channelName: String) {
        delegateHelper.notifyDelegate {
            it.onRoomDestroy(channelName)
        }
    }



}