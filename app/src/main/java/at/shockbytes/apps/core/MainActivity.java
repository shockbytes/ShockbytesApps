package at.shockbytes.apps.core;

import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.apps.R;
import at.shockbytes.apps.drive.OnAppCopiedListener;
import at.shockbytes.apps.drive.OnDriveAppsAvailableListener;
import at.shockbytes.apps.drive.model.DriveShockApp;
import at.shockbytes.apps.drive.model.LocalShockApp;
import at.shockbytes.apps.drive.webapi.WebDriveManager;
import at.shockbytes.apps.fragments.MainFragment;
import at.shockbytes.apps.storage.AppsManager;
import at.shockbytes.apps.util.AppParams;
import butterknife.Bind;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks, OnDriveAppsAvailableListener,
        OnAppCopiedListener, SwipeRefreshLayout.OnRefreshListener {


    @Bind(R.id.swipeContainer)
    protected SwipeRefreshLayout refreshLayout;

    @Inject
    protected WebDriveManager driveManager;

    @Inject
    protected AppsManager appManager;

    @State
    protected boolean isInitialSync = true;

    private MainFragment mainFragment;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Snackbar.make(findViewById(R.id.main_content), "Updates available", Snackbar.LENGTH_LONG)
                    .show();
            sync();
        }
    };

    private IntentFilter intentFilter = new IntentFilter(AppParams.FCM_MESSAGE_ACTION);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setEnterTransition(new Fade(Fade.IN));
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        Icepick.restoreInstanceState(this, savedInstanceState);
        ((AppsApp) getApplication()).getAppComponent().inject(this);

        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorDark,
                R.color.colorAccent,
                R.color.colorPrimaryDark);

        mainFragment = MainFragment.newInstance();
        try {
            appManager.setup();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Cannot initialize AppManager",
                    Toast.LENGTH_LONG).show();
        }

        driveManager.setup(this);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, mainFragment)
                .commit();

        if (isInitialSync) {
            showSplashScreen();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        sync();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_main_settings) {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(this);
            startActivity(SettingsActivity.newIntent(this), options.toBundle());
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case AppParams.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    Toast.makeText(getApplicationContext(),
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.", Toast.LENGTH_LONG).show();
                } else {
                    driveManager.getInstalledApps(this);
                }
                break;
            case AppParams.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
                        settings.edit().putString(AppParams.PREF_ACCOUNT_NAME, accountName).apply();

                        driveManager.setSelectedAccount(accountName);
                        driveManager.getInstalledApps(this);
                    }
                }
                break;
            case AppParams.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    driveManager.getInstalledApps(this);
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(
                requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    @Override
    public void onDriveAppsAvailable(List<DriveShockApp> driveApps) {
        if (driveApps.size() != 0) {
            appManager.synchronizeWithDriveApps(driveApps, this);
        } else {
            mainFragment.setApps(appManager.getInstalledApps(true));
            refreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCopyFailed(final String appName, final String cause) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Cannot copy " + appName + "\n" + cause, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onLocalAppSynced(final LocalShockApp app, boolean lastCopy) {
        if (lastCopy) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    syncFinished();
                }
            });
        }

    }

    @Override
    public void inSync() {
        syncFinished();
    }

    @Override
    public void onRefresh() {
        sync();
    }

    private void showSplashScreen() {
        startActivity(SplashActivity.newIntent(this));
    }

    private void sync() {
        refreshLayout.setRefreshing(true);
        driveManager.getInstalledApps(this);
    }

    private void syncFinished() {
        refreshLayout.setRefreshing(false);
        mainFragment.setApps(appManager.getInstalledApps(true));

        if (isInitialSync) {
            isInitialSync = false;
            Intent intent = new Intent(AppParams.DATA_LOAD_FINISH_ACTION);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}