package com.anthonyh.recordshow;
// 首先要先看一下当前手机可用的麦克风列表 （建议真机运行测试 不要使用 android studio 里面的虚拟机）
// 建议你阅读一下 AudioRecord 类中的方法
// https://developer.android.google.cn/reference/android/media/AudioRecord#getActiveMicrophones() 确认手机可用麦克风列表
//另外 我也不太确定是否可以这样分别获取两个声道的数据 你阅读过 AudioFormat 类中的 channel mask 相关内容了么
// https://developer.android.google.cn/reference/android/media/AudioFormat#channelMask
//如果 手机可用麦克风OK 分别获取声道数据OK 那么你可以录音完拿到文件然后使用python来画图看数据区别
// （android 画图不方便） 另外 其实在正常录音的情况下两个声道数据差异很小 你可以堵住一个麦克风来人为拉大差异
// （这也可以用来验证你分别获取声道数据有没有获得正确）
// 在Android Studio中，引用.so库，默认就是\src\main\jniLibs
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.anthonyh.recordshow.audio.agent.BaseAudioCustomAgent;
import com.anthonyh.recordshow.audio.agent.DifAgent;
import com.anthonyh.recordshow.audio.agent.MonoAgent;
import com.anthonyh.recordshow.audio.agent.StroAgent;
import com.anthonyh.recordshow.audio.record.DefaultRecord;
import com.anthonyh.recordshow.audio.record.RecordConfig;
import com.anthonyh.recordshow.audio.record.RecordException;
import com.anthonyh.recordshow.audio.record.RecordManger;
import com.anthonyh.recordshow.ui.ShowActivity;
import com.anthonyh.recordshow.widget.VadView;
import com.iflytek.cloud.SpeechUtility;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
// 实现的接口：两个按钮选择，然后填入文件名
public class MainActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
    Button button, startLocFileBt, iat_stream;
    VadView vadViewTop, vadViewBottom;
    EditText editText;
    RadioGroup radioGroup;  // 单选按钮控件
    String registId;
    RecordConfig recordConfig;
    BaseAudioCustomAgent audioCustomAgent;
    ExecutorService executorService;
    Future customFuture;
    private static final String TAG = "MainActivity";

    public static String AudioPath = "/sdcard/recordFile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  // 便于刷新界面
        setContentView(R.layout.activity_main);
        vadViewTop = findViewById(R.id.wav1);
        editText = findViewById(R.id.fileNameEd);
        vadViewBottom = findViewById(R.id.wav2);
        radioGroup = findViewById(R.id.channel);
        radioGroup.setOnCheckedChangeListener(this);  // 监听选中状态
        button = findViewById(R.id.recordButton);
        startLocFileBt = findViewById(R.id.startLoc);
        iat_stream = findViewById(R.id.iat_recognize_stream);
        refreshButton();
        initView();
        executorService = Executors.newFixedThreadPool(1);
        SpeechUtility.createUtility(MainActivity.this, "appid=" + "705c5357");
//        AudioPath = Environment.getDownloadCacheDirectory().getPath()+"/recordFile";
    }

    private void initView() {
        // 语音转写
        findViewById(R.id.iat_recognize_stream).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startActivity(new Intent(MainActivity.this, IatDemo.class));
            }
        });
    }

    private Toast mToast;

    private void showTip(final String str) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT);
        mToast.show();
    }


    private void refreshButton() {
        switch (RecordManger.getInstance().getRecordState()) {
            case RECORDING:
                button.setText("点击停止");
                break;
            case NONE:
                button.setText("点击录音");
                break;
        }

    }


    public void recordClick(View view) {
        RecordManger.RecordState recordState = RecordManger.getInstance().getRecordState();
        Log.e(TAG, "recordClick: " + recordState);
        if (recordState == RecordManger.RecordState.RECORDING) {
            stopRecord();
        } else {
            startRecord();
        }
    }


    private void startRecord() {
        try {
            String fileName = getFileName();
            if (TextUtils.isEmpty(fileName)) {
                Toast.makeText(getApplicationContext(), "请输入正确的文件名", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(getApplicationContext(), "开始录音", Toast.LENGTH_SHORT).show();
            recordConfig = getRecordConfig();  // 必须要建立此实例
            switch (radioGroup.getCheckedRadioButtonId()) {
                case R.id.mono:
                    vadViewTop.setVisibility(View.VISIBLE);
                    vadViewBottom.setVisibility(View.GONE);
                    audioCustomAgent = new MonoAgent(vadViewTop);
                    vadViewTop.startShow();
                    break;
                case R.id.sreo:
                    vadViewTop.setVisibility(View.VISIBLE);
                    vadViewBottom.setVisibility(View.VISIBLE);
                    audioCustomAgent = new StroAgent(vadViewTop, vadViewBottom);
                    vadViewTop.startShow();
                    vadViewBottom.startShow();
                    break;
                case R.id.dif:
                    vadViewTop.setVisibility(View.VISIBLE);
                    vadViewBottom.setVisibility(View.GONE);
                    audioCustomAgent = new DifAgent(vadViewTop);
                    vadViewTop.startShow();
                    break;
            }
            audioCustomAgent.setFilePath(AudioPath);
            audioCustomAgent.setFileName(fileName);
            registId = UUID.randomUUID().toString();
            RecordManger.getInstance().registAudioCustom(registId, audioCustomAgent);
            customFuture = executorService.submit(audioCustomAgent);
            RecordManger.getInstance().startRecord(recordConfig, new DefaultRecord());
            refreshButton();
            disableView();
        } catch (RecordException e) {
            e.printStackTrace();
        }
    }

    private String getFileName() {
        String fileName = editText.getText().toString().trim();
        if (!TextUtils.isEmpty(fileName))
            fileName += ".pcm";
        return fileName;
    }

    private void stopRecord() {
        Toast.makeText(getApplicationContext(), "结束录音", Toast.LENGTH_SHORT).show();
        editText.setText("");
        RecordManger.getInstance().unRegistAudioCustom(registId);
        RecordManger.getInstance().stopRecord();
        refreshButton();
        if (customFuture != null) {
            customFuture.cancel(true);
        }
        enableView();
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }

    private RecordConfig getRecordConfig() {
        int sampleRate = getSam();
        int channel = getChannel();
        int audioformat = AudioFormat.ENCODING_PCM_16BIT;  // 采样16bit
        int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channel,
                audioformat);
        RecordConfig recordConfig = new RecordConfig.Builder()
                .audioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
                .sampleRateInHz(sampleRate)
                .channelConfig(channel)
                .audioFormat(audioformat)
                .bufferSizeInBytes(10 * minBufferSize)
                .readBufferSize(30 * sampleRate / 1000 * 10)// 30ms
                .build();

        return recordConfig;
    }

    private int getSam() {
        return 16000;
    }

    private int getChannel() {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.mono:
                return AudioFormat.CHANNEL_IN_MONO;
            case R.id.sreo:
                return AudioFormat.CHANNEL_IN_STEREO;
            case R.id.dif:
                return AudioFormat.CHANNEL_IN_STEREO;
        }
        return AudioFormat.CHANNEL_IN_STEREO;
    }

    public void disableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(false);
        }
    }

    public void enableRadioGroup(RadioGroup testRadioGroup) {
        for (int i = 0; i < testRadioGroup.getChildCount(); i++) {
            testRadioGroup.getChildAt(i).setEnabled(true);
        }
    }

    private void enableView() {
        enableRadioGroup(radioGroup);
        editText.setEnabled(true);
        startLocFileBt.setEnabled(true);
    }

    private void disableView() {
        disableRadioGroup(radioGroup);
        editText.setEnabled(false);
        startLocFileBt.setEnabled(false);
    }

    public void startLocClick(View view) {
        startActivity(new Intent(this, ShowActivity.class));
    }
}
