package com.example.testssss;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class PlaySound {
    private Context context;
    private final int TOTOL_VALUE = 6;
    private final int TOTOL_REGION = 12;
    public final int SOUND_COUNT = TOTOL_VALUE*TOTOL_REGION;       // 預計要載入的聲音數量
    private SoundPool sp = null;                    // 宣告SoundPool物件
    private int[] nSoundId = new int[SOUND_COUNT];  // 存放音效ID的陣列
    private String[] Number = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "eleven", "twelve"};

    public PlaySound(Context context) {
        this.context = context;
        this.initSoundPool();
    }

    private void initSoundPool()
    {
        // 建立SoundPool物件
        sp = new SoundPool(SOUND_COUNT,                 // 要用SoundPool來管理多少個音效
                AudioManager.STREAM_MUSIC,  // 聲音類型, 一般為STREAM_MUSIC
                0);                         // 聲音品質, 0 = 預設品質

        for (int region = 0; region < TOTOL_REGION; region++) {
            for (int value = 0; value < TOTOL_VALUE; value++) {
                String name = new String("raw/"+Number[region] + "_" + Number[9-value]);
                int resID = this.context.getResources().getIdentifier(name, null, this.context.getPackageName());
                nSoundId[region * TOTOL_VALUE + value] = sp.load(this.context,         // 要使用SoundPool的Context, 也就是目前的Activity
                        resID, // 音效資源
                        1);           // 優先權, 目前無作用, 設定1來保持未來的相容性
            }
        }
    }

    public void play(int region, int value) {
        sp.play(nSoundId[(region-1) * TOTOL_VALUE + 10-value],  // 音效ID
                1.0F,         // 左聲道音量(1.0為原始音量)
                1.0F,         // 右聲道音量(1.0為原始音量)
                0,            // priority沒用到, 目前請設為0
                0,            // 是否要重複播放, 0 = 不重複, -1 = 一直重複
                1.0F);        // 撥放的速度(1.0 = 原速, 大於1表示加速, 小於1表示減速)
    }
}
