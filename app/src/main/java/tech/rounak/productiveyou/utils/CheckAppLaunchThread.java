package tech.rounak.productiveyou.utils;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import tech.rounak.productiveyou.R;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/

public class CheckAppLaunchThread extends Thread {

    private Context context;
    private Handler handler;
    private ActivityManager actMan;
    private int timer = 1000;
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

                    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000*1000, time);
                    if(stats != null) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
                        for (UsageStats usageStats : stats) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//                            Log.i(TAG, "stats not Empty");
                        }

                        Log.i(TAG, "empty" + mySortedMap.isEmpty());

                        if (mySortedMap != null && !mySortedMap.isEmpty()) {

                            Log.i(TAG, "last =  " + mySortedMap.get(mySortedMap.lastKey()).getPackageName());
                            topPackageName = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                }

                recentTasks = topPackageName;
                Thread.sleep(timer);

//                Log.i(TAG, "top =  " + topPackageName);

                Log.i(TAG, "recurring " + System.currentTimeMillis() + "  " + recentTasks + " " +  (appTimerMap.containsKey(recentTasks)) );

                if(appTimerMap.containsKey(recentTasks) && appTimerMap.get(recentTasks)<System.currentTimeMillis()){

                    Log.i(TAG, "PLEASE QUIT APP");
                    handler.post(new ShowEndDialog(context,recentTasks));
                    appTimerMap.remove(recentTasks);
                }

                if (recentTasks.length()==0 || recentTasks.equals(prevTasks) ) {
                } else {
                    if (isinPrefList(recentTasks)) {
                        Log.i(TAG, "pref: "  + recentTasks);
//                        Log.d(TAG, "START TIMER" + recentTasks);
                        handler.post(new ShowStartDialog(context, recentTasks));
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            prevTasks = recentTasks;



//            Log.i(TAG, "rect = " + recentTasks +" prev = " + prevTasks);


        }

    }


    class ShowEndDialog implements Runnable {

        private Context mContext;
        private String pkgName;

        public ShowEndDialog(Context mContext, String pkgName) {
            this.mContext = mContext;
            this.pkgName = pkgName;
        }


        @Override
        public void run() {

            if (!appTimerMap.containsKey(pkgName)) {

                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                        PixelFormat.TRANSLUCENT);

                View timeUpView = LayoutInflater.from(context).inflate(R.layout.card_timeup, null);
                WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
                wm.addView(timeUpView, params);

                Button buttonQuit = timeUpView.findViewById(R.id.btn_quit);
                buttonQuit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        wm.removeView(timeUpView);
                    }
                });


//                Toast.makeText(context, "TIMEUP QUITTT", Toast.LENGTH_SHORT).show();

            }
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

              showTimeStartWindow(pkgName);

        }

    }

    private void showTimeStartWindow(String pkgName){


        if (!appTimerMap.containsKey(pkgName)) {


            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);

            View timeStartView = LayoutInflater.from(context).inflate(R.layout.card_timestart, null);
            WindowManager wm = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            wm.addView(timeStartView, params);

            Button buttonQuit = timeStartView.findViewById(R.id.btn_start);
            Button buttonMinus = timeStartView.findViewById(R.id.btn_minus);
            Button buttonPlus = timeStartView.findViewById(R.id.button_plus);
            TextView tvTimer = timeStartView.findViewById(R.id.tv_timer);

            buttonQuit.setOnClickListener(view -> {
                int timeV = Integer.parseInt(tvTimer.getText().toString());
                long timeInMillis = timeV*1000*60;
                long endTime = System.currentTimeMillis() + timeInMillis;
                Log.d(TAG, "showTimeStartWindow: " + System.currentTimeMillis() + timeInMillis + "  -  " + timeInMillis + " e-> " + endTime);

                appTimerMap.put(pkgName, endTime);
                wm.removeView(timeStartView);
            });
//1620134283184- 1620134283342

            buttonMinus.setOnClickListener(view -> {
                int timeV = Integer.parseInt(tvTimer.getText().toString());
                if(timeV>1){
                    handler.post(() -> {
                        String newTime = String.valueOf(timeV-1);
                        tvTimer.setText(newTime);
                    });
                }
            });

            buttonPlus.setOnClickListener(view -> {
                int timeV = Integer.parseInt(tvTimer.getText().toString());

                handler.post(() -> {
                    String newTime = String.valueOf(timeV+1);
                    tvTimer.setText(newTime);
                });
            });

//            Toast.makeText(context, "TIMEUP QUITTT", Toast.LENGTH_SHORT).show();

        }

            //SHOW TIME ASKING WINDOW AND ASK FOR TIME
//            Toast.makeText(context, "ADD TIME WINDOW(30 sec)", Toast.LENGTH_SHORT).show();
//            Log.i(TAG, "showTimeUpWindow: " +pkgName + " " +  System.currentTimeMillis() + (long)20000);



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