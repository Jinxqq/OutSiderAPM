package me.wsj.apm.jank;

import android.content.ContentValues;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

import me.wsj.apm.OutSiderKt;
import me.wsj.core.storage.BaseInfo;
import me.wsj.core.utils.ProcessUtils;

/**
 * 卡顿信息
 *
 * @author OutSiderAPM
 */
public class BlockInfo extends BaseInfo {
    private final String SUB_TAG = "BlockInfo";


    public String processName;//进程名称
    public String blockStack;
    public int blockTime;

    public static class DBKey {
        public static final String PROCESS_NAME = "pn"; //进程名称
        public static final String BLOCK_STACK = "stack";
        public static final String BLOCK_TIME = "bt";
    }

    public BlockInfo() {
        processName = ProcessUtils.getCurrentProcessName();
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject ori = super.toJson()
                .put(DBKey.PROCESS_NAME, processName)
                .put(DBKey.BLOCK_STACK, blockStack)
                .put(DBKey.BLOCK_TIME, blockTime);
        return ori;
    }

    @Override
    public void parserJsonStr(String json) throws JSONException {
        parserJson(new JSONObject(json));
    }

    @Override
    public void parserJson(JSONObject json) throws JSONException {
        this.processName = json.getString(DBKey.PROCESS_NAME);
        this.blockStack = json.getString(DBKey.BLOCK_STACK);
        this.blockTime = json.getInt(DBKey.BLOCK_TIME);
    }

    @Override
    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        try {
            values.put(DBKey.PROCESS_NAME, processName);
            values.put(DBKey.BLOCK_STACK, blockStack);
            values.put(DBKey.BLOCK_TIME, blockTime);
        } catch (Exception e) {
            Log.e(OutSiderKt.TAG, e.toString());
        }
        return values;
    }
}
