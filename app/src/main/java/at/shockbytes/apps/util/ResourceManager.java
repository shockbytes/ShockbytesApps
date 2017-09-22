package at.shockbytes.apps.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import at.shockbytes.apps.drive.model.DriveShockApp;
import at.shockbytes.apps.storage.AppsApk;

/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public class ResourceManager {

    public static double roundDoubleWithDigits(double value, int digits) {

        if (digits < 0) {
            throw new IllegalArgumentException();
        }
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(digits, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
        }
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(i);
        return true;
    }

    public static void installApk(Context context, String apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(apk)), "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static int cleanupOldRevisions(Context context, final DriveShockApp app, File baseFile) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int revision = prefs.getInt(app.getAppName(), -1);

        if (/*revision > -1 &&*/ revision < app.getRevision()) {

            File[] files = baseFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File file, String filename) {
                    return filename.contains(app.getAppName());
                }
            });

            if (files != null) {
                for (File apk : files) {
                    apk.delete();
                }
            }
        }
        return revision;
    }

    public static AppsApk extractInfosFromApk(@NonNull Context context, String apkPath) {

        if (apkPath != null) {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageArchiveInfo(apkPath, 0);

            if (pi != null) {
                pi.applicationInfo.sourceDir = apkPath;
                pi.applicationInfo.publicSourceDir = apkPath;

                Drawable icon = pi.applicationInfo.loadIcon(pm);
                String appName = (String)pi.applicationInfo.loadLabel(pm);
                String packageName = pi.applicationInfo.packageName;

                return new AppsApk(appName, icon, packageName, isPackageInstalled(context, packageName));
            } else {
                Log.wtf("Apps", "Info null for " + apkPath);
            }
        }
        return null;
    }

    private static boolean isPackageInstalled(@NonNull Context context, String targetPackage){
        List<ApplicationInfo> packages;
        PackageManager pm;

        pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if(packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }


}
