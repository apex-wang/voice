package io.agora.auikit.ui.primary.listener

import io.agora.auikit.model.AUIExpressionIcon

interface AUIExpressionClickListener {
    fun onDeleteImageClicked()
    fun onExpressionClicked(emojiIcon: AUIExpressionIcon?)
}