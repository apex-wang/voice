package io.agora.asceneskit.voice.dialog

import android.view.View
import io.agora.auikit.model.AUiUserInfo

interface VoiceInvitedDialogEventListener {
    fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUiUserInfo){}
}