package at.shockbytes.apps.drive;

import java.util.List;

import at.shockbytes.apps.drive.model.DriveShockApp;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2017.
 */

public interface OnDriveAppsAvailableListener {

    void onDriveAppsAvailable(List<DriveShockApp> apps);

}
