package tech.rounak.productiveyou.utils;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/

public class CheckAppLaunchThread extends Thread {

    private Context context;
    private Handler handler;
    private ActivityManager actMan;
    private int timer = 100;
    public static final String TAG = "App Thread";
    public static String lastUnlocked;
//    volatile Map<String,Long> appTimerMap;

    private Map<String,Long> appTimerMap;

    public CheckAppLaunchThread(Handler mainHandler, Context context) {
        this.context = context;
        this.handler = mainHandler;
        actMan = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        this.appTimerMap=appTimer;
        this.setPriority(MAX_PRIORITY);
        appTimerMap = new HashMap<>();
    }

    @Override
    public void run() {
//        context.startService(new Intent(context, AppLockService.class));
        Looper.prepare();
        String prevTasks;
        String recentTasks = "";

        prevTasks = recentTasks;
        Log.d("Thread", "Inside Thread");

        while (true) {
            try {
                String topPackageName = "";
                    UsageStatsManager mUsageStatsManager = StatsHelper.getUsageStatsManager();
                    long time = System.currentTimeMillis();
                Log.i(TAG, "FirstTime " + time);

                    // We get usage stats for the last 10 seconds
//                UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);

                    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*5, time);
                    if(stats != null) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                        for (UsageStats usageStats : stats) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (mySortedMap != null && !mySortedMap.isEmpty()) {

                            topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                }

                recentTasks = topPackageName;
                Thread.sleep(timer);

                Log.i(TAG, "recurring " + System.currentTimeMillis() + "  " + recentTasks + " " +  (appTimerMap.containsKey(recentTasks)) );

                if(appTimerMap.containsKey(recentTasks) && appTimerMap.get(recentTasks)<System.currentTimeMillis()){

                    //REMOVE KEY
                    //SHOW QUIT APP DIALOG
                    Toast.makeText(context, "PLEASE QUIT APP", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "PLEASE QUIT APP");
                }

                if (recentTasks.length()==0 || recentTasks.equals(prevTasks)) {
                } else {
                    if (isinPrefList(recentTasks)) {
//                        Log.d(TAG, "START TIMER" + recentTasks);
                        handler.post(new ShowStartDialog(context, recentTasks));
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            prevTasks = recentTasks;

        }

    }


    class ShowStartDialog implements Runnable {

        private Context mContext;
        private String pkgName;

        public ShowStartDialog(Context mContext, String pkgName) {
            this.mContext = mContext;
            this.pkgName = pkgName;
        }

        @Override
        public void run() {

              showTimeUpWindow(pkgName);

        }

    }

    private void showTimeUpWindow(String pkgName){
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
//                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//                PixelFormat.TRANSLUCENT);
//
//        View timeUpView = LayoutInflater.from(context).inflate(R.layout.card_timeup,null);
//        WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
//        wm.addView(timeUpView, params);

//        ADD IN APPTIMER AFTER CHECKING IT

        if(!appTimerMap.containsKey(pkgName)){

            //SHOW TIME ASKING WINDOW AND ASK FOR TIME
            Toast.makeText(context, "ADD TIME WINDOW(30 sec)", Toast.LENGTH_SHORT).show();
            appTimerMap.put(pkgName, System.currentTimeMillis() + (long)40000); // suppose 10 sec given from timer
            Log.i(TAG, "showTimeUpWindow: " +pkgName + " " +  System.currentTimeMillis() + (long)30000);
        }


    }

    public boolean isinPrefList(String currentActivity){
        String serialized = PrefHandler.INSTANCE.getPkgList(context);
        List<String> prefList = null;
        if (serialized != null) {
            prefList = new LinkedList<String>(Arrays.asList
                    (TextUtils.split(serialized, ",")));
        }

        return prefList != null && prefList.contains(currentActivity);
    }
}