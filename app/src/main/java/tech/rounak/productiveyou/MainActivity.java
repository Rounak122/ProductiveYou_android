package tech.rounak.productiveyou;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/

public class MainActivity extends AppCompatActivity {

//    private static final int USAGE_STATS_PERMISSION = 100;
//    private List<String> prefList = new ArrayList<>();
//    private String[] defaultList = {"com.facebook.katana", "com.instagram.android", "com.whatsapp", "com.android.chrome", "com.twitter.android"};
//    PackageManager packageManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        packageManager =  getPackageManager();

//        PrefHandler.INSTANCE.setMode(0, getApplicationContext());
//
//        prefList = new LinkedList<String>();
//        if (PrefHandler.INSTANCE.getIsFirstTime(getApplicationContext())) {
////            setDefaultSelection();
//            addAllPackages();
//            PrefHandler.INSTANCE.saveIsFirstTime(false, getApplicationContext());
//        }

//        fillStats();

    }

//    // FIll the stats
//    private void fillStats() {
//        if (hasPermission()) {
//            initStatsHelper(getApplicationContext());
//        } else {
//            requestPermission();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data); //TODO: Verify
//        if (requestCode == USAGE_STATS_PERMISSION) {
//            fillStats();
//        }
//    }
//
//    //Request for app time permission
//    private void requestPermission() {
//        Toast.makeText(this, "Please grant permission to read app usage data", Toast.LENGTH_SHORT).show();
//        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), USAGE_STATS_PERMISSION);
//    }
//
////    Checks if has permission
//    private boolean hasPermission() {
//        AppOpsManager appOps = (AppOpsManager)
//                getSystemService(Context.APP_OPS_SERVICE);
//        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
//                android.os.Process.myUid(), getPackageName());
//        return mode == AppOpsManager.MODE_ALLOWED;
//    }


    // checks if items present in our default list are present in phone if yes, saves in preferences
//    private void setDefaultSelection() {
//        PackageManager pm = getPackageManager();
//        for (String packageName : defaultList) {
//            Intent intent = pm.getLaunchIntentForPackage(packageName);
//            if (intent != null) {
//                List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
//                if (list.size() > 0) {
//                    prefList.add(packageName);
//                }
//            }
//        }
//        PrefHandler.INSTANCE.savePkgList(TextUtils.join(",", prefList), this);
//    }

//
//    private void addAllPackages(){
//        new LoadApplications().execute();
//    }
//
//
//
//    private void checkForLaunchIntent() {
//        List<ApplicationInfo> list = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
//        for (ApplicationInfo info : list) {
//            try {
//                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
//                    prefList.add(info.packageName);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//    private class LoadApplications extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected Void doInBackground(Void... params) {
//            checkForLaunchIntent();
//            return null;
//        }
//
//        @Override
//        protected void onCancelled() {
//            super.onCancelled();
//        }
//
//        @Override
//        protected void onPostExecute(Void result) {
//            super.onPostExecute(result);
//        }
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//        }
//    }

}