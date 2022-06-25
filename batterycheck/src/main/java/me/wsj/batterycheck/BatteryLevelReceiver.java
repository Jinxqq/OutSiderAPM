package me.wsj.batterycheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

@Deprecated
public class BatteryLevelReceiver extends BroadcastReceiver {

    private volatile float batteryPct;
    private int level;
    private int scale;
    private boolean isCharging;
    private int voltage;
    private int chargingType;

    @Override
    public void onReceive(Context context, Intent intent) {
        //当前剩余电量
        level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        //电量最大值
        scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);

        int status = intent.getIntExtra("status", 0);
        isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public int getCurrentBatteryLevel() {
        return level;
    }

    public int getTotalBatteryPercent() {
        return scale;
    }

    public int getVoltage() {
        return voltage;
    }

    public boolean isCharging() {
        return isCharging;
    }
}
