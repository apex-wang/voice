package io.agora.asceneskit.voice

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.VoiceRoomViewBinding
import io.agora.asceneskit.voice.bean.VoiceRoomBean
import io.agora.asceneskit.voice.dialog.*
import io.agora.auikit.binder.*
import io.agora.auikit.model.*
import io.agora.auikit.service.*
import io.agora.auikit.service.callback.AUIChatMsgCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.imkit.AUIChatSubscribeDelegate
import io.agora.auikit.ui.basic.AUiAlertDialog
import io.agora.auikit.ui.basic.AUiBottomDialog
import io.agora.auikit.ui.member.impl.AUiRoomMemberListView
import io.agora.auikit.ui.primary.impl.AUIKeyboardStatusWatcher
import io.agora.auikit.ui.primary.listener.AUISoftKeyboardHeightChangeListener
import io.agora.auikit.utils.GsonTools
import io.agora.auikit.utils.ThreadManager
import io.agora.chat.ChatMessage
import java.util.ArrayList

class VoiceRoomView : FrameLayout , IAUiUserService.AUiUserRespDelegate,
    IAUiMicSeatService.AUiMicSeatRespDelegate, IAUiRoomManager.AUiRoomRespDelegate,
    AUIChatSubscribeDelegate, IAUIChatService.AUIChatRespDelegate,
    IAUiInvitationService.AUiInvitationRespDelegate {
    private val mRoomViewBinding = VoiceRoomViewBinding.inflate(LayoutInflater.from(context))
    private val mBinders = mutableListOf<IAUiBindable>()
    private var mVoiceService: IVoiceRoomService? = null
    private var listener:AUISoftKeyboardHeightChangeListener? = null
    private var activity:FragmentActivity?= null
    private var mSeatMap = mutableMapOf<Int, String>()
    private var mLocalMute = true
    private var topBinder:AUITopInformationBinder? = null
    private var giftBinder:AUIGiftViewBinder? = null
    private var chatManager:AUIChatManager? = null
    private var mMemberMap = mutableMapOf<String, AUiUserInfo>()
    private var moreDialog:VoiceRoomMoreDialog?=null
    private var primaryMenuBinder:AUIChatPrimaryMenuBinder?=null
    private val applyList = mutableListOf<AUiUserInfo>()
    private var applyDialog:VoiceRoomApplyDialog?=null
    private var invitedDialog:VoiceRoomInvitedDialog?=null
    private var currentMemberList:MutableList<AUiUserInfo> = mutableListOf()
    private var micType:MicSeatType?=null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mRoomViewBinding.root)
    }

    fun bindService(service: IVoiceRoomService?) {
        mVoiceService = service
        chatManager = service?.getChatManager()
        val roomInfo = service?.getRoomInfo()
        roomInfo?.let {
            val seatStyle = it.micSeatStyle
            micType = MicSeatType.fromString(seatStyle)
            Log.e("apex","MicSeatType  Tag $seatStyle  ${micType?.ordinal}  ${it.micSeatCount}")
            when( micType ){
                MicSeatType.OneTag ->{
                    mRoomViewBinding.micSeatsView.visibility = View.GONE
                    mRoomViewBinding.micSeatsCircleView.visibility = View.VISIBLE
                    mRoomViewBinding.micSeatsCircleView.setOptions(MicSeatOption(
                        it.micSeatCount,MicSeatType.OneTag
                    ))
                }
                MicSeatType.SixTag ->{
                    mRoomViewBinding.micSeatsView.visibility = View.GONE
                    mRoomViewBinding.micSeatsCircleView.visibility = View.VISIBLE
                    mRoomViewBinding.micSeatsCircleView.setOptions(MicSeatOption(
                        it.micSeatCount,MicSeatType.SixTag,if (it.micSeatCount == 3 || it.micSeatCount == 5) 90 else 0
                    ))
                }
                MicSeatType.EightTag ->{
                    mRoomViewBinding.micSeatsView.visibility = View.VISIBLE
                    mRoomViewBinding.micSeatsCircleView.visibility = View.GONE
                }
                MicSeatType.NineTag ->{
                    mRoomViewBinding.micSeatsView.visibility = View.GONE
                    mRoomViewBinding.micSeatsCircleView.visibility = View.GONE
                }
                else -> {}
            }
        }

        ThreadManager.getInstance().runOnMainThread {
            getSoftKeyboardHeight()
        }

        chatManager?.subscribeChatMsg(this)

        if (chatManager?.isOwner() == true){
            chatManager?.getCurrentRoom()?.let { joinRoom(it) }
            Log.e("apex","3 - ${chatManager?.getCurrentRoom()}")
        }

        service?.getUserService()?.bindRespDelegate(this)
        service?.getMicSeatsService()?.bindRespDelegate(this)
        service?.getRoomManager()?.bindRespDelegate(this)
        service?.getChatManagerService()?.bindRespDelegate(this)
        service?.getInvitationService()?.bindRespDelegate(this)

        /** 获取礼物列表 初始化礼物Binder */
        mVoiceService?.getGiftList(object : (List<AUIGiftTabEntity>) -> Unit{
            override fun invoke(data: List<AUIGiftTabEntity>) {
                Log.d("apex","getGiftList:" +  GsonTools.beanToString(data))
                // gift
                giftBinder = service?.let {
                    AUIGiftViewBinder(
                        activity,
                        service.context,
                        mRoomViewBinding.giftView,
                        data,
                        it.getGiftManagerService(),
                        chatManager
                    )
                }
                giftBinder?.bind()
                giftBinder?.let { mBinders.add(it) }
            }
        })

        service?.enterRoom({
            ThreadManager.getInstance().runOnMainThread {
                // top
                topBinder = AUITopInformationBinder(
                    mRoomViewBinding.leftView,
                    mRoomViewBinding.rightView,
                    service.getUserService(),
                    it,
                    listener = object : AUITopInformationBinder.TopInformationEvent{
                        override fun onUserMoreClickListener() {
                            showUserListDialog()
                        }

                        override fun onCloseClickListener() {
                            showExitDialog()
                        }
                    })
                topBinder?.let {
                    it.bind()
                    mBinders.add(it)
                }

                val micSeatsBinder = AUiMicSeatsBinder(
                    if (micType == MicSeatType.EightTag)
                        mRoomViewBinding.micSeatsView
                    else mRoomViewBinding.micSeatsCircleView ,
                    service.getUserService(),
                    service.getMicSeatsService(),
                    service.getInvitationService()
                )

                micSeatsBinder.bind()
                mBinders.add(micSeatsBinder)

                // bottom input
                primaryMenuBinder = AUIChatPrimaryMenuBinder(
                    mVoiceService?.getRoomManager()?.context,
                    mRoomViewBinding.primaryView,
                    mRoomViewBinding.barrageView,
                    mVoiceService?.getChatManager(),
                    object : AUIChatPrimaryMenuBinder.AUIMenuItemClickCallback{
                        override fun onClickGift(view: View?) {
                            giftBinder?.showBottomGiftDialog()
                        }

                        override fun onClickLike(view: View?) {
                            mRoomViewBinding.likeView.addFavor()
                        }

                        override fun onClickMore(view: View?) {
                            showMoreDialog()
                            updateMoreStatus(false)
                        }

                        override fun onClickMic(view: View?) {
                            mLocalMute = !mLocalMute
                            setLocalMute(mLocalMute)
                        }
                    })
                primaryMenuBinder?.let {
                    it.bind()
                    mBinders.add(it)
                    listener = it.getListener()
                }

                // barrage
                val auiBarrageBinder = AUIBarrageBinder(
                    mRoomViewBinding.barrageView,
                    mRoomViewBinding.primaryView,
                    service.getChatManager()
                )
                auiBarrageBinder.bind()
                mBinders.add(auiBarrageBinder)
            }
        },{
            post {
                Toast.makeText(context, "Enter room failed : ${it.code}", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun filterCurrentMember(){
        // 过滤已在麦位的用户
        currentMemberList.clear()
        mMemberMap.values.toList().forEach {  user ->
            // 在麦位数据和所有成员数据中 查找共有的uid
            val uid = mSeatMap.entries.find { it.value == user.userId }?.value
            if (user.userId != uid){
                currentMemberList.add(user)
            }
        }
        invitedDialog?.refreshData(currentMemberList)
    }

    private fun setLocalMute(isMute: Boolean) {
        Log.d("local_mic","update rtc mute state: $isMute")
        mLocalMute = isMute
        mVoiceService?.setupLocalAudioMute(isMute)
        primaryMenuBinder?.updateLocalMuteView(isMute)
    }

    /** IAUiUserService.AUiUserRespDelegate */
    override fun onRoomUserEnter(roomId: String, userInfo: AUiUserInfo) {
        Log.d("apex","onRoomUserEnter ${userInfo.userAvatar} ${userInfo.userId}")
        mMemberMap[userInfo.userId] = userInfo
        filterCurrentMember()
    }

    override fun onRoomUserLeave(roomId: String, userInfo: AUiUserInfo) {
        mMemberMap.remove(userInfo.userId)
        filterCurrentMember()
        updateUserPreview()
    }

    override fun onRoomUserUpdate(roomId: String, userInfo: AUiUserInfo) {
        mMemberMap[userInfo.userId] = userInfo
        filterCurrentMember()
        updateUserPreview()
    }

    override fun onUserAudioMute(userId: String, mute: Boolean) {
        val localUserId = mVoiceService?.context?.currentUserInfo?.userId ?: ""
        if (localUserId != userId) {
            return
        }
        var localUserSeat: AUiMicSeatInfo? = null
        for (i in 0..7) {
            val seatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(i)
            if (seatInfo?.user != null && seatInfo.user?.userId == userId) {
                localUserSeat = seatInfo
                break
            }
        }
        if (localUserSeat != null) {
            val userInfo = mVoiceService?.getUserService()?.getUserInfo(userId)
            val mute = (localUserSeat.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(mute)
        }
    }

    override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUiUserInfo>?) {
        userList?.forEach { userInfo ->
            mMemberMap[userInfo.userId] = userInfo
        }
        filterCurrentMember()
        updateUserPreview()
    }

    /** IAUiMicSeatService.AUiMicSeatRespDelegate */
    override fun onAnchorEnterSeat(seatIndex: Int, userInfo: AUiUserThumbnailInfo) {
        mSeatMap[seatIndex] = userInfo.userId
        val localUserId = mVoiceService?.context?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户上麦
            mVoiceService?.setupLocalStreamOn(true)
            val micSeatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
            val userInfo = mVoiceService?.getUserService()?.getUserInfo(localUserId)
            val isMute = (micSeatInfo?.muteAudio == 1) || (userInfo?.muteAudio == 1)
            setLocalMute(isMute)
        }
        filterCurrentMember()
    }

    override fun onAnchorLeaveSeat(seatIndex: Int, userInfo: AUiUserThumbnailInfo) {
        if (mSeatMap[seatIndex].equals(userInfo.userId)) {
            mSeatMap.remove(seatIndex)
        }
        val localUserId = mVoiceService?.context?.currentUserInfo?.userId ?: ""
        if (userInfo.userId == localUserId) { // 本地用户下麦
            setLocalMute(true)
            mVoiceService?.setupLocalStreamOn(false)
        }
        filterCurrentMember()
    }

    override fun onSeatAudioMute(seatIndex: Int, isMute: Boolean) {
        // 麦位被禁用麦克风
        // 远端用户：关闭对该麦位的音频流订阅
        // 本地用户：保险起见关闭本地用户的麦克风音量
        val micSeatInfo = mVoiceService?.getMicSeatsService()?.getMicSeatInfo(seatIndex)
        val seatUserId = micSeatInfo?.user?.userId
        if (seatUserId == null || seatUserId.isEmpty()) {
            return
        }
        val userInfo = mVoiceService?.getUserService()?.getUserInfo(seatUserId) ?: return
        val localUserId = mVoiceService?.context?.currentUserInfo?.userId ?: ""
        val mute = isMute || (userInfo.muteAudio == 1)
        if (seatUserId == localUserId) {
            setLocalMute(mute)
        } else {
            mVoiceService?.setupRemoteAudioMute(seatUserId, mute)
        }
    }

    private fun updateUserPreview() {
        topBinder?.updateRankList(mMemberMap.values.toList())
    }

    private fun updateMoreStatus(showStatus:Boolean){
        primaryMenuBinder?.setMoreStatus(isRoomOwner(),showStatus)
    }

    fun setFragmentActivity(activity: FragmentActivity){
        this.activity = activity
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mVoiceService?.getUserService()?.unbindRespDelegate(this)
        mVoiceService?.getMicSeatsService()?.unbindRespDelegate(this)
        mVoiceService?.getRoomManager()?.unbindRespDelegate(this)
        mVoiceService?.getChatManagerService()?.unbindRespDelegate(this)
        mVoiceService?.getInvitationService()?.unbindRespDelegate(this)
        chatManager?.unsubscribeChatMsg(this)

        mBinders.forEach {
            it.unBind()
        }

        chatManager?.clear()
    }

    private fun isRoomOwner() =
        mVoiceService?.context?.isRoomOwner(mVoiceService?.channelName) ?: false


    private fun getSoftKeyboardHeight(){
        activity?.let {
            AUIKeyboardStatusWatcher(
                it, activity!!
            ) { isKeyboardShowed: Boolean, keyboardHeight: Int? ->
                Log.e("apex", "isKeyboardShowed: $isKeyboardShowed $keyboardHeight")
                listener?.onSoftKeyboardHeightChanged(isKeyboardShowed,keyboardHeight)
            }
        }
    }

    override fun onUpdateChatRoomId(roomId: String) {
        chatManager?.setChatRoom(roomId)
        Log.e("apex","5")
        if (chatManager?.isOwner() != true){
            joinRoom(roomId)
        }
    }


    private fun joinRoom(roomId:String){
        mVoiceService?.getChatManagerService()?.joinedChatRoom(roomId,object : AUIChatMsgCallback{
            override fun onOriginalResult(error: AUiException?, message: ChatMessage?) {
                ThreadManager.getInstance().runOnMainThread{
                    chatManager?.saveWelcomeMsg(context.getString(R.string.voice_room_welcome))
                    mRoomViewBinding.barrageView.refreshSelectLast()
                    message?.let { chatManager?.parseMsgChatEntity(it) }
                    mRoomViewBinding.barrageView.refreshSelectLast()
                }
            }
        })
    }

    /**
     * 显示用户列表
     */
    private fun showUserListDialog() {
        val membersView = AUiRoomMemberListView(context)
        membersView.setMembers(mMemberMap.values.toList(), mSeatMap)
        membersView.setIsOwner(isRoomOwner(),chatManager?.getCurrentRoomOwnerId())
        membersView.setMemberActionListener(object : AUiRoomMemberListView.ActionListener{
            override fun onKickClick(view: View, position: Int, user: AUiUserInfo) {

            }
        })
        AUiBottomDialog(context).apply {
            setBackground(null)
            setCustomView(membersView)
            show()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun showMoreDialog(){
        val bean = VoiceMoreItemBean()
        val list = mutableListOf<VoiceMoreItemBean>()
        val applyBean = VoiceRoomBean(applyList)
        if (isRoomOwner()){ // 房主显示申请列表
            bean.ItemIcon = context.getDrawable(R.drawable.voice_icon_more_hands)
            bean.ItemTitle = "Application list"
            list.add(bean)
        }
        moreDialog = VoiceRoomMoreDialog(context,list,object :
            VoiceRoomMoreDialog.GridViewItemClickListener{
                override fun onItemClickListener(position: Int) {
                    if (position == 0){
                        if (isRoomOwner()){
                            // 显示申请列表
                            applyDialog = VoiceRoomApplyDialog().apply {
                                arguments = Bundle().apply {
                                    putSerializable(VoiceRoomApplyDialog.KEY_ROOM_APPLY_BEAN, applyBean)
                                    putInt(VoiceRoomApplyDialog.KEY_CURRENT_ITEM, 0)
                                }
                                setApplyDialogListener(object : VoiceApplyDialogEventListener{
                                    override fun onApplyItemClick(
                                        view: View,
                                        applyIndex: Int,
                                        user: AUiUserInfo,
                                        position: Int
                                    ) {
                                        mVoiceService?.getInvitationService()?.acceptApply(
                                            user.userId,
                                            applyIndex
                                        ) {
                                            if (it == null){
                                                Log.d("apex","房主同意上麦申请 成功")
                                                applyList.removeAt(position)
                                                applyDialog?.refreshData(applyList)
                                            }
                                        }
                                    }
                                })
                            }
                            activity?.supportFragmentManager?.let {
                                applyDialog?.show(
                                    it, "VoiceRoomApplyDialog"
                                )
                            }
                        }
                    }
                }
            })
        if (isRoomOwner()){
            activity?.supportFragmentManager?.let { moreDialog?.show(it,"more_dialog") }
        }
    }

    override fun onApplyListUpdate(userList: ArrayList<AUiUserInfo>?) {
        applyList.clear()
        userList?.forEach { it1 ->
            val userInfo = mVoiceService?.getUserService()?.getUserInfo(it1.userId)
            userInfo?.micIndex = it1.micIndex
            userInfo?.let {
                applyList.add(it)
            }
        }
        ThreadManager.getInstance().runOnMainThread {
            updateMoreStatus(true)
            if (isRoomOwner()){
                moreDialog?.updateStatus(0,true)
            }
            applyDialog?.refreshData(applyList)
        }
    }

    override fun onShowInvited(index:Int) {
        // 显示邀请dialog
        val roomBean = VoiceRoomBean()
        roomBean.userList = currentMemberList
        roomBean.invitedIndex = index
        Log.e("apex","onShowInvited $index $roomBean")
        invitedDialog = VoiceRoomInvitedDialog().apply {
            arguments = Bundle().apply {
                putSerializable(VoiceRoomInvitedDialog.KEY_ROOM_INVITED_BEAN, roomBean)
                putInt(VoiceRoomInvitedDialog.KEY_CURRENT_ITEM, 0)
            }
            setInvitedDialogListener(object : VoiceInvitedDialogEventListener{
                override fun onInvitedItemClick(view: View, invitedIndex: Int, user: AUiUserInfo) {
                    mVoiceService?.getInvitationService()?.sendInvitation(
                        user.userId,
                        invitedIndex
                    ) {
                        if (it == null){
                            Log.d("apex","邀请${user.userId}上麦成功 $invitedIndex 成功")
                        }
                    }
                }
            })
        }
        activity?.supportFragmentManager?.let {
            invitedDialog?.show(
                it, "VoiceRoomInvitedDialog"
            )
        }
    }

    override fun onReceiveInvitation(userId: String, micIndex: Int) {
        // 收到上麦邀请
        AUiAlertDialog(context).apply {
            setTitle("收到上麦邀请")
            setMessage("是否同意上麦？")
            setPositiveButton("同意") {
                mVoiceService?.getInvitationService()?.acceptInvitation(userId,micIndex
                ) {
                    if (it == null){
                        Log.d("apex","同意上麦邀请成功")
                    }
                }
                dismiss()
            }
            setNegativeButton("拒绝") {
                mVoiceService?.getInvitationService()?.rejectInvitation(userId){
                    if (it == null){
                        Log.d("apex","拒绝上麦邀请成功")
                    }
                }
                dismiss()
            }
            show()
        }
    }

    fun showExitDialog() {
        AUiAlertDialog(context).apply {
            setTitle("Tip")
            if (isRoomOwner()) {
                setMessage("是否离开并销毁房间？")
            } else {
                setMessage("是否离开房间？")
            }
            setPositiveButton("确认") {
                if (isRoomOwner()) {
                    mVoiceService?.destroyRoom()
                } else {
                    mVoiceService?.exitRoom()
                }
                dismiss()
            }
            setNegativeButton("取消") {
                dismiss()
            }
            show()
        }
    }

    override fun onRoomDestroy(roomId: String) {
        if (roomId == mVoiceService?.channelName){
            AUiAlertDialog(context).apply {
                setTitle("房间已销毁")
                setMessage("请返回房间列表 ")
                setPositiveButton("我知道了") {
                    mVoiceService?.exitRoom()
                    dismiss()
                }
                setCancelable(false)
                show()
            }
        }
    }
}