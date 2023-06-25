package io.agora.auikit.binder

import android.util.Log
import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.model.AgoraChatMessage
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.imkit.AUIChatSubscribeDelegate
import io.agora.auikit.ui.barrage.impl.AUIBarrageView
import io.agora.auikit.ui.barrage.listener.AUIBarrageItemClickListener
import io.agora.auikit.ui.primary.impl.AUIChatPrimaryMenuView
import io.agora.auikit.utils.ThreadManager

class AUIBarrageBinder constructor(
    auiBarrageView: AUIBarrageView,
    chatPrimaryView: AUIChatPrimaryMenuView?,
    chatManager: AUIChatManager
) : IAUiBindable, AUIBarrageItemClickListener,
    IAUIChatService.AUIChatRespDelegate, AUIChatSubscribeDelegate {

    private var auiBarrageView: AUIBarrageView? =null
    private var chatPrimaryView: AUIChatPrimaryMenuView?=null
    private var chatManager: AUIChatManager

    init {
        this.auiBarrageView = auiBarrageView
        this.chatPrimaryView = chatPrimaryView
        this.chatManager = chatManager
        auiBarrageView.initView(chatManager)
    }

    override fun bind() {
        auiBarrageView?.setBarrageItemClickListener(this)
        chatManager.subscribeChatMsg(this)
    }

    override fun unBind() {
        auiBarrageView?.setBarrageItemClickListener(null)
        chatManager.unsubscribeChatMsg(null)
    }

    override fun onItemClickListener(message: AUIChatEntity?) {
        Log.d("apex","AUIBarrageBinder onItemClickListener")
    }

    override fun onBarrageViewClickListener() {
        Log.d("apex","AUIBarrageBinder onBarrageViewClick")
        chatPrimaryView?.hideKeyboard()
    }

    override fun onReceiveTextMsg(roomId: String?, message: AgoraChatMessage?) {
        ThreadManager.getInstance().runOnMainThread{
            auiBarrageView?.refreshSelectLast()
        }
    }

    override fun onReceiveMemberJoinedMsg(roomId: String?, message: AgoraChatMessage?) {
        ThreadManager.getInstance().runOnMainThread{
            auiBarrageView?.refreshSelectLast()
        }
    }

}