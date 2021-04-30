package tech.rounak.models;

import android.graphics.drawable.Drawable;

/**
 * Created by Rounak
 * For more info visit https://rounak.tech
 **/

public class AppModel {
    private String appName;
    private String appPkgName;
    private Drawable appIcon;
    private String usageTime ;

    public AppModel(String name, String pkgName, Drawable icon, String usageTime) {
        this.appName = name;
        this.appPkgName = pkgName;
        this.appIcon = icon;
        this.usageTime = usageTime;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getAppPkgName() {
        return appPkgName;
    }

    public void setAppPkgName(String appPkgName) {
        this.appPkgName = appPkgName;
    }

    public String getUsageTime() {
        return usageTime;
    }

    public void setUsageTime(String usageTime) {
        this.usageTime = usageTime;
    }
}
