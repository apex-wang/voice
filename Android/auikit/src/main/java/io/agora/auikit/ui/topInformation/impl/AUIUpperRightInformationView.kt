package io.agora.auikit.ui.topInformation.impl

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.imageview.ShapeableImageView
import io.agora.auikit.R
import io.agora.auikit.databinding.AuiUpperRightInformationLayoutBinding
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.ui.topInformation.AUITopInformationView
import io.agora.auikit.ui.topInformation.listener.AUIUpperRightActionListener

class AUIUpperRightInformationView : ConstraintLayout, AUITopInformationView {
    private val mRoomViewBinding = AuiUpperRightInformationLayoutBinding.inflate(LayoutInflater.from(context))
    private var aUpperRightListener:AUIUpperRightActionListener? = null
    private var aContext: Context? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        this.aContext = context
        addView(mRoomViewBinding.root)
        initListener()
    }

    private fun initListener(){
        mRoomViewBinding.llMemberRank.setOnClickListener{
            aUpperRightListener?.onMemberRankClickListener(it)
        }

        mRoomViewBinding.btnUserMore.setOnClickListener{
            aUpperRightListener?.onUpperRightUserMoreClickListener(it)
        }

        mRoomViewBinding.btnShutDown.setOnClickListener{
            aUpperRightListener?.onUpperRightShutDownClickListener(it)
        }
    }

    override fun setUpperRightActionListener(listener: AUIUpperRightActionListener?) {
        this.aUpperRightListener = listener
    }

    /**
     * 设置排行榜前三
     */
    fun setRankData(rankList: List<AUiUserInfo>?){
        val size = rankList?.size
        size?.let {
            if (it > 3){
                setMemberView(3,rankList)
                return
            }
            setMemberView(it,rankList)
        }

    }

    private fun setMemberView(size:Int,rankList:List<AUiUserInfo>){
        when(size){
            0 -> {
                mRoomViewBinding.ivMember1.visibility = GONE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
            }
            1 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = GONE
                mRoomViewBinding.ivMember3.visibility = GONE
                setResources(rankList[0].userAvatar,mRoomViewBinding.ivMember1)
            }
            2 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = GONE
                setResources(rankList[0].userAvatar,mRoomViewBinding.ivMember1)
                setResources(rankList[1].userAvatar,mRoomViewBinding.ivMember2)
            }
            3 -> {
                mRoomViewBinding.ivMember1.visibility = VISIBLE
                mRoomViewBinding.ivMember2.visibility = VISIBLE
                mRoomViewBinding.ivMember3.visibility = VISIBLE
                setResources(rankList[0].userAvatar,mRoomViewBinding.ivMember1)
                setResources(rankList[1].userAvatar,mRoomViewBinding.ivMember2)
                setResources(rankList[2].userAvatar,mRoomViewBinding.ivMember3)
            }
        }
    }

    private fun setResources(url:String,view: ShapeableImageView){
        aContext?.let {
            Glide.with(it)
                .load(url)
                .error(R.drawable.aui_upper_left_avatar)
                .apply(RequestOptions.circleCropTransform())
                .into(view)
        }
    }

    private fun setCount(){

    }

    /**
     * 设置右侧icon
     */
    fun setRightIconResources(url:String,view: ShapeableImageView){
        setResources(url,view)
    }



}