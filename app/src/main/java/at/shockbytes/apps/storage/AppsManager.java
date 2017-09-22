package at.shockbytes.apps.storage;

import java.io.IOException;
import java.util.List;

import at.shockbytes.apps.drive.OnAppCopiedListener;
import at.shockbytes.apps.drive.model.DriveShockApp;
import at.shockbytes.apps.drive.model.LocalShockApp;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public interface AppsManager {

    void setup() throws IOException;

    List<LocalShockApp> getInstalledApps(boolean forceReload);

    void synchronizeWithDriveApps(List<DriveShockApp> driveShockApps, OnAppCopiedListener listener);

}
