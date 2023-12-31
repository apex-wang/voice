package io.agora.auikit.ui.barrage.impl

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import io.agora.auikit.R
import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.ui.basic.AUiImageView
import io.agora.auikit.ui.primary.impl.AUISmileUtils
import io.agora.auikit.utils.ThreadManager


class AUIBarrageAdapter(
    private val context: Context,
    private var chatManager: AUIChatManager,
    typedArray: TypedArray
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var messages: ArrayList<AUIChatEntity> = ArrayList()
    private val ITEM_DEFAULT_TYPE = 0
    private val ITEM_SYSTEM_TYPE = 1
    private var normalTagIcon: Int
    private var normalTitleColor:Int
    private var normalContentColor:Int
    private var systemTitleColor:Int
    private var systemContentColor:Int



    init {
        normalTagIcon = typedArray.getResourceId(
            R.styleable.AUIBarrageView_aui_barrage_normal_title_tag_icon,
            R.drawable.voice_icon_owner
        )

        normalTitleColor = typedArray.getColor(
            R.styleable.AUIBarrageView_aui_barrage_normal_title_TextColor,
            context.resources.getColor(R.color.voice_color_8bb3ff)
        )

        normalContentColor = typedArray.getColor(
            R.styleable.AUIBarrageView_aui_barrage_normal_content_TextColor,
            context.resources.getColor(R.color.voice_white)
        )

        systemTitleColor = typedArray.getColor(
            R.styleable.AUIBarrageView_aui_barrage_system_title_TextColor,
            context.resources.getColor(R.color.voice_color_8bb3ff)
        )

        systemContentColor = typedArray.getColor(
            R.styleable.AUIBarrageView_aui_barrage_system_content_TextColor,
            context.resources.getColor(R.color.voice_color_fcf0b3)
        )

        typedArray.recycle()

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_DEFAULT_TYPE) {
            return NormalViewHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.voice_widget_barrage_msgs_item, parent, false)
            )
        } else if (viewType == ITEM_SYSTEM_TYPE) {
            return SystemViewHolder(
                LayoutInflater.from(
                    context
                ).inflate(R.layout.voice_widget_barrage_system_msgs_item, parent, false)
            )
        }
        return NormalViewHolder(
            LayoutInflater.from(
                context
            ).inflate(R.layout.voice_widget_barrage_msgs_item, parent, false)
        )
    }

    override fun getItemViewType(position: Int): Int {
        val message: AUIChatEntity = messages[position]
        return if (message.joined) { ITEM_SYSTEM_TYPE } else ITEM_DEFAULT_TYPE
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var from = ""
        val message: AUIChatEntity = messages[position]
        from = message.user?.userName.toString()
        val s: String? = message.content
        if (holder is NormalViewHolder) {
            if (TextUtils.isEmpty(from)) {
                from = message.user?.userId.toString()
            }
            if (s != null) {
                showNormalText(
                    chatManager.getCurrentRoomOwnerId() ==  message.user?.userId,
                    (holder as NormalViewHolder).content,
                    from,
                    s
                )
            }
        } else if (holder is SystemViewHolder) {
            from = message.user?.userName.toString()
            showSystemMsg(
                (holder as SystemViewHolder).name,
                from,
                (holder as SystemViewHolder).content
            )
        }
        holder.itemView.setOnClickListener {
            messageViewListener?.onItemClickListener(message)
        }
    }

    private var messageViewListener: MessageViewListener? = null

    interface MessageViewListener {
        fun onItemClickListener(message: AUIChatEntity?)
    }

    fun setMessageViewListener(messageViewListener: MessageViewListener?) {
        this.messageViewListener = messageViewListener
    }

    private fun showSystemMsg(name: TextView, nickName: String ,content: TextView) {
        val builder = StringBuilder()
        builder.append(nickName).append(" ")
        val span = SpannableString(builder.toString())
        name.text = span
        content.text = context.getString(R.string.voice_room_system_msg_member_add)
    }

    private fun showNormalText(isOwner: Boolean, con: TextView, nickName: String, content: String) {
        val builder = StringBuilder()
        if (isOwner) {
            builder.append("O").append(nickName).append(" : ").append(content)
        } else {
            builder.append(nickName).append(" : ").append(content)
        }
        if (!TextUtils.isEmpty(builder.toString()) && AUISmileUtils.containsKey(builder.toString())) {
            val span1: Spannable = AUISmileUtils.getSmiledText(context, builder.toString())
            if (isOwner) {
                span1.setSpan(
                    AUICenteredImageSpan(
                        context,
                        normalTagIcon,
                        0,
                        10
                    ), 0, 1, 0
                )
                span1.setSpan(
                    ForegroundColorSpan(
                        normalTitleColor
                    ),
                    0, nickName.length + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span1.setSpan(
                    ForegroundColorSpan(normalContentColor),
                    nickName.length + 4, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    nickName.length + 4,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            } else {
                span1.setSpan(
                    ForegroundColorSpan(
                        normalContentColor
                    ),
                    0, nickName.length + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span1.setSpan(
                    ForegroundColorSpan(normalContentColor),
                    nickName.length + 3, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                span1.setSpan(
                    StyleSpan(Typeface.BOLD),
                    0,
                    nickName.length + 3,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            con.setText(span1, TextView.BufferType.SPANNABLE)
            return
        }
        val span = SpannableString(builder.toString())
        if (isOwner) {
            span.setSpan(
                AUICenteredImageSpan(
                    context,
                    normalTagIcon,
                    0,
                    10
                ), 0, 1, 0
            )
            span.setSpan(
                ForegroundColorSpan(
                    normalTitleColor
                ),
                0, nickName.length + 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                nickName.length + 4,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(
                ForegroundColorSpan(normalContentColor),
                nickName.length + 4, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        } else {
            span.setSpan(
                ForegroundColorSpan(
                    normalTitleColor
                ),
                0, nickName.length + 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(
                StyleSpan(Typeface.BOLD),
                0,
                nickName.length + 3,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            span.setSpan(
                ForegroundColorSpan(normalContentColor),
                nickName.length + 3, builder.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        con.setText(span, TextView.BufferType.SPANNABLE)
    }

    override fun getItemCount(): Int {
        if (messages.size > 0){
            return messages.size
        }
        return 0
    }

    fun refresh() {
        // 刷新礼物列表 记录当前礼物列表数量 计算起始 position
        val positionStart = messages.size
        // 获取增量礼物列表插入原礼物列表
        val msgList = chatManager.getMsgList()
        Log.e("getMsgList","${msgList}")
        msgList.let { messages.addAll(it) }
        // 只更新新插入的礼物item
        if ( messages.size > 0 && positionStart < messages.size) {
            ThreadManager.getInstance().runOnMainThread{
                notifyItemRangeInserted(
                    positionStart,
                    messages.size
                )
            }
        }
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var content: TextView

        init {
            content = itemView.findViewById<View>(R.id.content) as TextView
        }
    }

    class SystemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var name: TextView
        var content: TextView
        var icon: AUiImageView

        init {
            name = itemView.findViewById<View>(R.id.name) as TextView
            content = itemView.findViewById<View>(R.id.content) as TextView
            icon = itemView.findViewById(R.id.icon_system)
        }
    }
}