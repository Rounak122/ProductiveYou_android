package tech.rounak.productiveyou.fragments;

import android.app.usage.UsageStats;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import tech.rounak.productiveyou.models.AppModel;
import tech.rounak.productiveyou.R;
import tech.rounak.productiveyou.adapters.AppListAdapter;
import tech.rounak.productiveyou.utils.PrefHandler;
import tech.rounak.productiveyou.utils.StatsHelper;

public class AllAppsFragment extends Fragment {

    private static final String TAG = "ALLAPPSFRAGMENT";
    private List<AppModel> appList = new ArrayList<AppModel>();
    private AppListAdapter appListAdapter;
    private RecyclerView appRecyclerList;
    private PackageManager packageManager = null;
    private List<String> prefList = new ArrayList<String>();
    private MaterialButtonToggleGroup btn_timeFrame;
    private int timeframe =0;
    private long startTimeMillis = 0;
    private long endTimeMillis =0;
    long totalTime=0;

    public AllAppsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_all_apps, container, false);
        btn_timeFrame=v.findViewById(R.id.toggleButton);
        btn_timeFrame.check(R.id.btn_today);

        appRecyclerList = (RecyclerView) v.findViewById(R.id.app_recyclerview);
        setTimes(); //set timeframe
        setAppList();

        packageManager = requireActivity().getPackageManager();
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



            appRecyclerList.post(new Runnable(){
                @Override
                public void run() {
                    fetchUsageData(
                            packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
//                    tv_totalTime.setText(StatsHelper.getTime(totalTime));
                    appListAdapter.notifyDataSetChanged();
                }
            });







//            new LoadApplications().execute();
        });



        return v;
    }


    private void setAppList() {
        String serialized = PrefHandler.INSTANCE.getPkgList(requireActivity().getApplicationContext());
//        Log.i("GOING", "setAppList: " + serialized);
        if (serialized != null) {
            prefList = new LinkedList<String>(Arrays.asList
                    (TextUtils.split(serialized, ",")));
        }

        appListAdapter = new AppListAdapter(getContext(), appList);
        appListAdapter.setFrag(1);
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

        appList.clear();
        Map<String, UsageStats> usageStatsMap = StatsHelper.getUsageStatsManager().
                queryAndAggregateUsageStats(startTimeMillis, endTimeMillis);


        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName) && ( usageStatsMap.containsKey(info.packageName))  && prefList.contains(info.packageName)) {

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
//                Log.i(TAG, "fetchUsageData:-0)))))))))))))))))))) " + info.packageName);
            }
        }

    }



    private class LoadApplications extends AsyncTask<Void, Void, Void> {
//        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            fetchUsageData(
                    packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            Collections.sort(appList, new Comparator<AppModel>() {
                @Override
                public int compare(AppModel lhs, AppModel rhs) {
                    return (lhs.getAppName().compareTo(rhs.getAppName()));
                }
            });
            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            appListAdapter.notifyDataSetChanged();
//TODO: CHange total time

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

            appListAdapter.notifyDataSetChanged();
            super.onProgressUpdate(values);
        }
    }

}


