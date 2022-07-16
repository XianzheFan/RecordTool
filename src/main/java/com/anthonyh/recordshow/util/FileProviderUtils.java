package com.anthonyh.recordshow.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import com.anthonyh.recordshow.BuildConfig;

import java.io.File;
// URI与URL的联系是它们都能够唯一地确定一个资源，区别是URL是通过资源的地点来确定资源的
public class FileProviderUtils {
// 应用程序App可以创建的Context（Activity和Service没启动就不会创建）个数公式一般为：
// 总Context实例个数 = Service个数 + Activity个数 + 1（Application对应的Context对象）
    public static Uri getUriForFile(Context mContext, File file) {
        Uri fileUri = null;
        if (Build.VERSION.SDK_INT >= 24) {
            fileUri = getUriForFile24(mContext, file);
        } else {
            fileUri = Uri.fromFile(file);
        }
        return fileUri;
    }

    public static Uri getUriForFile24(Context mContext, File file) {
        Uri fileUri = android.support.v4.content.FileProvider.getUriForFile(mContext,
                BuildConfig.APPLICATION_ID + ".provider",
                file);
        return fileUri;
    }

    public static void setIntentDataAndType(Context mContext,
                                            Intent intent,
                                            String type,
                                            File file,
                                            boolean writeAble) {
        if (Build.VERSION.SDK_INT >= 24) {
            intent.setDataAndType(getUriForFile(mContext, file), type);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            // 给启动Activity的Intent添加flag
            if (writeAble) {
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        } else {
            intent.setDataAndType(Uri.fromFile(file), type);
        }
    }
}

