package io.agora.auikit.binder

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import io.agora.auikit.model.AUiRoomInfo
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.service.IAUiUserService
import io.agora.auikit.service.IAUiUserService.AUiUserRespDelegate
import io.agora.auikit.ui.topInformation.AUITopInformationView
import io.agora.auikit.ui.topInformation.impl.AUIUpperLeftInformationView
import io.agora.auikit.ui.topInformation.impl.AUIUpperRightInformationView
import io.agora.auikit.ui.topInformation.listener.AUIUpperLeftActionListener
import io.agora.auikit.ui.topInformation.listener.AUIUpperRightActionListener

class AUITopInformationBinder constructor(
    upperLeft:AUIUpperLeftInformationView,
    upperRight:AUIUpperRightInformationView,
    userService: IAUiUserService?,
    auiRoomInfo: AUiRoomInfo,
    listener:TopInformationEvent
):
    IAUiBindable,
    AUiUserRespDelegate,
    AUITopInformationView,
    AUIUpperLeftActionListener,
    AUIUpperRightActionListener {
    private var mMainHandler: Handler? = null
    private var userService:IAUiUserService?=null
    private var upperLeft:AUIUpperLeftInformationView? = null
    private var upperRight:AUIUpperRightInformationView? = null
    private var auiRoomInfo:AUiRoomInfo? = null
    private var event:TopInformationEvent?=null

    init {
        mMainHandler = Handler(Looper.getMainLooper())
        this.event = listener
        this.userService = userService
        this.upperLeft = upperLeft
        this.upperRight = upperRight
        this.auiRoomInfo = auiRoomInfo
        // 缺少房间信息server 设置房主头像、房间标题和子标题
        auiRoomInfo.let {
            it.roomOwner?.userAvatar?.let { it1 -> upperLeft.setMemberAvatar(it1) }
            it.roomName.let { it1 -> upperLeft.setVoiceTitle(it1) }
            upperLeft.setVoiceSubTitle(it.roomId)
        }
    }

    override fun bind() {
        userService?.bindRespDelegate(this)
        upperLeft?.setUpperLeftActionListener(this)
        upperRight?.setUpperRightActionListener(this)
    }

    override fun unBind() {
        mMainHandler?.removeCallbacksAndMessages(null)
        mMainHandler = null
        userService?.unbindRespDelegate(this)
        upperLeft?.setUpperLeftActionListener(null)
        upperRight?.setUpperRightActionListener(null)
    }

    private fun runOnUiThread(runnable: Runnable) {
        if (mMainHandler != null) {
            if (mMainHandler?.looper?.thread === Thread.currentThread()) {
                runnable.run()
            } else {
                mMainHandler?.post(runnable)
            }
        }
    }

    fun updateRankList(rankList: List<AUiUserInfo>){
        runOnUiThread { upperRight?.setRankData(rankList) }
    }

    interface TopInformationEvent{
        fun onRankListClickListener(){}
        fun onUserMoreClickListener(){}
        fun onCloseClickListener(){}
    }

    override fun onClickUpperLeftAvatar(view: View) {
        Log.e("apex","onClickUpperLeftAvatar")
    }

    override fun onLongClickUpperLeftAvatar(view: View): Boolean {
        Log.e("apex","onLongClickUpperLeftAvatar")
        return true
    }

    override fun onUpperLeftRightIconClickListener(view: View) {
        Log.e("apex","onUpperLeftRightIconClickListener")
    }

    override fun onMemberRankClickListener(view: View) {
        Log.e("apex","onMemberRankClickListener")
        event?.onRankListClickListener()
    }

    override fun onUpperRightUserMoreClickListener(view: View) {
        Log.e("apex","onUpperRightUserMoreClickListener")
        event?.onUserMoreClickListener()
    }

    override fun onUpperRightShutDownClickListener(view: View) {
        Log.e("apex","onUpperRightShutDownClickListener")
        event?.onCloseClickListener()
    }


}