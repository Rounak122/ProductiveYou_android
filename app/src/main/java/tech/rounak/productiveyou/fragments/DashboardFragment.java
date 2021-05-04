package tech.rounak.productiveyou.fragments;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tech.rounak.productiveyou.R;
import tech.rounak.productiveyou.adapters.AppListAdapter;
import tech.rounak.productiveyou.models.AppModel;
import tech.rounak.productiveyou.utils.CheckAppLaunchThread;
import tech.rounak.productiveyou.utils.PrefHandler;
import tech.rounak.productiveyou.utils.StatsHelper;

import static android.os.Looper.getMainLooper;
import static tech.rounak.productiveyou.utils.StatsHelper.TAG;
import static tech.rounak.productiveyou.utils.StatsHelper.initStatsHelper;

public class DashboardFragment extends Fragment {

    private static final int USAGE_STATS_PERMISSION = 100;
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 200;
    private List<AppModel> appList = new ArrayList<AppModel>();
    private AppListAdapter appListAdapter;
    private RecyclerView appRecyclerList;
    private PackageManager packageManager = null;
//    private List<String> prefList = new ArrayList<String>();
    private MaterialButtonToggleGroup btn_timeFrame;
    private int timeframe =0;
    private long startTimeMillis = 0;
    private long endTimeMillis =0;
    private long totalTime=0;
    private String[] defaultList = {"com.facebook.katana", "com.instagram.android", "com.whatsapp", "com.android.chrome", "com.twitter.android"};

    Handler handler;
//    volatile Map<String,Long> appTimer = new HashMap<>();

    View timeStartDialog;
    CardView btnAllApps;
    MaterialToolbar toolbar;
    TextView tv_totalTime;
    PieChart pieChart;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_dashboard, container, false);
        btnAllApps = v.findViewById(R.id.card_all_apps);
        toolbar = v.findViewById(R.id.toolbar);
        AppBarLayout appBarLayout = v.findViewById(R.id.appBarLayout);
        ConstraintLayout appbarContents=v.findViewById(R.id.appbar_content);
        timeStartDialog = inflater.inflate(R.layout.card_timeup, null);

        NavController navController = NavHostFragment.findNavController(this);

        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupWithNavController(toolbar,navController,appBarConfiguration);
        btnAllApps.setOnClickListener(view -> navController.navigate(R.id.action_DashboardFragment_to_AllAppsFragment));

        tv_totalTime = v.findViewById(R.id.tv_totalTime);
        btn_timeFrame=v.findViewById(R.id.toggleButton);
        btn_timeFrame.check(R.id.btn_today);
        appRecyclerList = (RecyclerView) v.findViewById(R.id.topAppList);
        pieChart = (PieChart) v.findViewById(R.id.chart);


        packageManager = requireActivity().getPackageManager();

//
//        if (!Settings.canDrawOverlays(requireActivity().getApplicationContext())) {
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + requireActivity().getPackageName()));
//            startActivityForResult(intent,  CODE_DRAW_OVER_OTHER_APP_PERMISSION);
//        }

        PrefHandler.INSTANCE.setMode(0, requireActivity().getApplicationContext());

        initiateUsageStats();
        checkDrawPermission();


        return v;
    }




//    private void getForegroundApp(){
//            long time = System.currentTimeMillis();
//            List<UsageStats> appList = StatsHelper.getUsageStatsManager().queryUsageStats(UsageStatsManager.INTERVAL_DAILY,time - 1000 * 1000, time);
//            if (appList != null && appList.size() > 0) {
//                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//                for (UsageStats usageStats : appList) {
//                    mySortedMap.put(usageStats.getLastTimeUsed(),
//                            usageStats);
//                }
//                if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                    currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
//                }
//            }
//    }

    private void fillStats(){

        if (PrefHandler.INSTANCE.getIsFirstTime(requireActivity().getApplicationContext())) {
//            setDefaultSelection();
            addAllPackages();
            PrefHandler.INSTANCE.saveIsFirstTime(false, requireActivity().getApplicationContext());
        }

        setTimes(); //set timeframe
        setAppList();
//        setChart();

        new LoadApplications().execute();
        btn_timeFrame.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

            if(checkedId==R.id.btn_today){
                timeframe=0;

            }else if(checkedId==R.id.btn_yesterday){
                timeframe=1;
            }else{
                timeframe=2;
            }
            setTimes(); //set timeframe
//            setAppList(); //
            new LoadApplications().execute();
        });

    }

    public void setChart(){

        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(60f);
        pieChart.animateY(1000, Easing.EaseInOutQuad);
//        pieChart.setDrawSliceText(false);

        ArrayList<PieEntry> yValues = new ArrayList<>();

//        for (int i=0; i<5; i++){
//            AppModel app = appList.get(i);
//            yValues.add(new PieEntry(app.getUsageMillis(), app.getAppName()));
//
//        }
        for (AppModel app: appList){
            if(app.getUsageMillis()>120000){
                yValues.add(new PieEntry(app.getUsageMillis(), app.getAppName()));
            }
        }


        PieDataSet dataSet = new PieDataSet(yValues, "Apps");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.WHITE);

        pieChart.setData(data);

    }



    // Fill the stats
    private void initiateUsageStats() {
        if (hasPermission()) {
            initStatsHelper(requireActivity().getApplicationContext());
            fillStats();
        } else {
            requestPermission();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //TODO: Verify
        if (requestCode == USAGE_STATS_PERMISSION) {
            initiateUsageStats();
        }
        if (requestCode == CODE_DRAW_OVER_OTHER_APP_PERMISSION) {
            if (!Settings.canDrawOverlays(requireActivity().getApplicationContext())) {
                // You don't have permission
                checkDrawPermission();
            }
        }


    }

    public void checkDrawPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(requireActivity().getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + requireActivity().getPackageName()));
                startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            }else{
                initiateDrawService();
            }

        }
    }

    public void initiateDrawService(){

        handler = new Handler(getMainLooper());
        new CheckAppLaunchThread(handler, requireActivity().getApplicationContext()).start();
    }

    //Request for app time permission
    private void requestPermission() {
        Toast.makeText(getContext(), "Please grant permission to read app usage data", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), USAGE_STATS_PERMISSION);
    }

    //    Checks if has permission
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                requireActivity().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), requireActivity().getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void setAppList() {

//        String serialized = PrefHandler.INSTANCE.getPkgList(requireActivity().getApplicationContext());
////        Log.i("GOING", "setAppList: " + serialized);
//        if (serialized != null) {
//            List<String> prefList = new LinkedList<String>(Arrays.asList
//                    (TextUtils.split(serialized, ",")));
//        }

        appListAdapter = new AppListAdapter(getContext(), appList);
        appListAdapter.setFrag(0);
        if (appListAdapter != null) {
            Log.i("RECYCLERVIEW", "setAppList: ADAPTER SETUP------------------------------------------------>>>>>>>>>>>>>>>>>>>");
            appRecyclerList.setAdapter(appListAdapter);
            appRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
//            appRecyclerList.addItemDecoration(new ListItemDecoration(
//                    Math.round(getResources().getDisplayMetrics().density * 5)));
        }
    }

    private void setTimes() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startMillis = 0;
        long endMillis = System.currentTimeMillis();

        Date endresultdate = new Date(System.currentTimeMillis());
        switch (timeframe) {
            case StatsHelper.DAILY_STATS:
                startMillis = calendar.getTimeInMillis();
                break;
            case StatsHelper.YESTERDAY_STATS:
                calendar.set(Calendar.HOUR_OF_DAY, -24);
                startMillis = calendar.getTimeInMillis();
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 99);
                endMillis = calendar.getTimeInMillis();
                endresultdate = calendar.getTime();
                break;
//            case StatsHelper.WEEKLY_STATS:
//                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
//                startMillis = calendar.getTimeInMillis();
//                break;
            case StatsHelper.MONTHLY_STATS:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startMillis = calendar.getTimeInMillis();
                break;
            default:
                break;
        }
        startTimeMillis=startMillis;
        endTimeMillis=endMillis;

    }

    private void fetchUsageData(List<ApplicationInfo> list) {
        String serialized = PrefHandler.INSTANCE.getPkgList(requireActivity().getApplicationContext());
        List<String> prefList = null;
//        Log.i("GOING", "setAppList: " + serialized);
        if (serialized != null) {
            prefList = new LinkedList<String>(Arrays.asList
                    (TextUtils.split(serialized, ",")));
        }

        appList.clear();
//        List<AppModel> allAppList = new ArrayList<>();
        Map<String, UsageStats> usageStatsMap = StatsHelper.getUsageStatsManager().
                queryAndAggregateUsageStats(startTimeMillis, endTimeMillis);

        for (ApplicationInfo info : list) {
            try {
//                Log.i(TAG, "fetchUsageData: +++++++++++++++++++" + info.packageName + (null != packageManager.getLaunchIntentForPackage(info.packageName)) + " "+prefList.contains(info.packageName) );

                if ((null != packageManager.getLaunchIntentForPackage(info.packageName))&& ( usageStatsMap.containsKey(info.packageName))  && prefList.contains(info.packageName)) {

                    long uTime = usageStatsMap.get(info.packageName).getTotalTimeInForeground();
                    totalTime+=uTime;

                    appList.add(new AppModel((String) info.loadLabel(packageManager),
                            info.packageName,
                            info.loadIcon(packageManager),
                            StatsHelper.getTime(uTime),
                            uTime));

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Collections.sort(appList, new Comparator<AppModel>() {
            @Override
            public int compare(AppModel lhs, AppModel rhs) {
                return Long.compare(rhs.getUsageMillis(),lhs.getUsageMillis());
            }
        });

    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            fetchUsageData(
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA));

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            tv_totalTime.setText(StatsHelper.getTime(totalTime));
            appListAdapter.notifyDataSetChanged();
            setChart();
            pieChart.invalidate(); // refresh
            Toast.makeText(getContext(), "DONEEE", Toast.LENGTH_SHORT).show();
            //TODO: close progressbar when added
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
//            TODO: SHOW PROGRESSBAR
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private void addAllPackages(){
        new LoadApplicationsFirstTime().execute();
    }
//
//    private void setDefaultSelection() {
//        PackageManager pm = requireActivity().getPackageManager();
//        for (String packageName : defaultList) {
//            Intent intent = pm.getLaunchIntentForPackage(packageName);
//            if (intent != null) {
//                List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                if (list.size() > 0) {
//                    prefList.add(packageName);
//                }
//            }
//        }
//        PrefHandler.INSTANCE.savePkgList(TextUtils.join(",", prefList), getContext());
//    }

    private void checkForLaunchIntent() {
        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        List<String> prefAppsList = new ArrayList<String>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    prefAppsList.add(info.packageName);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "checkForLaunchIntent: --------------------------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>___________>>>>>-<>!!!!!!!" + TextUtils.join(",", prefAppsList));
        PrefHandler.INSTANCE.savePkgList(TextUtils.join(",", prefAppsList), getContext());
    }

    private class LoadApplicationsFirstTime extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            checkForLaunchIntent();
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            fillStats();
//            appListAdapter.notifyDataSetChanged();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }


}