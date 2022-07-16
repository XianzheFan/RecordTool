package com.anthonyh.recordshow.audio.agent;

import android.util.Log;

import com.anthonyh.recordshow.NsUtils;
import com.anthonyh.recordshow.audio.IAudioCustom;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

// CHANNEL_OUT_STEREO
// CHANNEL_OUT_FRONT_LEFT   CHANNEL_IN_LEFT
// CHANNEL_OUT_FRONT_RIGHT    CHANNEL_IN_RIGHT

public class DifAgent extends BaseAudioCustomAgent {
    static IAudioCustom dif;
    FileOutputStream fileOutputStream;
    //ns 与 nxs
    private static final boolean DO_NS = true;


    public DifAgent(IAudioCustom dif) {
        this.dif = dif;
        try {
            fileOutputStream = new FileOutputStream("/sdcard/dif.pcm");
            this.fileOutputStream = fileOutputStream;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        doWork(difAudio);
    }

    static void doWork(byte[] difAudio) {
        if (DO_NS) {
            doNs(difAudio);  // 之前默认选第一种
        } else {
            doNsx(difAudio);
        }
    }

    static void doNs(byte[] difAudio) {  // 定点数运算
        try {
            NsUtils nsUtils = new NsUtils();
            nsUtils.useNs().setNsConfig(16000, 2).prepareNs();
            byte[] buffer = new byte[320];
            short[] inputData = new short[160];
            short[] outData = new short[160];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
            // wrap方法获得ByteBuffer的实例：缓冲区的数据存放在byte数组中
            // ByteOrder字节在内存中的组织，小端LITTLE_ENDIAN
            dif.addAudioArray(shortArrayToByteArray(outData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void doNsx(byte[] difAudio) {  // 浮点数运算
        try {
            NsUtils nsUtils = new NsUtils();
            nsUtils.useNsx().setNsxConfig(16000, 2).prepareNsx();
            byte[] buffer = new byte[320];
            short[] inputData = new short[160];
            short[] outData = new short[160];
            ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(inputData);
            int ret = nsUtils.nsxProcess(inputData, null, outData, null);
            dif.addAudioArray(shortArrayToByteArray(outData));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // shortArray to byteArray
    static byte[] shortArrayToByteArray(short[] data) {
        byte[] byteVal = new byte[data.length * 2];
        for (int i = 0; i < data.length; i++) {
            byteVal[i * 2] = (byte) (data[i] & 0xff);
            byteVal[i * 2 + 1] = (byte) ((data[i] & 0xff00) >> 8);
        }
        return byteVal;
    }


    private static final String TAG = "DifAgent";

    boolean leftV = true;

    @Override
    protected void decodeAudio(byte[] audio) {
        Log.e(TAG, "decodeAudio: " + audio.length);
        byte[] leftAudio = new byte[audio.length / 2];
        byte[] rightAudio = new byte[audio.length / 2];
        byte[] difAudio = new byte[audio.length / 2];
        int leftIndex = 0;
        int rightIndex = 0;

        for (int i = 0; i < audio.length; i++) {
            if (leftV) {
                leftAudio[leftIndex] = audio[i];
                i++;
                leftIndex++;
                leftAudio[leftIndex] = audio[i];
                leftIndex++;
            } else {
                rightAudio[rightIndex] = audio[i];
                i++;
                rightIndex++;
                rightAudio[rightIndex] = audio[i];
                rightIndex++;
            }
// 说明录制的是16bit，所以每个采样的数据占两位*2，先左声道（2位）后右声道（2位）
            leftV = !leftV;
        }
        for (int i = 0; i < audio.length; i++) {
            difAudio[i] = (byte) (leftAudio[i] - rightAudio[i]);
        }
//        dif.addAudioArray(difAudio);
        doWork(difAudio);
    }
}
// 显示不出来？？？？？？