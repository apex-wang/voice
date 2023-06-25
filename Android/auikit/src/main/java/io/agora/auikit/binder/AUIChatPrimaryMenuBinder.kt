package io.agora.auikit.binder

import android.util.Log
import android.view.View
import io.agora.auikit.R
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.ui.barrage.impl.AUIBarrageView
import io.agora.auikit.ui.primary.impl.AUIChatPrimaryMenuView
import io.agora.auikit.ui.primary.listener.AUIMenuItemClickListener
import io.agora.auikit.ui.primary.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.utils.ThreadManager

class AUIChatPrimaryMenuBinder constructor(
    roomContext:AUiRoomContext?,
    chatPrimaryView: AUIChatPrimaryMenuView,
    auiBarrageView:AUIBarrageView,
    chatManager: AUIChatManager?,
    menuItemCallback:AUIMenuItemClickCallback
) : IAUiBindable, AUIMenuItemClickListener {

    private var chatPrimaryView: AUIChatPrimaryMenuView?=null
    private var auiBarrageView: AUIBarrageView? =null
    private var listener:AUISoftKeyboardHeightChangeListener?=null
    private var menuItemListener:AUIMenuItemClickCallback
    private var chatManager:AUIChatManager?
    private var roomContext:AUiRoomContext?


    init {
        this.chatPrimaryView = chatPrimaryView
        this.auiBarrageView = auiBarrageView
        this.menuItemListener = menuItemCallback
        this.chatManager = chatManager
        this.roomContext = roomContext
        chatPrimaryView.initMenu()
    }

    override fun bind() {
        chatPrimaryView?.setMenuItemClickListener(this)
        chatPrimaryView?.setSoftKeyListener()
    }

    override fun unBind() {
        chatPrimaryView?.setMenuItemClickListener(null)
    }

    fun setMoreStatus(isOwner:Boolean,status:Boolean){
        chatPrimaryView?.setShowMoreStatus(isOwner,status)
    }

    fun updateLocalMuteView(enable:Boolean){
        chatPrimaryView?.setEnableMic(enable)
    }

    override fun setSoftKeyBoardHeightChangedListener(listener: AUISoftKeyboardHeightChangeListener) {
        this.listener = listener
    }

    fun getListener(): AUISoftKeyboardHeightChangeListener?{
        return listener
    }

    override fun onChatExtendMenuItemClick(itemId: Int, view: View?) {
        when (itemId) {
            R.id.voice_extend_item_more -> {
                //自定义预留
                Log.e("apex","more")
                menuItemListener.onClickMore(view)
            }
            R.id.voice_extend_item_mic -> {
                //点击下方麦克风
                Log.e("apex","mic")
                ThreadManager.getInstance().runOnMainThread{
                    menuItemListener.onClickMic(view)
                }
            }
            R.id.voice_extend_item_gift -> {
                //点击下方礼物按钮 弹出送礼菜单
                menuItemListener.onClickGift(view)
            }
            R.id.voice_extend_item_like -> {
                //点击下方点赞按钮
                menuItemListener.onClickLike(view)
                Log.e("apex","like")
            }
        }
    }

    override fun onSendMessage(content: String?) {
        Log.d("apex","send")
        chatManager?.sendTxtMsg(chatManager?.getCurrentRoom(),content,roomContext?.currentUserInfo,object: AUIChatMsgCallback{
            override fun onResult(error: AUiException?, message: AgoraChatMessage?) {
                Log.d("apex","onResult")
                auiBarrageView?.refreshSelectLast()
            }
        })
    }

    interface AUIMenuItemClickCallback{
        fun onClickGift(view: View?){}
        fun onClickLike(view: View?){}
        fun onClickMore(view: View?){}
        fun onClickMic(view: View?){}
    }

}