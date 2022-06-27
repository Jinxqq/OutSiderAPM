package me.wsj.apm.traffic.okhttp;

/**
 * OKHTTP数据采集字段
 *
 * @author OutSiderAPM
 */
public class OkHttpData {

    public String url;
    public long requestSize;
    public long responseSize;

    public long startTime;
    public long costTime;

    public int code;

    @Override
    public String toString() {
        return "OkHttpData{" +
                "url='" + url + '\'' +
                ", requestSize=" + requestSize +
                ", responseSize=" + responseSize +
                ", startTime=" + startTime +
                ", costTime=" + costTime +
                ", code=" + code +
                '}';
    }
}