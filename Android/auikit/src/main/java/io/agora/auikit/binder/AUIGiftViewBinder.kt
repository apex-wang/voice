package io.agora.auikit.binder

import android.util.Log
import androidx.fragment.app.FragmentActivity
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUIChatService
import io.agora.auikit.service.IAUIGiftsManagerService
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.ui.gift.impl.AUIGiftBarrageBarrageView
import io.agora.auikit.ui.gift.impl.dialog.AUIGiftBottomDialog
import io.agora.auikit.utils.ThreadManager

class AUIGiftViewBinder constructor(
    activity: FragmentActivity?,
    roomContext:AUiRoomContext?,
    giftView:AUIGiftBarrageBarrageView,
    data:List<AUIGiftTabEntity>,
    giftService: IAUIGiftsManagerService,
    chatManager:AUIChatManager?
):IAUiBindable,IAUIGiftsManagerService.AUIGiftRespDelegate {

    private var auiGiftBarrageView: AUIGiftBarrageBarrageView? =null
    private var mGiftList : List<AUIGiftTabEntity> = mutableListOf()
    private var activity: FragmentActivity?
    private var giftService:IAUIGiftsManagerService
    private var roomContext:AUiRoomContext?=null
    private var chatManager:AUIChatManager?


    init {
        auiGiftBarrageView = giftView
        this.chatManager = chatManager
        this.giftService = giftService
        this.mGiftList = data
        this.activity = activity
        this.roomContext = roomContext
        chatManager?.let { giftView.initView(it) }
    }

    override fun bind() {
        giftService.bindRespDelegate(this)
    }

    override fun unBind() {
        giftService.bindRespDelegate(null)
    }

    fun showBottomGiftDialog(){
        activity?.let {
            val dialog = AUIGiftBottomDialog(it, mGiftList)
            dialog.setDialogActionListener(object : AUIGiftBottomDialog.ActionListener{
                override fun onGiftSend(bean: AUIGiftEntity?) {
                    bean?.let { it1 ->
                        it1.sendUser = roomContext?.currentUserInfo
                        it1.giftCount = 1
                        giftService.sendGift(it1) { error ->
                            if (error == null) {
                                ThreadManager.getInstance().runOnMainThread{
                                    Log.e("apex", "sendGift suc ${giftService.channelName}")
                                    chatManager?.addGiftList(it1)
                                    auiGiftBarrageView?.refresh(giftService.channelName)
                                }
                            } else {
                                Log.e("apex", "sendGift error ${error.code} ${error.message}")
                            }
                        }
                    }
                }
            })
            dialog.show(it.supportFragmentManager, "gift_dialog")
        }

    }

    override fun onReceiveGiftMsg(channel:String) {
        Log.e("apex", "onReceiveGiftMsg ")
        ThreadManager.getInstance().runOnMainThread{
            auiGiftBarrageView?.refresh(channel)
        }
    }

}