package io.agora.auikit.ui.likeView
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import io.agora.auikit.R
import io.agora.auikit.ui.basic.AUiImageView
import java.lang.ref.WeakReference
import java.util.*

class LikeLayout : RelativeLayout, View.OnClickListener {

    private var mAnimator: AbstractPathAnimator? = null
    private var attrs: AttributeSet? = null
    private var defStyleAttr = 0
    private var onHearLayoutListener: OnHearLayoutListener? = null
    private var nowTime: Long = 0
    private var lastTime: Long = 0
    private val random = Random()
    private var drawableIds:Array<Int> = emptyArray()
    private var imageView: AUiImageView? = null

    fun setOnHearLayoutListener(onHearLayoutListener: OnHearLayoutListener?) {
        this.onHearLayoutListener = onHearLayoutListener
    }

    interface OnHearLayoutListener {
        fun onAddFavor(): Boolean
        fun onLikeClick(view: View?)
    }

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr) {
        findViewById(context)
        imageView?.setOnClickListener(this)
    }

    init {
        heartHandler = HeartHandler(this)
    }

    companion object {
        const val MSG_SHOW = 1
        private var heartHandler: HeartHandler? = null
        private var heartThread: HeartThread? = null
    }

    private fun findViewById(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.voice_widget_gift_periscope, this)
        imageView = view.findViewById<AUiImageView>(R.id.img)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.voice_icon_like)
        dHeight = bitmap.width
        dWidth = bitmap.height
        textHight = sp2px(getContext(), 20f) + dHeight / 2
        pointx = dWidth //随机上浮方向的x坐标
        bitmap.recycle()
    }


    fun getLikeView(): AUiImageView? {
        return imageView
    }

    fun setLikeViewIcon(ResId:Int){
        imageView?.setImageResource(ResId)
    }

    fun setLikeViewSize(width:Int,height:Int){
        val lp = imageView?.layoutParams
        lp.let {
            it?.width = width
            it?.height = height
            imageView?.layoutParams = it
        }
    }

    private var mHeight = 0
    private var mWidth = 0
    private var textHight = 0
    private var dHeight = 0
    private var dWidth = 0
    private var initX = 0
    private var pointx = 0

    private fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        val a = context.obtainStyledAttributes(
            attrs, R.styleable.voice_LikeLayout, defStyleAttr, 0
        )
        if (pointx in 0..initX) {
            pointx -= 10
        } else if (pointx >= -initX && pointx <= 0) {
            pointx += 10
        } else pointx = initX
        mAnimator = PathAnimator(
            AbstractPathAnimator.Config.fromTypeArray(
                a,
                initX.toFloat(),
                textHight.toFloat(),
                pointx,
                dWidth,
                dHeight
            )
        )
        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        //获取本身的宽高 这里要注意,测量之后才有宽高
        mWidth = measuredWidth
        mHeight = measuredHeight
        initX = mWidth / 2 - dWidth / 2
    }

    fun getAnimator(): AbstractPathAnimator? {
        return mAnimator
    }

    fun setAnimator(animator: AbstractPathAnimator?) {
        clearAnimation()
        mAnimator = animator
    }

    override fun clearAnimation() {
        for (i in 0 until childCount) {
            getChildAt(i).clearAnimation()
        }
        removeAllViews()
    }

    private val defaultDrawableIds = intArrayOf(
        R.drawable.voice_icon_like_1,
        R.drawable.voice_icon_like_2,
        R.drawable.voice_icon_like_3,
        R.drawable.voice_icon_like_4,
        R.drawable.voice_icon_like_5,
        R.drawable.voice_icon_like_6
    )

    fun setDrawableIds(drawableIds: Array<Int>){
        this.drawableIds = drawableIds
    }

      fun addFavor() {
        val likeView = LikeView(context)
        if (drawableIds.isNotEmpty()){
            drawableIds.let {
                likeView.setDrawable(it[random.nextInt(it.size)])
            }
        }else{
            likeView.setDrawable(defaultDrawableIds[random.nextInt(5)])
        }
        init(attrs, defStyleAttr)
        mAnimator?.start(likeView, this)
    }


    private val sizeTable = intArrayOf(
        9, 99, 999, 9999, 99999, 999999, 9999999,
        99999999, 999999999, Int.MAX_VALUE
    )

    private fun sizeOfInt(x: Int): Int {
        var i = 0
        while (true) {
            if (x <= sizeTable[i]) return i + 1
            i++
        }
    }

    fun addFavor(s: Int) {
        var size = s
        size = when (sizeOfInt(size)) {
            1 -> size % 10
            else -> size % 100
        }
        if (size == 0) return
        nowTime = System.currentTimeMillis()
        var time: Long = nowTime - lastTime
        if (lastTime == 0L) time = (2 * 1000).toLong() //第一次分为2秒显示完
        time /= (size + 15)
        if (heartThread == null) {
            heartThread = HeartThread()
        }
        if (heartHandler == null) {
            heartHandler = HeartHandler(this)
            heartHandler!!.post(heartThread!!)
        }
        heartThread!!.addTask(time, size)
        lastTime = nowTime
    }

    fun addHeart(color: Int) {
        val likeView = LikeView(context)
        likeView.setColor(color)
        init(attrs, defStyleAttr)
        mAnimator?.start(likeView, this)
    }

    fun addHeart(color: Int, heartResId: Int, heartBorderResId: Int) {
        val likeView = LikeView(context)
        likeView.setColorAndDrawables(color, heartResId, heartBorderResId)
        init(attrs, defStyleAttr)
        mAnimator?.start(likeView, this)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.img) {
            onHearLayoutListener.let {
                it?.onLikeClick(v)
                val isAdd = it?.onAddFavor()
                if (isAdd != null && isAdd){
                    addFavor()
                }
            }
        }
    }


    class HeartHandler(layout: LikeLayout): Handler(Looper.getMainLooper()){
        private val wf: WeakReference<LikeLayout>
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            wf.get()?.run {
                when (msg.what) {
                    MSG_SHOW -> addFavor()
                    else -> {}
                }
            }
        }
        init {
            wf = WeakReference(layout)
        }
    }

    class HeartThread : Runnable {
        private var time: Long = 0
        private var allSize = 0
        fun addTask(time: Long, size: Int) {
            this.time = time
            allSize += size
        }

        fun clean() {
            allSize = 0
        }

        override fun run() {
            if (heartHandler == null) return
            if (allSize > 0) {
                heartHandler?.sendEmptyMessage(MSG_SHOW)
                allSize--
            }
            heartHandler?.postDelayed(this, time)
        }
    }

    fun clean() {
        heartThread?.clean()
        heartHandler?.removeCallbacksAndMessages(null)
    }

    fun release() {
        heartThread?.let { heartHandler?.removeCallbacks(it)}
        heartThread = null
        heartHandler = null
    }

}





