package at.shockbytes.apps.drive.model;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2017.
 */

public class LocalShockApp {

    private String appName;
    private String apkPath;

    public LocalShockApp() {
        this("", "");
    }

    public LocalShockApp(String appName, String apkPath) {
        setAppName(appName);
        setApkPath(apkPath);
    }

    public String getAppName() {
        return appName.split("_")[0];
    }

    public LocalShockApp setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getApkPath() {
        return apkPath;
    }

    public LocalShockApp setApkPath(String apkPath) {
        this.apkPath = apkPath;
        return this;
    }

    public int getRevision() {
        return Integer.parseInt(appName.split("_")[1]);
    }


    @Override
    public String toString() {
        return "App appName: " + getAppName() + "\nRevision: " + getRevision() + "\nApk path: " + apkPath;
    }

}
