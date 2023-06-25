package io.agora.auikit.service.callback


interface AUICreateChatRoomCallback {
    fun onResult(error: AUiException?,chatRoomId: String?)
}