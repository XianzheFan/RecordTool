package com.anthonyh.recordshow.audio.agent;
//读写音频数据以及存文件

import android.text.TextUtils;
import android.util.Log;

import com.anthonyh.recordshow.audio.IAudioCustom;
import com.anthonyh.recordshow.util.DateUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

//Java中实现多线程主要由以下两种方式:继承Thread类和实现Runnable接口
//Runnable接口的run()方法可以被多个线程共享，适用于多个进程处理一种资源的问题
//Java中真正能创建新线程的只有Thread类对象
//通过实现Runnable的方式，最终还是通过Thread类对象来创建线程
public abstract class BaseAudioCustomAgent implements IAudioCustom, Runnable {

    private final String TAG = getClass().getSimpleName();

    protected String filePath="/sdcard/recordFile";
    protected String fileName="";

// LinkedBlockingQueue是一个单向链表实现的阻塞队列。该队列按 FIFO（先进先出）排序元素，
// 新元素插入到队列的尾部，并且队列获取操作会获得位于队列头部的元素。（可指定队列容量，默认容量大小等于Integer.MAX_VALUE）
// 支持多线程并发。当多线程竞争同一个资源时，某线程获取到该资源之后，其它线程需要阻塞等待。
// LinkedBlockingQueue在实现“多线程对竞争资源的互斥访问”时，对于“插入”和“取出(删除)”操作分别使用了不同的锁。
// 对于插入操作，通过“插入锁putLock”进行同步；对于取出操作，通过“取出锁takeLock”进行同步。
// 此外，插入锁putLock和“非满条件notFull”相关联，取出锁takeLock和“非空条件notEmpty”相关联。

    LinkedBlockingQueue<byte[]> linkedBlockingQueue = new LinkedBlockingQueue<>();

    @Override
    public void addAudioArray(byte[] audio) {
        try {
            linkedBlockingQueue.put(audio);  // put()加入队列
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;  // 成员变量
    }

    @Override
    public void run() {
        if (!ensureRootPath(filePath)) {
            Log.e(TAG, "run: create path failed");
        }

        String fileRealPath=filePath+File.separator;
//      File.separator 文件分隔符'\'
        if (TextUtils.isEmpty(fileName))
        {
            fileRealPath+=DateUtil.getRandFileName();  // 文件名为当时的时间戳
        }else {
            fileRealPath+=fileName;
        }

        File fileRecord = new File(fileRealPath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(fileRecord);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }  // 文件输出流

        while (!Thread.currentThread().isInterrupted()) {  // 线程未阻塞
            try {
                byte[] audio = linkedBlockingQueue.take();
//  put()方法向队列中生产数据，当队列满时，线程阻塞
//  take()方法从队列中消费数据，当队列为空时，线程阻塞
//  interrupted()会清除线程的中断状态，在中断状态下，多次调用，第一次会返回true表示线程是中断状态，
//  随后会清除线程的中断状态。以后多次调用都会返回false，除非在调用之前线程再次中断。
//  isInterrupted()，会返回线程的当前状态，不会清理中断线程的中断状态，所以多次调用都会返回同一结果。

                if (fileOutputStream != null) {
                    fileOutputStream.write(audio);
                }  // 向文件输出流里写入音频数据
                decodeAudio(audio);
//                DifAgent.doWork(audio);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.flush();  // 清空内存
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void decodeAudio(byte[] audio);
//  boolean（bool数据类型） 的默认值是 false，Boolean （一个类）的默认值是 null
    private boolean ensureRootPath(String audioRootPath) {
        File file = new File(audioRootPath);
        if (!file.exists()) {
            boolean ret = file.mkdirs();
// mkdir方法是用于创建最后一个/后面的文件夹，最后一个/前面的文件夹必须都存在。这是才会创建成功。
// 否则会创建失败。mkdirs方法是无论父文件夹是否存在都会创建。
            return ret;
        }
        return true;
    }
}
