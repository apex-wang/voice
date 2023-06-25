package io.agora.auikit.ui.barrage.listener

import io.agora.auikit.model.AUIChatEntity

interface AUIBarrageItemClickListener {
    fun onItemClickListener(message: AUIChatEntity?){}
    fun onBarrageViewClickListener(){}
}