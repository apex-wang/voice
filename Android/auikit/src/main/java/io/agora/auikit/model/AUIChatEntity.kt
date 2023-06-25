package io.agora.auikit.model

import java.io.Serializable

data class AUIChatEntity (

    /**
     * chatId : string
     * user : AUiUserThumbnailInfo
     * content: String
     * joined : Boolean
     */
    var user: AUiUserThumbnailInfo?,
    var content: String?,
    var joined: Boolean

): Serializable