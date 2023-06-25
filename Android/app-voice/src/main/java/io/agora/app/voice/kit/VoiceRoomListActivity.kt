package io.agora.app.voice.kit

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import io.agora.app.voice.BuildConfig
import io.agora.app.voice.databinding.RoomListItemBinding
import io.agora.app.voice.databinding.VoiceRoomListActivityBinding
import io.agora.auikit.model.*
import io.agora.auikit.service.http.TokenGenerator
import io.agora.auikit.ui.basic.AUiAlertDialog
import io.agora.auikit.utils.BindingViewHolder
import java.util.*

class VoiceRoomListActivity: AppCompatActivity() {
    private val mUserId = Random().nextInt(99999999).toString()
    private val mViewBinding by lazy { VoiceRoomListActivityBinding.inflate(LayoutInflater.from(this)) }
    private var mList = listOf<AUiRoomInfo>()
    private val listAdapter by lazy { RoomListAdapter() }

    companion object {
        private var ThemeId = io.agora.asceneskit.R.style.Theme_VoiceRoom
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initService()
    }

    override fun onDestroy() {
        super.onDestroy()
        VoiceRoomUikit.release()
    }

    private fun initView() {
        if (ThemeId != View.NO_ID) {
            setTheme(ThemeId)
        }
        setContentView(mViewBinding.root)

        val out = TypedValue()
        if (theme.resolveAttribute(android.R.attr.windowBackground, out, true)) {
            window.setBackgroundDrawableResource(out.resourceId)
        }

        mViewBinding.btnCreateRoom.setOnClickListener {
            AUiAlertDialog(this@VoiceRoomListActivity).apply {
                setTitle("房间主题")
                setInput("房间主题", "", true)
                setPositiveButton("一起嗨") {
                    dismiss()
                    createRoom(inputText)
                }
                show()
            }
        }

        mViewBinding.btnSwitchTheme.setOnClickListener {
            ThemeId = if (ThemeId == io.agora.asceneskit.R.style.Theme_VoiceRoom) {
                io.agora.asceneskit.R.style.Theme_VoiceRoom_Voice
            } else {
                io.agora.asceneskit.R.style.Theme_VoiceRoom
            }
            theme.setTo(resources.newTheme())
            initView()
        }

        mViewBinding.btnConfig.setOnClickListener {

        }

        mViewBinding.rvList.adapter = listAdapter

        mViewBinding.swipeRefresh.setOnRefreshListener {
            refreshRoomList()
        }

    }


    private fun initService() {
        // Create Common Config
        val config = AUiCommonConfig()
        config.context = application
        config.appId = BuildConfig.AGORA_APP_ID
        config.appKey = BuildConfig.AGORA_APP_KEY
        config.userId = mUserId
        config.userName = "user_$mUserId"
        config.userAvatar = randomAvatar()
        // init AUiKit
        VoiceRoomUikit.init(
            config = config, // must
            rtmClient = null, // option
            rtcEngineEx = null, // option
        )
        fetchRoomList()
    }

    private fun createRoom(roomName: String) {
        val createRoomInfo = AUiCreateRoomInfo()
        Log.e("apex","createRoom ${MicSeatType.SixTag.value} ")
        createRoomInfo.roomName = roomName
        createRoomInfo.micSeatCount = 8
        createRoomInfo.micSeatStyle= MicSeatType.EightTag.value.toString()
        VoiceRoomUikit.createRoom(
            createRoomInfo,
            success = { roomInfo ->
                Log.e("apex", "createRoom success ${roomInfo.roomId}  ${roomInfo.roomOwner?.userId}")
                gotoRoomDetailPage(roomInfo,VoiceRoomUikit.LaunchType.CREATE)
            },
            failure = {
                Toast.makeText(this@VoiceRoomListActivity, "Create room failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun fetchRoomList(){
        var lastCreateTime: Long? = null
        if (!mViewBinding.swipeRefresh.isRefreshing) {
            mList.lastOrNull()?.let {
                lastCreateTime = it.createTime
            }
        }
        VoiceRoomUikit.getRoomList(lastCreateTime,10,
                success = { roomList ->
                   Log.d("apex","fetchRoomList get roomList suc! $roomList")
                    if (roomList.size < 10) {
                        listAdapter.loadingMoreState = LoadingMoreState.NoMoreData
                    } else {
                        listAdapter.loadingMoreState = LoadingMoreState.Normal
                    }
                    mList = if (mViewBinding.swipeRefresh.isRefreshing) { // 下拉刷新则重新设置数据
                        roomList
                    } else {
                        val temp = mutableListOf<AUiRoomInfo>()
                        temp.addAll(mList)
                        temp.addAll(roomList)
                        temp
                    }
                    runOnUiThread {
                        mViewBinding.swipeRefresh.isRefreshing = false
                        listAdapter.submitList(mList)
                    }
        },
            failure = {
                Toast.makeText(this@VoiceRoomListActivity, "get roomList failed!", Toast.LENGTH_SHORT)
                    .show()
            }
        )
    }

    private fun refreshRoomList() {
        mViewBinding.swipeRefresh.isRefreshing = true
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun loadMore() {
        listAdapter.loadingMoreState = LoadingMoreState.Loading
        fetchRoomList()
    }

    private fun gotoRoomDetailPage(roomInfo: AUiRoomInfo,launchType: VoiceRoomUikit.LaunchType) {
        createRoomConfig(roomInfo) {
            VoiceRoomUikit.launchRoom(roomInfo, it,launchType,VoiceRoomUikit.RoomEventHandler(
                onRoomLaunchSuccess = {
                    Toast.makeText(
                        this@VoiceRoomListActivity,
                        "Room ${roomInfo.roomName} launch success.",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                onRoomLaunchFailure = {
                    Toast.makeText(
                        this@VoiceRoomListActivity,
                        "Room ${roomInfo.roomName} launch failure: code=${it.code}, msg=${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            ))
        }
    }

    private fun createRoomConfig(roomInfo: AUiRoomInfo, success: (AUiRoomConfig) -> Unit) {
        val config = AUiRoomConfig(roomInfo.roomId)
        config.themeId = ThemeId

        TokenGenerator.generateMultiChannelTokens(
            BuildConfig.AGORA_APP_ID,
            BuildConfig.AGORA_CERTIFICATE,
            listOf(
                TokenGenerator.ChannelToken(
                    AUiRoomConfig.TOKEN_RTM_VOICE_LOGIN,
                    roomInfo.roomId,
                    mUserId,
                    TokenGenerator.TokenGeneratorType.token007,
                    TokenGenerator.AgoraTokenType.rtm
                ),
            ),
            success = { tokenMap ->
                config.tokenMap = tokenMap
                success.invoke(config)
            },
            failure = {
                Toast.makeText(
                    this@VoiceRoomListActivity,
                    "Token generate failed! error:$it",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }



    private fun randomAvatar(): String {
        val randomValue = Random().nextInt(8) + 1
        return "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_${randomValue}.png"
    }

    enum class LoadingMoreState {
        Normal,
        Loading,
        NoMoreData,
    }

    inner class RoomListAdapter :
        ListAdapter<AUiRoomInfo, BindingViewHolder<RoomListItemBinding>>(object :
            DiffUtil.ItemCallback<AUiRoomInfo>() {

            override fun areItemsTheSame(oldItem: AUiRoomInfo, newItem: AUiRoomInfo) =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(
                oldItem: AUiRoomInfo,
                newItem: AUiRoomInfo
            ) = false
        }) {

        var loadingMoreState: LoadingMoreState = LoadingMoreState.NoMoreData

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ) =
            BindingViewHolder(RoomListItemBinding.inflate(LayoutInflater.from(parent.context)))

        override fun onBindViewHolder(
            holder: BindingViewHolder<RoomListItemBinding>,
            position: Int
        ) {
            val item = getItem(position)
            holder.binding.tvRoomName.text = item.roomName
            holder.binding.tvRoomOwner.text = item.roomOwner?.userName ?: "unKnowUser"
            holder.binding.root.setOnClickListener {
                this@VoiceRoomListActivity.gotoRoomDetailPage(item,VoiceRoomUikit.LaunchType.JOIN) }

            Glide.with(holder.binding.ivAvatar)
                .load(item.roomOwner?.userAvatar)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.binding.ivAvatar)

            if (loadingMoreState == LoadingMoreState.Normal && itemCount > 0 && position == itemCount - 1) {
                this@VoiceRoomListActivity.loadMore()
            }
        }
    }


}