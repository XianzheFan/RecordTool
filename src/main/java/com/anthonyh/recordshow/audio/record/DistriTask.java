package com.anthonyh.recordshow.audio.record;

import android.os.Handler;
import android.os.Looper;

import com.anthonyh.recordshow.audio.IAudioCustom;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;


public class DistriTask implements Runnable {
    LinkedBlockingQueue<byte[]> linkedBlockingQueue;  // 单向列表阻塞队列
    Map<String, IAudioCustom> customMap;  // Map<String, Object> 为键值对类型
    Handler handler = new Handler(Looper.getMainLooper());
// 如果不带参数实例化：Handler handler = new Handler();那么这个会默认用当前线程的looper
// Looper.getMainLooper() 代表放在主UI线程下处理
    public DistriTask(LinkedBlockingQueue<byte[]> linkedBlockingQueue, Map<String, IAudioCustom> customMap) {
        this.linkedBlockingQueue = linkedBlockingQueue;
        this.customMap = customMap;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                byte[] content = linkedBlockingQueue.take();  // 从队列中取出数据
                distriContent(content);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void distriContent(final byte[] value) {
        Collection<IAudioCustom> customCollection = customMap.values();
        // Collection 集合：只能存放对象的引用，可以存放不同类型的元素
        Iterator<IAudioCustom> it = customCollection.iterator();
        // Iterator 迭代器 it
        while (it.hasNext()) {  // 如果迭代器存在下一个元素
            final IAudioCustom audioCustom = it.next();  // 输出下一个元素
            handler.post(new Runnable() {  // 在子线程中更新主线程的UI
                @Override
                public void run() {
                    audioCustom.addAudioArray(value);
                }
            });
        }
    }
}
