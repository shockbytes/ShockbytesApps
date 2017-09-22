package at.shockbytes.apps.core;

import android.app.Application;
import android.os.StrictMode;

import at.shockbytes.apps.dagger.AppComponent;
import at.shockbytes.apps.dagger.AppModule;
import at.shockbytes.apps.dagger.DaggerAppComponent;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2017.
 */

public class AppsApp extends Application {

    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO Replace this solution with:
        // https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }

}
