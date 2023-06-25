package io.agora.auikit.ui.primary.impl;


import io.agora.auikit.R;
import io.agora.auikit.model.AUIExpressionIcon;

public class AUIDefaultExpressionData {
    
    private static String[] emojis = new String[]{
        AUISmileUtils.ee_1,
        AUISmileUtils.ee_2,
        AUISmileUtils.ee_3,
        AUISmileUtils.ee_4,
        AUISmileUtils.ee_5,
        AUISmileUtils.ee_6,
        AUISmileUtils.ee_7,
        AUISmileUtils.ee_8,
        AUISmileUtils.ee_9,
        AUISmileUtils.ee_10,
        AUISmileUtils.ee_11,
        AUISmileUtils.ee_12,
        AUISmileUtils.ee_13,
        AUISmileUtils.ee_14,
        AUISmileUtils.ee_15,
        AUISmileUtils.ee_16,
        AUISmileUtils.ee_17,
        AUISmileUtils.ee_18,
        AUISmileUtils.ee_19,
        AUISmileUtils.ee_20,
        AUISmileUtils.ee_21,
        AUISmileUtils.ee_22,
        AUISmileUtils.ee_23,
        AUISmileUtils.ee_24,
        AUISmileUtils.ee_25,
        AUISmileUtils.ee_26,
        AUISmileUtils.ee_27,
        AUISmileUtils.ee_28,
        AUISmileUtils.ee_29,
        AUISmileUtils.ee_30,
        AUISmileUtils.ee_31,
        AUISmileUtils.ee_32,
        AUISmileUtils.ee_33,
        AUISmileUtils.ee_34,
        AUISmileUtils.ee_35,
        AUISmileUtils.ee_36,
        AUISmileUtils.ee_37,
        AUISmileUtils.ee_38,
        AUISmileUtils.ee_39,
        AUISmileUtils.ee_40,
        AUISmileUtils.ee_41,
        AUISmileUtils.ee_42,
        AUISmileUtils.ee_43,
        AUISmileUtils.ee_44,
        AUISmileUtils.ee_45,
        AUISmileUtils.ee_46,
        AUISmileUtils.ee_47,
        AUISmileUtils.ee_48,
        AUISmileUtils.ee_49,
        AUISmileUtils.ee_50,
        AUISmileUtils.ee_51,
        AUISmileUtils.ee_52,
    };
    
    private static int[] icons = new int[]{
        R.drawable.voice_ee_1,
        R.drawable.voice_ee_2,
        R.drawable.voice_ee_3,
        R.drawable.voice_ee_4,
        R.drawable.voice_ee_5,
        R.drawable.voice_ee_6,
        R.drawable.voice_ee_7,
        R.drawable.voice_ee_8,
        R.drawable.voice_ee_9,
        R.drawable.voice_ee_10,
        R.drawable.voice_ee_11,
        R.drawable.voice_ee_12,
        R.drawable.voice_ee_13,
        R.drawable.voice_ee_14,
        R.drawable.voice_ee_15,
        R.drawable.voice_ee_16,
        R.drawable.voice_ee_17,
        R.drawable.voice_ee_18,
        R.drawable.voice_ee_19,
        R.drawable.voice_ee_20,
        R.drawable.voice_ee_21,
        R.drawable.voice_ee_22,
        R.drawable.voice_ee_23,
        R.drawable.voice_ee_24,
        R.drawable.voice_ee_25,
        R.drawable.voice_ee_26,
        R.drawable.voice_ee_27,
        R.drawable.voice_ee_28,
        R.drawable.voice_ee_29,
        R.drawable.voice_ee_30,
        R.drawable.voice_ee_31,
        R.drawable.voice_ee_32,
        R.drawable.voice_ee_33,
        R.drawable.voice_ee_34,
        R.drawable.voice_ee_35,
        R.drawable.voice_ee_36,
        R.drawable.voice_ee_37,
        R.drawable.voice_ee_38,
        R.drawable.voice_ee_39,
        R.drawable.voice_ee_40,
        R.drawable.voice_ee_41,
        R.drawable.voice_ee_42,
        R.drawable.voice_ee_43,
        R.drawable.voice_ee_44,
        R.drawable.voice_ee_45,
        R.drawable.voice_ee_46,
        R.drawable.voice_ee_47,
        R.drawable.voice_ee_48,
        R.drawable.voice_ee_49,
        R.drawable.voice_ee_50,
        R.drawable.voice_ee_51,
        R.drawable.voice_ee_52,

    };
    
    
    private static final AUIExpressionIcon[] DATA = createData();
    
    private static AUIExpressionIcon[] createData(){
        AUIExpressionIcon[] datas = new AUIExpressionIcon[icons.length];
        for(int i = 0; i < icons.length; i++){
            datas[i] = new AUIExpressionIcon(icons[i], emojis[i], AUIExpressionIcon.Type.NORMAL);
        }
        return datas;
    }
    
    public static AUIExpressionIcon[] getData(){
        return DATA;
    }
}
