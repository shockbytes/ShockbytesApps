package at.shockbytes.apps.drive.webapi;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import at.shockbytes.apps.drive.OnAppCopiedListener;
import at.shockbytes.apps.drive.OnDriveAppsAvailableListener;
import at.shockbytes.apps.drive.model.DriveShockApp;
import at.shockbytes.apps.drive.model.LocalShockApp;
import at.shockbytes.apps.drive.webapi.transport.DownloadDriveTask;
import at.shockbytes.apps.drive.webapi.transport.ResolveDriveFolderIdTask;
import at.shockbytes.apps.util.AppParams;
import at.shockbytes.apps.util.ResourceManager;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static at.shockbytes.apps.util.AppParams.REQUEST_ACCOUNT_PICKER;
import static at.shockbytes.apps.util.AppParams.REQUEST_PERMISSION_GET_ACCOUNTS;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public class GoogleWebDriveManager implements WebDriveManager,
        DownloadDriveTask.OnDriveFilesAvailableListener, ResolveDriveFolderIdTask.OnResolveFolderIdListener {

    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE};

    private Context context;
    private FragmentActivity activity;
    private GoogleAccountCredential credential;

    private Drive service;

    private SharedPreferences preferences;

    private OnDriveAppsAvailableListener listener;

    @Inject
    public GoogleWebDriveManager(Context context, SharedPreferences preferences) {
        this.preferences = preferences;
        this.context = context;
    }

    @Override
    public void setup(FragmentActivity activity) {
        this.activity = activity;

        // Initialize credentials and service object.
        credential = GoogleAccountCredential.usingOAuth2(
                context, Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());

        setupDriveService();
    }

    @Override
    public void setSelectedAccount(@NonNull String accountName) {
        credential.setSelectedAccountName(accountName);
    }

    @Override
    public void getInstalledApps(@NonNull OnDriveAppsAvailableListener listener) {
        this.listener = listener;

        getResultsFromApi();
    }

    @Override
    public void copyToLocalStorage(final DriveShockApp app, final java.io.File baseFile,
                                   final OnAppCopiedListener fileListener, final boolean lastCopy) {

        setupDriveService();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    ResourceManager.cleanupOldRevisions(context, app, baseFile);

                    String apk = baseFile.getAbsolutePath() + "/" + app.getFullAppName() + ".apk";
                    OutputStream apkOutputStream = new FileOutputStream(apk);

                    service.files()
                            .get(app.getApkFileId())
                            .executeMediaAndDownloadTo(apkOutputStream);

                    apkOutputStream.close();

                    LocalShockApp localApp = new LocalShockApp(app.getFullAppName(), apk);
                    fileListener.onLocalAppSynced(localApp, lastCopy);

                } catch (IOException e) {
                    e.printStackTrace();
                    fileListener.onCopyFailed(app.getAppName(), e.getLocalizedMessage());
                }

            }
        }).start();

    }

    @Override
    public void synchronizeFCMToken(final Context context, final String token) {

        // Service not initialized yet
        if (service == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    File fileMetadata = new File();
                    fileMetadata.setName("apps_fcm_token.txt");
                    java.io.File filePath = new java.io.File(context.getFilesDir() + "/apps_fcm_token.txt");

                    if (!filePath.exists()) {
                        filePath.createNewFile();
                    }
                    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)));
                    writer.println(token);
                    FileContent mediaContent = new FileContent("text/plain", filePath);
                    writer.close();

                    String fileId = getFCMFileId();
                    // File already exists
                    if (fileId != null) {
                        fileMetadata.setModifiedTime(new DateTime(System.currentTimeMillis()));
                        service.files().update(fileId, fileMetadata, mediaContent)
                                .setFields("id, modifiedTime")
                                .execute();
                    } else {
                        service.files().create(fileMetadata, mediaContent)
                                .setFields("id")
                                .execute();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onDriveFilesAvailable(List<DriveShockApp> apps) {
        listener.onDriveAppsAvailable(apps);
    }

    @Override
    public void onDriveDownloadCancelled(Exception e) {

        if (e != null && activity != null) {
            if (e instanceof GooglePlayServicesAvailabilityIOException) {
                showGooglePlayServicesAvailabilityErrorDialog(
                        ((GooglePlayServicesAvailabilityIOException) e)
                                .getConnectionStatusCode());
            } else if (e instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) e).getIntent(),
                        AppParams.REQUEST_AUTHORIZATION);
            }
        }
    }

    // ------------------------ Private methods ------------------------

    @AfterPermissionGranted(AppParams.REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {

        if (activity == null) {
            return;
        }

        if (EasyPermissions.hasPermissions(
                activity, Manifest.permission.GET_ACCOUNTS)) {
            String accountName = preferences.getString(AppParams.PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                credential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                activity.startActivityForResult(
                        credential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    activity,
                    "This app needs to access your Google account and your external storage.",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private void setupDriveService() {

        if (service == null) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            service = new com.google.api.services.drive.Drive.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Shockbytes Apps")
                    .build();
        }
    }

    private String getFCMFileId() throws IOException {

        String q = "name contains '" + AppParams.FCM_DRIVE_FILENAME+ "' " +
                "and trashed = false " +
                "and mimeType contains 'text/plain'";

        FileList result = service.files().list()
                .setQ(q)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        if (result.getFiles() != null && result.getFiles().size() > 0) {
            return result.getFiles().get(0).getId();
        }
        return null;
    }

    private void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (credential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            Toast.makeText(context, "No network connection available.", Toast.LENGTH_LONG).show();
        } else {

            String folderId = preferences.getString(AppParams.PREF_FOLDER_ID, null);
            if (folderId != null) {
                new DownloadDriveTask(service, this, folderId).execute();
            } else {
                resolveFolderId();
            }
        }
    }

    private void resolveFolderId() {
        new ResolveDriveFolderIdTask(service, this).execute(AppParams.DRIVE_FOLDER_NAME);
    }

    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        final int connectionStatusCode =
                apiAvailability.isGooglePlayServicesAvailable(context);
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
        }
    }

    private void showGooglePlayServicesAvailabilityErrorDialog(
            final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(
                activity,
                connectionStatusCode,
                AppParams.REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    @Override
    public void onDriveFilesAvailable(String folderId) {

        if (folderId != null) {
            preferences.edit().putString(AppParams.PREF_FOLDER_ID, folderId).apply();
            new DownloadDriveTask(service, this, folderId).execute();
        } else {
            Toast.makeText(context, "Cannot resolve folder id", Toast.LENGTH_LONG).show();
        }

    }
}
