package io.agora.auikit.service.callback

import io.agora.auikit.model.AUIGiftTabEntity

interface AUIGiftListCallback {

    fun onResult(error: AUiException?,giftList: List<AUIGiftTabEntity>)

}