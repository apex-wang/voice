package io.agora.asceneskit.voice

import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.*
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imkit.AUIChatManager

interface IVoiceRoomService :
    IAUiCommonService<IVoiceRoomService.IVoiceRoomRespDelegate> {

    fun getRoomManager(): IAUiRoomManager

    fun getUserService(): IAUiUserService

    fun getMicSeatsService(): IAUiMicSeatService

    fun getGiftManagerService(): IAUIGiftsManagerService

    fun getChatManagerService(): IAUIChatService

    fun getChatManager():AUIChatManager

    fun getInvitationService():IAUiInvitationService

    fun enterRoom(success: (AUiRoomInfo) -> Unit, failure: (AUiException)->Unit)

    fun exitRoom()

    fun destroyRoom()

    fun getRoomInfo(): AUiRoomInfo

    fun getUserList(callback: (List<AUiUserInfo>) -> Unit)

    fun getGiftList(callback: (List<AUIGiftTabEntity>) -> Unit)

    fun setupLocalStreamOn(isOn: Boolean)
    fun setupLocalAudioMute(isMute: Boolean)
    fun setupRemoteAudioMute(userId: String, isMute: Boolean)

    interface IVoiceRoomRespDelegate {
        fun onRoomExitedOrDestroyed() {}
    }
}