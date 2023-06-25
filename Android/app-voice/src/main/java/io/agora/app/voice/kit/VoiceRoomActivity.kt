package io.agora.app.voice.kit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.agora.app.voice.databinding.VoiceRoomActivityBinding
import io.agora.asceneskit.voice.IVoiceRoomService
import io.agora.scene.show.utils.PermissionHelp

class VoiceRoomActivity : AppCompatActivity(),
    IVoiceRoomService.IVoiceRoomRespDelegate {
    private val mViewBinding by lazy { VoiceRoomActivityBinding.inflate(LayoutInflater.from(this)) }
    private val mPermissionHelp = PermissionHelp(this)

    companion object {
        private var roomService: IVoiceRoomService? = null
        private var onRoomDestroy: ((Boolean) -> Unit)? = null
        private var onRoomCreated: (() -> Unit)? = null
        private var ThemeId = View.NO_ID

        fun launch(
            context: Context,
            themeId: Int,
            roomService: IVoiceRoomService,
            onRoomCreated: () -> Unit,
            onRoomDestroy: (Boolean) -> Unit
        ) {
            Companion.roomService = roomService
            Companion.onRoomCreated = onRoomCreated
            Companion.onRoomDestroy = onRoomDestroy
            Companion.ThemeId = themeId

            val intent = Intent(context, VoiceRoomActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomService?.unbindRespDelegate(this)
        roomService = null
        onRoomDestroy?.invoke(false)
        onRoomDestroy = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ThemeId != View.NO_ID) {
            setTheme(ThemeId)
        }
        setContentView(mViewBinding.root)
        mViewBinding.VoiceRoomView.setFragmentActivity(this)

        roomService?.bindRespDelegate(this)
        mPermissionHelp.checkMicPerm(
            {
                mViewBinding.VoiceRoomView.bindService(roomService)
                onRoomCreated?.invoke()
                onRoomCreated = null
            },
            {
                onRoomDestroy?.invoke(true)
                onRoomDestroy = null
                finish()
            },
            true
        )

    }

    override fun onRoomExitedOrDestroyed() {
        super.onRoomExitedOrDestroyed()
        finish()
    }

    override fun onBackPressed() {
        mViewBinding.VoiceRoomView.showExitDialog()
    }



}