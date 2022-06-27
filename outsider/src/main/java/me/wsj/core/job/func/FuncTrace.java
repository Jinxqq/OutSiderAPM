
package me.wsj.core.job.func;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import me.wsj.apm.OutSiderKt;

/**
 * @author OutSiderAPM
 */
public class FuncTrace {
    public static final String SUB_TAG = "tracefunc";

    public synchronized static void dispatch(long startTime, String kind, String sign, Context context, Intent intent,
                                             Object target, Object thiz, String location,
                                             String staticPartStr, String methodName, Object result) {
        Object[] args = new Object[2];
        args[0] = context;
        args[1] = intent;
        dispatch(startTime, kind, sign, args, target, thiz, location, staticPartStr, methodName, result);
    }

    // 统计run()，onReceive的耗时   FuncMethodAdapter.kt
    public synchronized static void dispatch(long startTime, String kind, String sign, Object[] args,
                                             Object target, Object thiz, String location,
                                             String staticPartStr, String methodName, Object result) {
        long cost = System.currentTimeMillis() - startTime;

        Log.d(OutSiderKt.TAG, String.format(
                "info [cost:%sms, kind:%s, sign:%s, target:%s, this: %s, location:%s, StaticPart:%s]",
                cost,
                kind,
                sign,
                target,
                thiz,
                location,
                staticPartStr
        ));

        /*if (!Manager.getInstance().getTaskManager().taskIsCanWork(ApmTask.TASK_FUNC)) {
            if (DEBUG) {
                LogX.d(TAG, SUB_TAG, "func task is not work");
            }
            return;
        }


        if (TextUtils.isEmpty(kind) ||
                TextUtils.isEmpty(sign) ||
                TextUtils.isEmpty(methodName) ||
                TextUtils.isEmpty(location)
        ) {
            if (DEBUG) {
                LogX.d(TAG, SUB_TAG, "params is empty");
            }
            return;
        }

        long cost = System.currentTimeMillis() - startTime;
        if (DEBUG) {
            if (!TextUtils.isEmpty(methodName) && !methodName.contains("read") && !methodName.contains("write")) {
                LogX.d(TAG, SUB_TAG, String.format(
                        "info [cost:%sms, kind:%s, sign:%s, target:%s, this: %s, location:%s, StaticPart:%s]",
                        cost,
                        kind,
                        sign,
                        target,
                        thiz,
                        location,
                        staticPartStr
                ));
                if (args != null && args.length > 0) {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("[");
                    for (Object arg : args) {
                        stringBuilder.append(arg).append(",");
                    }
                    stringBuilder.append("]");
                    LogX.d(TAG, SUB_TAG, "invoke args :" + stringBuilder);
                } else {
                    LogX.d(TAG, SUB_TAG, "invoke args : null");
                }
            }
        }

        if (TextUtils.equals(kind, AspectjUtils.JOINPOINT_KIND_EXECUTION_METHOD) ||
                TextUtils.equals(kind, AspectjUtils.JOINPOINT_KIND_CALL_METHOD)) {
            int type = FuncInfo.FUNC_TYPE_UNKNOWN;
            long minTime = Long.MAX_VALUE;
            if (methodName.equals("run")) {
                type = FuncInfo.FUNC_TYPE_RUN;
                minTime = ArgusApmConfigManager.getInstance().getArgusApmConfigData().funcControl.threadMinTime;
            } else if (methodName.equals("onReceive")) {
                type = FuncInfo.FUNC_TYPE_ONRECEIVE;
                minTime = ArgusApmConfigManager.getInstance().getArgusApmConfigData().funcControl.onreceiveMinTime;
            } else {

            }

            if (type == FuncInfo.FUNC_TYPE_UNKNOWN) {
                if (DEBUG) {
                    LogX.d(TAG, SUB_TAG, "unknown func type");
                }
                return;
            }
            if (cost < minTime) {
                if (DEBUG) {
                    LogX.d(TAG, SUB_TAG, String.format("[min:%s, real:%s, ignore]", minTime, cost));
                }
                return;
            }

            FuncInfo info = new FuncInfo();
            info.setType(type);

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(FuncInfo.KEY_PROCESS_NAME, ProcessUtils.getCurrentProcessName());
                jsonObject.put(FuncInfo.KEY_THREAD_NAME, Thread.currentThread().getName());
                jsonObject.put(FuncInfo.KEY_THREAD_ID, Thread.currentThread().getId());
                jsonObject.put(FuncInfo.KEY_COST, cost);
                jsonObject.put(FuncInfo.KEY_LOCATION, location);
                jsonObject.put(FuncInfo.KEY_STACK_NAME, CommonUtils.getStack());

                if (type == FuncInfo.FUNC_TYPE_ONRECEIVE) {
                    if (args != null && args.length >= 2) {
                        try {
                            Intent intent = (Intent) args[1];
                            String action = intent.getAction();
                            if (!TextUtils.isEmpty(action)) {
                                jsonObject.put("action", action);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            info.setParams(jsonObject.toString());
            ITask task = Manager.getInstance().getTaskManager().getTask(ApmTask.TASK_FUNC);
            if (task != null) {
                task.save(info);
            }
        }*/
    }
}
