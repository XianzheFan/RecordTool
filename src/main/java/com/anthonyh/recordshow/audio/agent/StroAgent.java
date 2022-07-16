package com.anthonyh.recordshow.audio.agent;

import android.util.Log;

import com.anthonyh.recordshow.audio.IAudioCustom;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;



public class StroAgent extends BaseAudioCustomAgent {
    IAudioCustom left, right;
    FileOutputStream fileOutputStream;

    public StroAgent(IAudioCustom left, IAudioCustom right) {
        this.left = left;
        this.right = right;
        try {
            fileOutputStream = new FileOutputStream("/sdcard/left.pcm");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static final String TAG = "StroAgent";

    boolean leftV = true;

    @Override
    protected void decodeAudio(byte[] audio) {
        Log.e(TAG, "decodeAudio: " + audio.length);
        byte[] leftAudio = new byte[audio.length / 2];
        byte[] rightAudio = new byte[audio.length / 2];
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
        left.addAudioArray(leftAudio);
        right.addAudioArray(rightAudio);
    }
}
// 单双声道一模一样？？？？？