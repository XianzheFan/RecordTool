package com.anthonyh.recordshow.ui;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.anthonyh.recordshow.MainActivity;
import com.anthonyh.recordshow.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ShowActivity extends Activity {

    private static final String TAG = "ShowActivity";
    ListView listView;
// 程序启动开启主线程（UI线程），监听用户点击并响应，分发事件（不要在主线程执行耗时操作，而是放在子线程中）
    List<String> dataList = new ArrayList<>();
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (dataList.size() == 0) {
                Toast.makeText(getApplicationContext(), "没有文件", Toast.LENGTH_SHORT).show();
                // 第一个参数是当前的上下文环境
                return;  // Toast是简单的消息提示框，不能被点击，会根据设置的时间自动消失
            }
            if (!ShowActivity.this.isDestroyed()) {
                adapter.notifyDataSetChanged();
            }
        }
    };
    Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);
        listView = findViewById(R.id.listView);
        adapter = new Adapter(dataList);
        listView.setAdapter(adapter);
        new Thread(new GetFile()).start();
    }
// handler本身不仅可以发送消息，还可以用post的方式添加一个实现Runnable接口的匿名对象到消息队列中
// 在目标收到消息后就可以回调的方式在自己的线程中执行run的方法体
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    class GetFile implements Runnable {
        @Override
        public void run() {
            File file = new File(MainActivity.AudioPath);
            if (!file.exists()) {
                return;
            }
            File[] fileArray = file.listFiles();
            for (File fileContent : fileArray) {
                dataList.add(fileContent.getName());
                Log.e(TAG, "run: " + fileContent.getName());
            }
            handler.sendEmptyMessage(0);
        }
    }


    class Adapter extends BaseAdapter {  // 画格子的类

        List<String> list;

        public Adapter(List<String> list) {
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }  // 格子的数量

        @Override  // 根据索引获得该位置的对象
        public Object getItem(int position) {
            return position;
        }

        @Override  // 获取条目的ID
        public long getItemId(int position) {
            return position;
        }

        @Override  // 获取条目要显示的界面
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = View.inflate(getApplicationContext(), R.layout.item, null);
            TextView textView = view.findViewById(R.id.itemTv);
            textView.setText(list.get(position));  // 获取position位置的元素
//          setText()每次覆盖之前显示的内容，append()接着上次显示的内容继续显示
            return view;
        }
    }

}
