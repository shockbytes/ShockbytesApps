package at.shockbytes.apps.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import javax.inject.Inject;

import at.shockbytes.apps.R;
import at.shockbytes.apps.core.AppsApp;
import at.shockbytes.apps.drive.webapi.WebDriveManager;

/**
 * @author Martin Macheiner
 *         Date: 27.10.2015.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {

    @Inject
    protected WebDriveManager driveManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AppsApp) getActivity().getApplication()).getAppComponent().inject(this);
        addPreferencesFromResource(R.xml.settings);
        findPreference(getString(R.string.prefs_sync_fcm_key)).setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.wtf("Apps", "Device token: <" + token + ">");
        driveManager.synchronizeFCMToken(getActivity(), token);
        Toast.makeText(getActivity(), "Device token synced", Toast.LENGTH_SHORT).show();
        return true;
    }
}
