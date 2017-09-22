package at.shockbytes.apps.dagger;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import at.shockbytes.apps.drive.webapi.GoogleWebDriveManager;
import at.shockbytes.apps.drive.webapi.WebDriveManager;
import at.shockbytes.apps.storage.AppsManager;
import at.shockbytes.apps.storage.LocalAppsManager;
import dagger.Module;
import dagger.Provides;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2017.
 */

@Module
public class AppModule {

    private Application app;

    public AppModule(Application app) {
        this.app = app;
    }

    @Provides
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(app);
    }

    @Provides
    @Singleton
    public WebDriveManager provideWebDriveManager(SharedPreferences preferences) {
        return new GoogleWebDriveManager(app.getApplicationContext(), preferences);
    }

    @Provides
    @Singleton
    public AppsManager provideAppsManager(WebDriveManager driveManager) {
        return new LocalAppsManager(app.getApplicationContext(), driveManager);
    }

}