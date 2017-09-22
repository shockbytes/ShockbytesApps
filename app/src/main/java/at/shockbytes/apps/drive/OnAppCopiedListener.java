package at.shockbytes.apps.drive;

import at.shockbytes.apps.drive.model.LocalShockApp;

/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public interface OnAppCopiedListener {

    void onCopyFailed(String appName, String cause);

    void onLocalAppSynced(LocalShockApp app, boolean lastCopy);

    void inSync();
}
