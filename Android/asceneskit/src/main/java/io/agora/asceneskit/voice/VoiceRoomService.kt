package io.agora.asceneskit.voice

import android.util.Log
import io.agora.auikit.model.*
import io.agora.auikit.service.*
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.utils.AUiLogger
import io.agora.auikit.utils.DelegateHelper
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine

class VoiceRoomService(
    private val mRoomContext: AUiRoomContext,
    private val mRtcEngine: RtcEngine,
    private val roomInfo: AUiRoomInfo,
    private val roomToken: String,
    private val rtcToken: String,
    private val roomManager: IAUiRoomManager,
    private val chatManager: AUIChatManager,
    private val userService: IAUiUserService,
    private val micSeatService: IAUiMicSeatService,
    private val giftManager:IAUIGiftsManagerService,
    private val chatService:IAUIChatService,
    private val inviteService: IAUiInvitationService
    ):IVoiceRoomService {

    private val mDelegateHelper = DelegateHelper<IVoiceRoomService.IVoiceRoomRespDelegate>()

    override fun bindRespDelegate(delegate: IVoiceRoomService.IVoiceRoomRespDelegate?) {
        mDelegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IVoiceRoomService.IVoiceRoomRespDelegate?) {
        mDelegateHelper.unBindDelegate(delegate)
    }

    override fun getContext() = roomManager.context
    override fun getChannelName() = roomInfo.roomId
    override fun getRoomManager() = roomManager
    override fun getUserService() = userService
    override fun getMicSeatsService() = micSeatService
    override fun getGiftManagerService() = giftManager
    override fun getChatManagerService() = chatService
    override fun getChatManager() = chatManager
    override fun getInvitationService() = inviteService

    override fun getRoomInfo() = roomInfo

    override fun enterRoom(success: (AUiRoomInfo) -> Unit, failure: (AUiException) -> Unit) {
        AUiLogger.logger().d("EnterRoom", "enterRoom $channelName start ...")
        roomManager.enterRoom(channelName, roomToken) { error ->
            AUiLogger.logger().d("EnterRoom", "enterRoom $channelName result : $error")
            if (error != null) {
                // failure
                failure.invoke(error)
            } else {
                // success
                success.invoke(roomInfo)

                // TODO Workaround: 在rtm加入成功之后才能加入rtc频道，且频道名不同，rtc频道为roomName+_rtc
                joinRtcRoom()
            }
            AUiLogger.logger().d("EnterRoom", "enterRoom $channelName end ...")
        }
    }

    override fun exitRoom() {
        roomManager.exitRoom(channelName) {}
        mDelegateHelper.notifyDelegate { it.onRoomExitedOrDestroyed() }
        leaveRtcRoom()
    }

    override fun destroyRoom() {
        roomManager.destroyRoom(channelName) {}
        mDelegateHelper.notifyDelegate { it.onRoomExitedOrDestroyed() }
        leaveRtcRoom()
    }

    override fun getUserList(callback: (List<AUiUserInfo>) -> Unit) {
        userService.getUserInfoList(channelName, null) { error, userList ->
            if (error != null || userList == null) {
                AUiLogger.logger().e(
                    "VoiceRoomService",
                    "getUserInfoList error : $error - $userList"
                )
            } else {
                AUiLogger.logger().d(
                    "VoiceRoomService",
                    "getUserInfoList userList : $userList"
                )
                callback.invoke(userList)
            }
        }
    }

    override fun getGiftList(callback: (List<AUIGiftTabEntity>) -> Unit) {
        giftManager.getGiftsFromService(object : AUIGiftListCallback{
            override fun onResult(error: AUiException?, giftList: List<AUIGiftTabEntity>) {
                if (error == null){
                    callback.invoke(giftList)
                }
            }
        })
    }


    override fun setupLocalStreamOn(isOn: Boolean) {
        Log.d("rtc_publish_state", "isOn: $isOn")
        if (isOn) {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = true
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            mRtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }

    override fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            mRtcEngine.adjustRecordingSignalVolume(0)
        } else {
            mRtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    override fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        mRtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }


    private fun joinRtcRoom() {
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        mRtcEngine.enableVideo()
        mRtcEngine.enableLocalVideo(false)
        mRtcEngine.enableAudio()
        mRtcEngine.setAudioProfile(
            Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )
        mRtcEngine.enableAudioVolumeIndication(50, 10, true)
        mRtcEngine.setClientRole(if (context.isRoomOwner(channelName)) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE)
        val ret: Int = mRtcEngine.joinChannel(
            rtcToken,
            channelName,
            null,
            mRoomContext.roomConfig.userId.toInt()
        )
        if (ret != Constants.ERR_OK) {
            // TODO LOG
        }
    }

    private fun leaveRtcRoom() {
        mRtcEngine.leaveChannel()
    }




}