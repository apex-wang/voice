package io.agora.asceneskit.voice.dialog

import android.view.View
import io.agora.auikit.model.AUiUserInfo

interface VoiceApplyDialogEventListener {
    fun onApplyItemClick(view: View, applyIndex: Int, user: AUiUserInfo,position:Int){}
}