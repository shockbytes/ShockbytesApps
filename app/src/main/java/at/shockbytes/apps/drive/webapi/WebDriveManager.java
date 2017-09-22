package at.shockbytes.apps.drive.webapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import java.io.File;

import at.shockbytes.apps.drive.OnAppCopiedListener;
import at.shockbytes.apps.drive.OnDriveAppsAvailableListener;
import at.shockbytes.apps.drive.model.DriveShockApp;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public interface WebDriveManager {

    void setup(FragmentActivity activity);

    void setSelectedAccount(@NonNull String accountName);

    void getInstalledApps(@NonNull OnDriveAppsAvailableListener listener);

    void copyToLocalStorage(DriveShockApp app, File baseFile,
                            OnAppCopiedListener listener, boolean lastCopy);

    void synchronizeFCMToken(Context context, String token);

}
