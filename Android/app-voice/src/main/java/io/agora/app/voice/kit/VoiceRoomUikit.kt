package io.agora.app.voice.kit

import android.util.Log
import io.agora.CallBack
import io.agora.asceneskit.voice.IVoiceRoomService
import io.agora.asceneskit.voice.VoiceRoomService
import io.agora.auikit.model.*
import io.agora.auikit.service.IAUiRoomManager
import io.agora.auikit.service.callback.AUICreateChatRoomCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.imp.*
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.utils.AUiLogger
import io.agora.auikit.utils.AgoraEngineCreator
import io.agora.chat.ChatClient
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineEx
import io.agora.rtm.RtmClient

object VoiceRoomUikit {
    private val notInitException =
        RuntimeException("The VoiceServiceManager has not been initialized!")
    private val initedException =
        RuntimeException("The VoiceServiceManager has been initialized!")

    private val initMainProcess =
        RuntimeException("The ChatManager enter the service process!")

    private var mRoomManager: IAUiRoomManager? = null
    private var mRoomContext: AUiRoomContext? = null
    private var mRtmManager: AUiRtmManager? = null
    private var mChatManager: AUIChatManager? = null

    private var shouldReleaseRtm = false
    private var shouldReleaseRtc = false

    private var mRtmClient: RtmClient? = null
    private var mRtcEngineEx: RtcEngineEx? = null

    fun init(
        config: AUiCommonConfig,
        rtmClient: RtmClient? = null,
        rtcEngineEx: RtcEngineEx? = null,
    ) {
        if (mRoomManager != null) {
            throw initedException
        }

        val roomContext = AUiRoomContext()
        roomContext.roomConfig = config
        roomContext.currentUserInfo.userId = config.userId
        roomContext.currentUserInfo.userName = config.userName
        roomContext.currentUserInfo.userAvatar = config.userAvatar
        mRoomContext = roomContext

        shouldReleaseRtc = mRtcEngineEx == null
        mRtcEngineEx = rtcEngineEx ?: AgoraEngineCreator.createRtcEngine(
            config.context,
            config.appId
        )

        AgoraEngineCreator.createChatClient(
            config.context,
            config.appKey
        )

        shouldReleaseRtm = rtmClient == null
        mRtmClient = rtmClient ?: AgoraEngineCreator.createRtmClient(
            config.context,
            config.appId,
            config.userId
        )

        mRtmManager = AUiRtmManager(roomContext.roomConfig.context, mRtmClient!!)
        mChatManager = AUIChatManager(roomContext.roomConfig.context, ChatClient.getInstance())
        mRoomManager = AUiRoomManagerImpl(roomContext, mRtmManager!!,mChatManager!!)

        mChatManager?.getChatUser(roomContext.currentUserInfo.userId)

        AUiLogger.initLogger(AUiLogger.Config(roomContext.roomConfig.context, "Voice"))
    }

    /**
     * 释放资源
     */
    fun release() {
        if (shouldReleaseRtc) {
            RtcEngine.destroy()
        }
        if (shouldReleaseRtm) {
            RtmClient.release()
        }
        mRoomContext = null
        mRtcEngineEx = null
        mRtmClient = null
        mRtmManager = null
        mRoomManager = null
    }

    /**
     * 获取房间列表
     */
    fun getRoomList(
        startTime: Long?,
        pageSize: Int,
        success: (List<AUiRoomInfo>) -> Unit,
        failure: (AUiException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.getRoomInfoList(
            startTime, pageSize
        ) { error, roomList ->
            if (error == null) {
                success.invoke(roomList ?: emptyList())
            } else {
                failure.invoke(error)
            }
        }
    }

    /**
     * 创建房间
     */
    fun createRoom(
        createRoomInfo: AUiCreateRoomInfo,
        success: (AUiRoomInfo) -> Unit,
        failure: (AUiException) -> Unit
    ) {
        val roomManager = mRoomManager ?: throw notInitException
        roomManager.createRoom(
            createRoomInfo
        ) { error, roomInfo ->
            if (error == null && roomInfo != null) {
                success.invoke(roomInfo)
            } else {
                failure.invoke(error ?: AUiException(-999, "RoomInfo return null"))
            }
        }

    }

    /**
     * 拉起并跳转的房间页面
     */
    fun launchRoom(
        roomInfo: AUiRoomInfo,
        config: AUiRoomConfig,
        launchType:LaunchType,
        eventHandler: RoomEventHandler? = null,
    ) {
        val roomContext = mRoomContext ?: throw notInitException
        val rtmManager = mRtmManager ?: throw notInitException
        val roomManager = mRoomManager ?: throw notInitException
        val rtcEngine = mRtcEngineEx ?: throw notInitException
        val chatManager = mChatManager?: throw notInitException
        val channelName = roomInfo.roomId

        if (!chatManager.isLoggedIn()){
            chatManager.loginChat(object : CallBack{
                override fun onSuccess() {
                    Log.e("apex","loginChat onSuccess ")
                }

                override fun onError(code: Int, error: String?) {
                    Log.e("apex","loginChat onError $code $error")
                }
            })
        }

        // login rtm
        rtmManager.login(
            config.tokenMap[AUiRoomConfig.TOKEN_RTM_VOICE_LOGIN]
        ) { error ->
            if (error == null) {
                // create room service
                val chatService =  AUIChatServiceImpI(roomContext,channelName, chatManager,rtmManager)
                val roomService = VoiceRoomService(
                    roomContext,
                    rtcEngine,
                    roomInfo,
                    config.tokenMap[AUiRoomConfig.TOKEN_RTC_VOICE_SERVICE],
                    config.tokenMap[AUiRoomConfig.TOKEN_RTC_VOICE_SERVICE],
                    roomManager,
                    chatManager,
                    AUiUserServiceImpl(roomContext, channelName, rtmManager),
                    AUiMicSeatServiceImpl(roomContext, channelName, rtmManager),
                    AUIGiftServiceImpI(roomContext,channelName, rtmManager,chatService.getChatManager()),
                    chatService,
                    AUiInvitationServiceImpl(roomContext,channelName,rtmManager)
                )

                if (launchType == LaunchType.CREATE){
                    chatService.createChatRoom(roomInfo.roomId,object : AUICreateChatRoomCallback{
                        override fun onResult(error: AUiException?, chatRoomId: String?) {
                            if (error == null){
                                Log.e("apex","createChatRoom suc")
                                chatManager.setChatRoom(chatRoomId)
                                // launch room activity
                                launchActivity(
                                    roomContext,
                                    config,
                                    roomService,
                                    eventHandler
                                )
                            }
                        }
                    })
                }else{
                    // launch room activity
                    launchActivity(
                        roomContext,
                        config,
                        roomService,
                        eventHandler
                    )
                }
            } else {
                eventHandler?.onRoomLaunchFailure?.invoke(
                    AUiException(
                        ErrorCode.RTM_LOGIN_FAILURE.value,
                        "$error"
                    )
                )
            }
        }
    }

    private fun launchActivity(
        roomContext: AUiRoomContext,
        config: AUiRoomConfig,
        roomService: IVoiceRoomService,
        eventHandler: RoomEventHandler? = null,){
        // launch room activity
        VoiceRoomActivity.launch(
            roomContext.roomConfig.context,
            config.themeId,
            roomService,
            onRoomCreated = {
                eventHandler?.onRoomLaunchSuccess?.invoke()
            },
            onRoomDestroy = { isPermsLeak ->
                mRtmManager?.logout()
                if (isPermsLeak) {
                    eventHandler?.onRoomLaunchFailure?.invoke(
                        AUiException(
                            ErrorCode.PERMISSIONS_LEAK.value,
                            ""
                        )
                    )
                }
            }
        )
    }

    enum class LaunchType(){
        CREATE,
        JOIN
    }


    enum class ErrorCode(val value: Int) {
        RTM_LOGIN_FAILURE(100),
        PERMISSIONS_LEAK(101)
    }

    data class RoomEventHandler(
        val onRoomLaunchSuccess: (() -> Unit)? = null,
        val onRoomLaunchFailure: ((AUiException) -> Unit)? = null,
    )
}