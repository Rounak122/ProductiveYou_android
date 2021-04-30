package tech.rounak.productiveyou;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import tech.rounak.productiveyou.utils.PrefHandler;

import static tech.rounak.productiveyou.utils.StatsHelper.initStatsHelper;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/

public class MainActivity extends AppCompatActivity {

    private static final int USAGE_STATS_PERMISSION = 100;
    private List<String> prefList = new ArrayList<>();
    private String[] defaultList = {"com.facebook.katana", "com.instagram.android", "com.whatsapp", "com.android.chrome", "com.twitter.android"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PrefHandler.INSTANCE.setMode(0, getApplicationContext());

        prefList = new LinkedList<String>();
        if (PrefHandler.INSTANCE.getIsFirstTime(getApplicationContext())) {
            setDefaultSelection();
            PrefHandler.INSTANCE.saveIsFirstTime(false, getApplicationContext());
        }

        fillStats();

    }

    // FIll the stats
    private void fillStats() {
        if (hasPermission()) {
            initStatsHelper(getApplicationContext());
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setAppList();
        Log.d("MainActivity", "onResume: ");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); //TODO: Verify
        if (requestCode == USAGE_STATS_PERMISSION) {
            fillStats();
        }
    }

    //Request for app time permission
    private void requestPermission() {
        Toast.makeText(this, "Please grant permission to read app usage data", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), USAGE_STATS_PERMISSION);
    }

//    Checks if has permission
    private boolean hasPermission() {
        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    // checks if items present in our default list are present in phone if yes, saves in preferences
    private void setDefaultSelection() {
        PackageManager pm = getPackageManager();
        for (String packageName : defaultList) {
            Intent intent = pm.getLaunchIntentForPackage(packageName);
            if (intent != null) {
                List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (list.size() > 0) {
                    prefList.add(packageName);
                }
            }
        }
        PrefHandler.INSTANCE.savePkgList(TextUtils.join(",", prefList), this);
    }
}