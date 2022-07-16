package com.anthonyh.recordshow.audio.agent;

import com.anthonyh.recordshow.audio.IAudioCustom;

//继承自之前写的一个类，为单声道

public class MonoAgent extends BaseAudioCustomAgent {
    IAudioCustom iAudioCustom;


    public MonoAgent(IAudioCustom iAudioCustom) {
        this.iAudioCustom = iAudioCustom;
    }  // 构造函数

    @Override
    protected void decodeAudio(byte[] audio) {

        if (iAudioCustom != null) {
            iAudioCustom.addAudioArray(audio);
        }
    }
}
//接口通过实现类来实例化接口：public interface Flyable { void fly(); } 定义接口
//public class Bird implements Flyable { public void fly() {...} }
//实现类，不需实现所有抽象方法
//Flyable fly = new Bird();  测试类

//采用匿名内部类创建  Flyable fly = new Flyable() {
//			@Override
//			public void fly() {}  }    创建匿名内部类一定要实现里面所有的抽象方法