package at.shockbytes.apps.storage;

import android.graphics.drawable.Drawable;

/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public class AppsApk {

    public String appName;
    public String packageName;
    public Drawable icon;
    public boolean isInstalled;

    public AppsApk(String appName, Drawable icon, String packageName, boolean isInstalled) {
        this.appName = appName;
        this.icon = icon;
        this.packageName = packageName;
        this.isInstalled = isInstalled;
    }
}
