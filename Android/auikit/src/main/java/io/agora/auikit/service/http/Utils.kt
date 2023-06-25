package io.agora.auikit.service.http

import android.util.Log
import io.agora.auikit.service.callback.AUiException
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response

class Utils {
    companion object {

        @JvmStatic
        fun <T>errorFromResponse(response: Response<T>): AUiException {
            val errorMsg = response.errorBody()?.string()
            var code = -1
            var msg = "error"
            if (errorMsg != null) {
                val obj = JSONObject(errorMsg)
                try {
                    code = obj.getInt("code")
                    msg = obj.getString("message")
                }catch (exception:JSONException){
                    Log.e("errorFromResponse",exception.message.toString())
                }
            }
            return AUiException(code, msg)
        }

    }
}