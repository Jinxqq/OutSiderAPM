package me.wsj.traffic.okhttp;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;


/**
 * 网络拦截器
 *
 * @author OutSiderAPM
 */
public class NetWorkInterceptor implements Interceptor {

    private static final Boolean DEBUG = true;
    private static final String TAG = "NetWorkInterceptor";

    private OkHttpData mOkHttpData;

    public NetWorkInterceptor() {
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        long startNs = System.currentTimeMillis();

        mOkHttpData = new OkHttpData();
        mOkHttpData.startTime = startNs;

        if (DEBUG) {
            Log.d(TAG, "okhttp request 开始时间：" + mOkHttpData.startTime);
        }

        Request request = chain.request();

        recordRequest(request);

        Response response;

        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            if (DEBUG) {
                e.printStackTrace();
                Log.e(TAG, "HTTP FAILED: " + e);
            }
            throw e;
        }

        mOkHttpData.costTime = System.currentTimeMillis() - startNs;

        if (DEBUG) {
            Log.d(TAG, "okhttp chain.proceed 耗时：" + mOkHttpData.costTime);
        }

        recordResponse(response);

        if (DEBUG) {
            Log.d(TAG, "okhttp chain.proceed end.");
        }

        DataRecordUtils.recordUrlRequest(mOkHttpData);
        return response;
    }

    /**
     * request
     */
    private void recordRequest(Request request) {
        if (request == null || request.url() == null || TextUtils.isEmpty(request.url().toString())) {
            return;
        }

        mOkHttpData.url = request.url().toString();

        RequestBody requestBody = request.body();
        if (requestBody == null) {
            mOkHttpData.requestSize = request.url().toString().getBytes().length;
            if (DEBUG) {
                Log.d(TAG, "okhttp request 上行数据，大小：" + mOkHttpData.requestSize);
            }
            return;
        }

        long contentLength = 0;
        try {
            contentLength = requestBody.contentLength();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (contentLength > 0) {
            mOkHttpData.requestSize = contentLength;
        } else {
            mOkHttpData.requestSize = request.url().toString().getBytes().length;
        }
    }

    /**
     * 设置 code responseSize
     */
    private void recordResponse(Response response) {
        if (response == null) {
            return;
        }

        mOkHttpData.code = response.code();

        if (DEBUG) {
            Log.d(TAG, "okhttp chain.proceed 状态码：" + mOkHttpData.code);
        }

        if (!response.isSuccessful()) {
            return;
        }

        ResponseBody responseBody = response.body();
        if (responseBody == null) {
            return;
        }

        long contentLength = responseBody.contentLength();

        if (contentLength > 0) {
            if (DEBUG) {
                Log.d(TAG, "直接通过responseBody取到contentLength:" + contentLength);
            }
        } else {
            BufferedSource source = responseBody.source();
            if (source != null) {
                try {
                    source.request(Long.MAX_VALUE);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Buffer buffer = source.getBuffer();
                contentLength = buffer.size();

//                if (DEBUG) {
//                    Log.d(TAG, "通过responseBody.source()才取到contentLength:" + contentLength);
//                }
            }
        }

        mOkHttpData.responseSize = contentLength;

        if (DEBUG) {
            Log.d(TAG, "okhttp 接收字节数：" + mOkHttpData.responseSize);
        }
    }
}