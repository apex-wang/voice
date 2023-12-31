package io.agora.auikit.ui.gift.impl

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textview.MaterialTextView
import io.agora.auikit.R
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.ui.basic.AUiImageView
import io.agora.auikit.utils.ThreadManager

class AUIGiftRowAdapter constructor(
    private val context: Context,
    private var chatManager: AUIChatManager
): RecyclerView.Adapter<GiftViewHolder>() {
    var dataList:ArrayList<AUIGiftEntity> = ArrayList<AUIGiftEntity>()
    private var mContext:Context?=null

    init {
        this.mContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        return GiftViewHolder(
            LayoutInflater.from(context).inflate(R.layout.voice_widget_gift_item, parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        val auiGiftInfo = dataList[position]
        Log.e("apex"," data $dataList")
        show(holder.avatar, holder.icon, holder.name, auiGiftInfo)

        if (mContext != null) {
            val fromAsset = Typeface.createFromAsset(
                mContext?.assets,
                "fonts/RobotoNembersVF.ttf"
            ) //根据路径得到Typeface
            holder.iconCount.typeface = fromAsset
        }
        holder.iconCount.text = "x" + auiGiftInfo.giftCount
    }

    private fun show(avatar: AUiImageView, icon: AUiImageView, name: MaterialTextView, auiGiftEntity: AUIGiftEntity?) {
        val builder = StringBuilder()
        if (null != auiGiftEntity) {
            val sendUser = auiGiftEntity.sendUser
            val sendName = if(TextUtils.isEmpty(sendUser?.userName)){
                sendUser?.userId.toString()
            }else{
                sendUser?.userName.toString()
            }

            Log.e("apex"," data1 ${auiGiftEntity.giftName}")
            builder.append(sendName)
                .append(":").append("\n").append(context.getString(R.string.voice_gift_sent))
                .append(" ").append(auiGiftEntity.giftName)

            Log.e("apex"," data2 ${auiGiftEntity.giftIcon}")

            ThreadManager.getInstance().runOnMainThread{
                Glide.with(context)
                    .load(sendUser?.userAvatar)
                    .error(R.drawable.vocie_user_image)
                    .into(avatar)

                Glide.with(context)
                    .load(auiGiftEntity.giftIcon)
                    .into(icon)
            }
        }
        val span = SpannableString(builder.toString())
        name.text = span
    }

    fun removeAll() {
        ThreadManager.getInstance().runOnMainThread {
            notifyItemRangeRemoved(0, dataList.size)
            dataList.clear()
        }
    }

    fun refresh(channel:String) {
        // 刷新礼物列表 记录当前礼物列表数量 计算起始 position
        val positionStart: Int = dataList.size
        // 获取增量礼物列表插入原礼物列表
        val giftList = chatManager.getGiftList()
        Log.e("apex","refresh $channel $giftList")
        giftList.let { dataList.addAll(it) }
        // 只更新新插入的礼物item
        if ( dataList.size > 0 && positionStart < dataList.size) {
            ThreadManager.getInstance().runOnMainThread{
                notifyItemRangeInserted(
                    positionStart,
                    dataList.size
                )
            }
        }
    }

    override fun getItemCount(): Int {
        if (dataList.size > 0){
            return dataList.size
        }
        return 0
    }
}


class GiftViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var name: MaterialTextView
    var icon: AUiImageView
    var iconCount: MaterialTextView
    var avatar: AUiImageView

    init {
        avatar = itemView.findViewById(R.id.avatar)
        name = itemView.findViewById(R.id.nick_name)
        icon = itemView.findViewById(R.id.icon)
        iconCount = itemView.findViewById(R.id.gift_count)
    }
}