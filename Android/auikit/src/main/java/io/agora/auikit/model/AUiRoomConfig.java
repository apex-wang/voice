package io.agora.auikit.model;

import android.util.SparseArray;
import android.view.View;

import androidx.annotation.NonNull;

public class AUiRoomConfig {
    public static final int TOKEN_RTC_VOICE_SERVICE = 98;
    public static final int TOKEN_RTM_VOICE_LOGIN = 99;
    public static final int TOKEN_RTM_LOGIN = 100;
    public static final int TOKEN_RTM_SERVICE = 101;
    public static final int TOKEN_RTM_KTV = 102;
    public static final int TOKEN_RTC_SERVICE = 200;
    public static final int TOKEN_RTC_KTV_CHORUS = 201;

    public @NonNull
    final String voiceChannelName;
    public @NonNull
    final String voiceChorusChannelName;

    public @NonNull SparseArray<String> tokenMap = new SparseArray<>();

    public int themeId = View.NO_ID;

    public AUiRoomConfig(String roomId) {
        this(roomId + "_rtc", roomId + "_rtc_ex");
    }

    public AUiRoomConfig(@NonNull String voiceChannelName, @NonNull String voiceChorusChannelName) {
        this.voiceChannelName = voiceChannelName;
        this.voiceChorusChannelName = voiceChorusChannelName;
    }

}
