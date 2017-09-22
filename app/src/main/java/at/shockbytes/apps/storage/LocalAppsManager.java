package at.shockbytes.apps.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.shockbytes.apps.drive.OnAppCopiedListener;
import at.shockbytes.apps.drive.model.DriveShockApp;
import at.shockbytes.apps.drive.model.LocalShockApp;
import at.shockbytes.apps.drive.webapi.WebDriveManager;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public class LocalAppsManager implements AppsManager {

    private Context context;
    private WebDriveManager driveManager;
    private SharedPreferences prefs;

    private List<LocalShockApp> localApps;
    private File baseFile;

    public LocalAppsManager(Context context, WebDriveManager driveManager) {
        this.context = context;
        this.driveManager = driveManager;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void setup() throws IOException {

        baseFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Shockbytes Apps/");
        if (!baseFile.exists()) {
            baseFile.mkdirs();
        }
        grabLocalApps();
    }

    @Override
    public List<LocalShockApp> getInstalledApps(boolean forceReload) {

        if (forceReload) {
            try {
                grabLocalApps();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return localApps;
    }

    @Override
    public void synchronizeWithDriveApps(List<DriveShockApp> driveShockApps,
                                         OnAppCopiedListener listener) {

        List<DriveShockApp> apps = new ArrayList<>(driveShockApps);
        // Kick the ones which are already synced
        for (DriveShockApp drive : driveShockApps) {
            for (LocalShockApp local : localApps) {
                if (drive.getAppName().equals(local.getAppName())
                        && drive.getRevision() <= local.getRevision()) {
                    apps.remove(drive);
                }
            }
        }

        if (apps.size() > 0) {

            for (int i = 0; i < apps.size(); i++) {

                DriveShockApp app = apps.get(i);
                //int revision = ResourceManager.cleanupOldRevisions(context, app, baseFile);
                //if (revision < app.getRevision()) {
                driveManager.copyToLocalStorage(app, baseFile, listener, i == apps.size()-1);
                //}
            }
        } else {
            listener.inSync();
        }
    }


    private void grabLocalApps() throws IOException {

        localApps = new ArrayList<>();
        String[] files = baseFile.list();
        if (files != null) {
            for (String apk : files) {
                String appName = apk.substring(0, apk.lastIndexOf("."));
                localApps.add(new LocalShockApp(appName, baseFile.getPath() + "/" +apk));
            }
        }

    }

}
