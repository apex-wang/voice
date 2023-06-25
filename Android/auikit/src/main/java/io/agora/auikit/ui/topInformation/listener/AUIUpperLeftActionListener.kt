package io.agora.auikit.ui.topInformation.listener

import android.view.View

interface AUIUpperLeftActionListener {
    fun onClickUpperLeftAvatar(view: View){}
    fun onLongClickUpperLeftAvatar(view: View): Boolean { return false }
    fun onUpperLeftRightIconClickListener(view: View){}
}