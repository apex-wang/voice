package io.agora.asceneskit.voice.bean

import io.agora.auikit.model.AUiUserInfo
import java.io.Serializable

data class VoiceRoomBean constructor(
    var userList: MutableList<AUiUserInfo> = mutableListOf(),
    var invitedIndex:Int? = -1
): Serializable
