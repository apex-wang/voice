package io.agora.auikit.service.callback

import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.model.AgoraChatMessage
import io.agora.chat.ChatMessage

interface AUIChatMsgCallback {
    fun onResult(error: AUiException?,message: AgoraChatMessage?){}
    fun onEntityResult(error: AUiException?,message: AUIChatEntity?){}
    fun onOriginalResult(error: AUiException?,message: ChatMessage?){}
}