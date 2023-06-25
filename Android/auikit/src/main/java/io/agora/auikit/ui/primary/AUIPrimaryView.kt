package io.agora.auikit.ui.primary

import io.agora.auikit.ui.primary.listener.AUIMenuItemClickListener

interface AUIPrimaryView {

    fun setMenuItemClickListener(listener: AUIMenuItemClickListener?){}

    fun setSoftKeyListener(){}
}