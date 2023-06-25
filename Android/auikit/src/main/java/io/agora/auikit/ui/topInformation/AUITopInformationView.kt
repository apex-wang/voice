package io.agora.auikit.ui.topInformation

import io.agora.auikit.ui.topInformation.listener.AUIUpperLeftActionListener
import io.agora.auikit.ui.topInformation.listener.AUIUpperRightActionListener


interface AUITopInformationView {

    fun setUpperLeftActionListener(listener: AUIUpperLeftActionListener?){}

    fun setUpperRightActionListener(listener:AUIUpperRightActionListener?){}

}