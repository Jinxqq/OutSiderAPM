package me.wsj.apm.traffic.okhttp;

import android.text.TextUtils;
import android.util.Log;

/**
 * 具体实现逻辑，位于mobile module中
 *
 * @author OutSiderAPM
 */
public class DataRecordUtils {

    /**
     * recordUrlRequest
     *
     * @param okHttpData
     */
    public static void recordUrlRequest(OkHttpData okHttpData) {
        if (okHttpData == null || TextUtils.isEmpty(okHttpData.url)) {
            return;
        }

        Log.e("DataRecordUtils", okHttpData.toString());
//        QOKHttp.recordUrlRequest(okHttpData.url, okHttpData.code, okHttpData.requestSize,
//                okHttpData.responseSize, okHttpData.startTime, okHttpData.costTime);
//
//        if (DEBUG) {
//            Log.d(TAG, "存储okkHttp请求数据，结束。");
//        }
    }
}