package io.agora.auikit.service.imp

import android.util.Log
import io.agora.auikit.model.AUIGiftEntity
import io.agora.auikit.model.AUIGiftTabEntity
import io.agora.auikit.model.AUiRoomContext
import io.agora.auikit.service.IAUIGiftsManagerService
import io.agora.auikit.service.callback.AUIGiftListCallback
import io.agora.auikit.service.callback.AUiCallback
import io.agora.auikit.service.callback.AUiException
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.gift.GiftInterface
import io.agora.auikit.service.imkit.AUIChatManager
import io.agora.auikit.service.rtm.AUiRtmManager
import io.agora.auikit.service.rtm.AUiRtmMsgProxyDelegate
import io.agora.auikit.utils.DelegateHelper
import io.agora.auikit.utils.GsonTools
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response

private const val giftKey = "AUIChatRoomGift"
class AUIGiftServiceImpI constructor(
    private val roomContext: AUiRoomContext,
    private val channelName: String,
    private val rtmManager: AUiRtmManager,
    private val chatManager:AUIChatManager
) : IAUIGiftsManagerService,AUiRtmMsgProxyDelegate {

    private val TAG = "AUIGiftServiceImpI"
    private val delegateHelper = DelegateHelper<IAUIGiftsManagerService.AUIGiftRespDelegate>()

    init {
        Log.e("apex","sub gift $giftKey")
        rtmManager.subscribeMsg(channelName, giftKey, this)
    }

    override fun getGiftsFromService(callback: AUIGiftListCallback?) {
        HttpManager.getService(GiftInterface::class.java)
            .fetchGiftInfo()
            .enqueue(object : retrofit2.Callback<CommonResp<List<AUIGiftTabEntity>>> {
                override fun onResponse(call: Call<CommonResp<List<AUIGiftTabEntity>>>
                , response: Response<CommonResp<List<AUIGiftTabEntity>>>
                ) {
                    val rsp = response.body()?.data
                    if (response.code() == 200 && rsp != null) {
                        callback?.onResult(null,rsp)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response),mutableListOf<AUIGiftTabEntity>())
                    }
                }
                override fun onFailure(call: Call<CommonResp<List<AUIGiftTabEntity>>>, t: Throwable) {
                    callback?.onResult(AUiException(-1, t.message),mutableListOf<AUIGiftTabEntity>())
                }
            })
    }

    override fun sendGift(gift: AUIGiftEntity, callback: AUiCallback) {
        rtmManager.sendGiftMetadata(channelName,gift,callback)
    }

    override fun bindRespDelegate(delegate: IAUIGiftsManagerService.AUIGiftRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun unbindRespDelegate(delegate: IAUIGiftsManagerService.AUIGiftRespDelegate?) {
        delegateHelper.bindDelegate(delegate)
    }

    override fun getContext() = roomContext

    override fun getChannelName() = channelName

    override fun onMsgDidChanged(channelName: String, key: String, value: Any) {
        Log.d("apex", "onMsgDidChanged: $key")
        if (key == giftKey){
            val gift = JSONObject(value.toString())
            GsonTools.toBean(gift["messageInfo"].toString(), AUIGiftEntity::class.java)?.let { it ->
                chatManager.addGiftList(it)
                this.delegateHelper.notifyDelegate {
                    it.onReceiveGiftMsg(channelName)
                }
            }
        }
    }
}