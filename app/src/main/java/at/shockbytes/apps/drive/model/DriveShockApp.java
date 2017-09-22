package at.shockbytes.apps.drive.model;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public class DriveShockApp {

    private String appName;
    private String apkFileId;

    public DriveShockApp(String filename, String apkFileId) {
        this.appName = filename;
        this.apkFileId = apkFileId;
    }

    public String getFullAppName() {
        return appName;
    }

    public String getAppName() {
        return appName.split("_")[0];
    }

    public String getApkFileId() {
        return apkFileId;
    }

    public int getRevision() {
        return Integer.parseInt(appName.split("_")[1]);
    }

    @Override
    public String toString() {
        return "App name: " + getAppName() + "\nRevision: " + getRevision() + "\nApk file id: " +
                apkFileId;
    }
}
