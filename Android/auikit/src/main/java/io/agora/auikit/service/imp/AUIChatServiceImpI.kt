package io.agora.auikit.service.imp

import android.util.Log
import io.agora.CallBack
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AUiUserThumbnailInfo
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUICreateChatRoomCallback
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.CreateChatRoomReq
import io.agora.auikit.service.http.room.CreateChatRoomRsp
import io.agora.auikit.service.http.room.CreateUserReq
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.http.user.UserInterface
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val chatRoomIdKey = "chatRoom"
class AUIChatServiceImpI(
    private val roomContext: AUiRoomContext,
    private val channelName: String,
    private val chatManager:AUIChatManager,
    private val rtmManager: AUiRtmManager
) : IAUIChatService, AUiRtmMsgProxyDelegate {

    init {
        chatManager.initManager(channelName,roomContext)
        rtmManager.subscribeMsg(channelName,chatRoomIdKey,this)
    }

    private val delegateHelper = DelegateHelper<IAUIChatService.AUIChatRespDelegate>()

    override fun bindRespDelegate(delegate: IAUIChatService.AUIChatRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIChatService.AUIChatRespDelegate?) {
        delegateHelper.unBindDelegate(delegate)
    }

    override fun getContext() = roomContext

    override fun getChannelName() = channelName

    fun getChatManager() = chatManager

    override fun createChatRoom(roomId: String, callback: AUICreateChatRoomCallback) {
        val userName = chatManager.userName
        userName?.let { createRoom(roomId, it,callback) }
    }

    override fun sendMessage(
        roomId: String,
        text: String,
        userInfo: AUiUserThumbnailInfo,
        callback: AUIChatMsgCallback
    ) {
        chatManager.sendTxtMsg(roomId,text,userInfo,callback)
    }

    override fun joinedChatRoom(roomId: String, callback: AUIChatMsgCallback) {
        chatManager.joinRoom(roomId,callback)
    }

    override fun userQuitRoom(callback: CallBack) {
        //区分是房主还是成员 房主调用解散聊天室api 成员调用退出聊天室api
        if ( roomContext.isRoomOwner(channelName) ){
            chatManager.asyncDestroyChatRoom(callback)
        }else{
            chatManager.leaveChatRoom()
            callback.onSuccess()
        }
    }

    private fun createRoom(roomId: String,userName:String, callback: AUICreateChatRoomCallback){
        HttpManager.getService(RoomInterface::class.java)
            .createChatRoom(
                CreateChatRoomReq(
                    roomId,
                    roomContext.currentUserInfo.userId,
                    userName,
                    description = "",
                    custom = ""
                )
            )
            .enqueue(object : retrofit2.Callback<CommonResp<CreateChatRoomRsp>> {
                override fun onResponse(call: Call<CommonResp<CreateChatRoomRsp>>, response: Response<CommonResp<CreateChatRoomRsp>>) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        val chatRoomId = rsp.chatRoomId
                        // success
                        chatManager.setChatRoom(chatRoomId)
                        Log.e("apex","createChatRoom suc")
                        callback.onResult(null,chatRoomId)
                    } else {
                        callback.onResult(Utils.errorFromResponse(response),null)
                    }
                }
                override fun onFailure(call: Call<CommonResp<CreateChatRoomRsp>>, t: Throwable) {
                    callback.onResult(AUiException(-1, t.message),"")
                    Log.e("apex","createChatRoom onFailure")
                }
            })
    }

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        // 解析数据 获取环信聊天室id
        if (key == chatRoomIdKey){
            delegateHelper.notifyDelegate {
                val room = JSONObject(value.toString())
                it.onUpdateChatRoomId(room.getString("chatRoomId"))
            }
        }
    }




}