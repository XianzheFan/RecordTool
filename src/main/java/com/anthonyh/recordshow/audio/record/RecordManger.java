package com.anthonyh.recordshow.audio.record;

import android.text.TextUtils;
import android.util.Log;

import com.anthonyh.recordshow.audio.IAudioCustom;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;


public class RecordManger {

    private static final RecordManger ourInstance = new RecordManger();

    Future recordFuture;
// Future表示一个可能还没有完成的异步任务的结果，针对这个结果可以添加Callback以便在任务执行成功或失败后作出相应的操作
    DistriTask distriTask;
    LinkedBlockingQueue<byte[]> linkedBlockingQueue = new LinkedBlockingQueue<>();
    ConcurrentHashMap<String, IAudioCustom> customMap;
    ExecutorService executorService;
    RecordState recordState = RecordState.NONE;

    public enum RecordState {
        RECORDING,
        NONE
    }


    public static RecordManger getInstance() {
        return ourInstance;
    }
//可得到系统当前已经实例化的该类对象，若当前系统还没有实例化过这个类的对象，则调用此类的构造函数（对象实例化）
    private RecordManger() {
        customMap = new ConcurrentHashMap<>();
        executorService = Executors.newFixedThreadPool(2);
// 线程池：开辟一块内存空间，存放很多未死亡的线程。有任务时，从池中选一个，执行完后线程对象归池
// ExecutorService 会自动提供一个线程池和相关 API
        startDistr();
    }

    private void startDistr() {
        distriTask = new DistriTask(linkedBlockingQueue, customMap);
        executorService.submit(distriTask);
// Runnable对象传递给submit方法，run()方法自动在一个线程上执行并返回Future对象
// 接收Runnable（无返回值）或Callable（有返回值）对象作为输入参数，返回一个Future对象
// submit方法提交的任务中的call方法如果返回Integer，那么submit方法就返回Future<Integer>；
// 如果call方法返回Float，那么submit方法就返回Future<Float>
    }


    public RecordState getRecordState() {
        return recordState;
    }

    public void registAudioCustom(String key, IAudioCustom value) {
        if (customMap != null) {
            if (!TextUtils.isEmpty(key) && value != null)
                customMap.put(key, value);
        }
    }

    public void unRegistAudioCustom(String key) {
        if (!TextUtils.isEmpty(key)) {
            if (customMap.containsKey(key)) {
                customMap.remove(key);
            }
        }
    }

    private static final String TAG = "RecordManger";

    public synchronized void startRecord(RecordConfig recordConfig, IRecord record) throws RecordException {
        Log.e(TAG, "startRecord: ");
        recordState = RecordState.RECORDING;
        RecordTask recordTask = new RecordTask(recordConfig, record, linkedBlockingQueue);
        recordFuture = executorService.submit(recordTask);
    }

    public synchronized void stopRecord() {
        recordState = RecordState.NONE;
        if (recordFuture != null) {
            recordFuture.cancel(true);
//cancel()方法，用于取消异步的任务，传入true会中断线程停止任务，传入false让线程正常执行至完成
        }
    }
}
//corePoolSize : 核心线程数，一旦创建将不会再释放。如果创建的线程数还没有达到指定的核心线程数量，
//将会继续创建新的核心线程，直到达到最大核心线程数后，核心线程数将不在增加；如果没有空闲的核心线程，
//同时又未达到最大线程数，则将继续创建非核心线程；如果核心线程数等于最大线程数，则当核心线程都处于激活状态时，任务将被挂起，等待空闲线程来执行。

//maximumPoolSize : 允许创建的最大线程数量。如果最大线程数等于核心线程数，则无法创建非核心线程；
//如果非核心线程处于空闲时，超过设置的空闲时间，则将被回收，释放占用的资源。

//keepAliveTime : 当线程空闲时，所允许保存的最大时间，超过这个时间，线程将被释放销毁，但只针对于非核心线程。
//unit : 时间单位，TimeUnit.SECONDS等。
//workQueue : 任务队列，存储暂时无法执行的任务，等待空闲线程来执行任务。
//handler : 当线程边界和队列容量已经达到最大时，用于处理阻塞时的程序