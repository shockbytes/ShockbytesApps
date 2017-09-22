package at.shockbytes.apps.dagger;

import javax.inject.Singleton;

import at.shockbytes.apps.core.MainActivity;
import at.shockbytes.apps.fragments.MainFragment;
import at.shockbytes.apps.fragments.SettingsFragment;
import dagger.Component;

/**
 * @author Martin Macheiner
 *         Date: 17.04.2017.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(MainActivity activity);

    void inject(MainFragment fragment);

    void inject(SettingsFragment fragment);

}