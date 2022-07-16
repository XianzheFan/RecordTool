package com.anthonyh.recordshow.util;


public class DateUtil {
    public static String getRandFileName() {
        return System.currentTimeMillis() + "";
    }
// 获得自1970-1-01 00:00:00.000 到当前时刻的时间距离,类型为long
// byte数组转short数组
    public static short[] byteArray2ShortArray(byte[] data, int items) {
        short[] retVal = new short[items / 2];
        try {
            for (int e = 0; e < retVal.length; ++e) {
                retVal[e] = (short) (data[e * 2] & 255 | (data[e * 2 + 1] & 255) << 8);
            }
// "|"：如果相对应位都是0，则结果为0，否则为1
// "&"：如果相对应位都是1，则结果为1，否则为0（对于二进制而言）
            return retVal;
        } catch (Exception var4) {
            return retVal;
        }
    }

}
