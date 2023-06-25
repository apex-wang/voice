package io.agora.asceneskit.voice.dialog

import android.graphics.Typeface
import android.os.Bundle
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.agora.asceneskit.R
import io.agora.asceneskit.databinding.VoiceRoomApplyLayoutBinding
import io.agora.asceneskit.voice.bean.VoiceRoomBean
import io.agora.asceneskit.voice.fragment.VoiceRoomApplyListFragment
import io.agora.auikit.model.AUiUserInfo
import io.agora.auikit.ui.basic.AUIBaseSheetDialog
import io.agora.auikit.utils.ResourcesTools

class VoiceRoomApplyDialog : AUIBaseSheetDialog<VoiceRoomApplyLayoutBinding>() {

    companion object {
        const val KEY_ROOM_APPLY_BEAN = "room_apply_bean"
        const val KEY_CURRENT_ITEM = "current_Item"
    }

    private val roomBean: VoiceRoomBean by lazy {
        arguments?.getSerializable(KEY_ROOM_APPLY_BEAN) as VoiceRoomBean
    }

    private val currentItem: Int by lazy {
        arguments?.getInt(KEY_CURRENT_ITEM, 0) ?: 0
    }

    private var adapter:RoomApplyFragmentAdapter?=null
    private var listener:VoiceApplyDialogEventListener?=null

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): VoiceRoomApplyLayoutBinding {
        return VoiceRoomApplyLayoutBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFragmentAdapter()
    }

    fun setApplyDialogListener(listener:VoiceApplyDialogEventListener){
        this.listener = listener
    }

    private fun initFragmentAdapter() {
        activity?.let { fragmentActivity->
            adapter = RoomApplyFragmentAdapter(fragmentActivity,roomBean,listener)
            binding?.apply {
                setOnApplyWindowInsets(root)
                vpApplyLayout.adapter = adapter
                val tabMediator = TabLayoutMediator(tabApplyLayout, vpApplyLayout) { tab, position ->
                    val customView =
                        LayoutInflater.from(root.context).inflate(R.layout.voice_view_room_tab_item, tab.view, false)
                    val tabText = customView.findViewById<TextView>(R.id.mtTabText)
                    tab.customView = customView
                    if (position == RoomApplyFragmentAdapter.PAGE_INDEX0) {
                        tabText.text = getString(R.string.voice_room_apply_list)
                        onTabLayoutSelected(tab)
                    } else {
                        onTabLayoutUnselected(tab)
                    }

                }
                tabMediator.attach()
                tabApplyLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab?) {
                        onTabLayoutSelected(tab)
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab?) {
                        onTabLayoutUnselected(tab)
                    }

                    override fun onTabReselected(tab: TabLayout.Tab?) {
                    }
                })
                vpApplyLayout.setCurrentItem(currentItem, false)
            }
        }
    }

    fun refreshData(userList:MutableList<AUiUserInfo>?){
        adapter?.refreshData(userList)
    }

    private fun onTabLayoutSelected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_040925))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.VISIBLE
        }
    }

    private fun onTabLayoutUnselected(tab: TabLayout.Tab?) {
        tab?.customView?.let {
            val tabText = it.findViewById<TextView>(R.id.mtTabText)
            tabText.setTextColor(ResourcesTools.getColor(resources, R.color.voice_dark_grey_color_6c7192))
            tabText.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
            val tabTip = it.findViewById<View>(R.id.vTabTip)
            tabTip.visibility = View.GONE
        }
    }

    class RoomApplyFragmentAdapter constructor(
        fragmentActivity: FragmentActivity,
        roomBean: VoiceRoomBean,
        event:VoiceApplyDialogEventListener?
    ) : FragmentStateAdapter(fragmentActivity), VoiceRoomApplyListFragment.ApplyEventListener {

        companion object {
            const val PAGE_INDEX0 = 0
            const val PAGE_INDEX1 = 1
        }

        private val fragments: SparseArray<Fragment> = SparseArray()
        private var listener:VoiceApplyDialogEventListener?=null

        init {
            this.listener = event
            with(fragments) {
                put(PAGE_INDEX0, VoiceRoomApplyListFragment.getInstance(fragmentActivity,roomBean))
            }
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = fragments[position]
            if (PAGE_INDEX0 == position){
                (fragment as VoiceRoomApplyListFragment).setApplyEventListener(this)
            }
            return fragment
        }

        override fun getItemCount(): Int {
            return fragments.size()
        }

        fun refreshData(userList:MutableList<AUiUserInfo>?){
            fragments.forEach { key, value ->
                if (key == PAGE_INDEX0){
                    val fragment = value as VoiceRoomApplyListFragment
                    fragment.refreshData(userList)
                }
            }
        }

        override fun onApplyItemClick(view: View, applyIndex: Int, user: AUiUserInfo, position: Int) {
            this.listener?.onApplyItemClick(view, applyIndex, user,position)
        }
    }

}