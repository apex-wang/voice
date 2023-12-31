package io.agora.auikit.ui.barrage.impl

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import io.agora.auikit.R
import io.agora.auikit.databinding.VoiceBarrageLayoutBinding
import io.agora.auikit.model.AUIChatEntity
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.ui.barrage.AUIBarrageView
import io.agora.auikit.ui.barrage.listener.AUIBarrageItemClickListener
import io.agora.auikit.utils.DeviceTools

class AUIBarrageView : RelativeLayout,AUIBarrageView{
    private val mViewBinding = VoiceBarrageLayoutBinding.inflate(LayoutInflater.from(context))
    private var listener:AUIBarrageItemClickListener?=null
    private var isScrollBottom = false
    private var adapter:AUIBarrageAdapter?=null
    private var appearanceId:Int=0
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        addView(mViewBinding.root)
        val themeTa = context.obtainStyledAttributes(attrs, R.styleable.AUIBarrageView, defStyleAttr, 0)
        appearanceId = themeTa.getResourceId(R.styleable.AUIMicSeatItemView_aui_micSeatItem_appearance, 0)
        themeTa.recycle()
        initListener()
    }

    fun initView(chatManager: AUIChatManager){
        val typedArray = context.obtainStyledAttributes(appearanceId, R.styleable.AUIBarrageView)
        adapter = AUIBarrageAdapter(context,chatManager,typedArray)
        val linearLayout = LinearLayoutManager(context)
//        val scrollSpeedLinearLayoutManger = ScrollSpeedLinearLayoutManger(context)
//        //设置item滑动速度
//        scrollSpeedLinearLayoutManger.setSpeedSlow()
        mViewBinding.listview.layoutManager = linearLayout

        //设置item 间距
        val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        val drawable = GradientDrawable()
        drawable.setSize(0, DeviceTools.dp2px(context, 6f))
        itemDecoration.setDrawable(drawable)
        mViewBinding.listview.addItemDecoration(itemDecoration)
        mViewBinding.listview.adapter = adapter
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initListener(){
        mViewBinding.listview.setOnTouchListener(OnTouchListener { v, event ->
            listener?.onBarrageViewClickListener()
            false
        })

        adapter?.setMessageViewListener(object :AUIBarrageAdapter.MessageViewListener{
            override fun onItemClickListener(message: AUIChatEntity?) {
                listener?.onItemClickListener(message)
            }
        })

        mViewBinding.listview.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lm = recyclerView.layoutManager as LinearLayoutManager?
                val lastVisibleItemPosition = lm!!.findLastVisibleItemPosition()
                val totalCount = lm.itemCount
                if (lastVisibleItemPosition == totalCount - 1) {
                    val findLastVisibleItemPosition = lm.findLastVisibleItemPosition()
                    if (findLastVisibleItemPosition == lm.itemCount - 1) {
                        isScrollBottom = true
                    }
                } else {
                    isScrollBottom = false
                }
            }
        })
    }

    override fun setBarrageItemClickListener(listener: AUIBarrageItemClickListener?) {
        this.listener = listener
    }

    fun refresh() {
        if (adapter != null) {
            if (isScrollBottom) {
                refreshSelectLast()
            } else {
                adapter?.refresh()
            }
        }
    }

    fun refreshSelectLast() {
        if (adapter != null) {
            Log.e("apex","refreshSelectLast")
            adapter?.refresh()
            val position = adapter?.itemCount
            if (position != null) {
                mViewBinding.listview.smoothScrollToPosition( position - 1)
            }
        }
    }


    /**
     * 控制滑动速度的LinearLayoutManager
     */
    class ScrollSpeedLinearLayoutManger(private val context: Context) :
        LinearLayoutManager(context) {
        private var MILLISECONDS_PER_INCH = 0.03f
        override fun smoothScrollToPosition(
            recyclerView: RecyclerView,
            state: RecyclerView.State,
            position: Int
        ) {
            val linearSmoothScroller: LinearSmoothScroller =
                object : LinearSmoothScroller(recyclerView.context) {
                    override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                        return this@ScrollSpeedLinearLayoutManger
                            .computeScrollVectorForPosition(targetPosition)
                    }

                    //This returns the milliseconds it takes to
                    //scroll one pixel.
                    override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                        return MILLISECONDS_PER_INCH / displayMetrics.density
                        //返回滑动一个pixel需要多少毫秒
                    }
                }
            linearSmoothScroller.targetPosition = position
            startSmoothScroll(linearSmoothScroller)
        }

        fun setSpeedSlow() {
            //自己在这里用density去乘，希望不同分辨率设备上滑动速度相同
            //0.3f是自己估摸的一个值，可以根据不同需求自己修改
            MILLISECONDS_PER_INCH = context.resources.displayMetrics.density * 0.3f
        }

        fun setSpeedFast() {
            MILLISECONDS_PER_INCH = context.resources.displayMetrics.density * 0.03f
        }
    }


}

