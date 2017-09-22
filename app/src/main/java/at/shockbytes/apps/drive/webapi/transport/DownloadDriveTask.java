package at.shockbytes.apps.drive.webapi.transport;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import at.shockbytes.apps.drive.model.DriveShockApp;

public class DownloadDriveTask extends AsyncTask<Void, Void, List<DriveShockApp>> {

    public interface OnDriveFilesAvailableListener {

        void onDriveFilesAvailable(List<DriveShockApp> files);

        void onDriveDownloadCancelled(Exception e);
    }

    private Exception lastException;
    private Drive service;
    private OnDriveFilesAvailableListener listener;

    private String folderId;

    public DownloadDriveTask(@NonNull Drive service,
                             @NonNull OnDriveFilesAvailableListener listener,
                             @NonNull String folderId) {
        this.service = service;
        this.folderId = folderId;
        this.listener = listener;
    }

    @Override
    protected List<DriveShockApp> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            lastException = e;
            cancel(true);
            return null;
        }
    }

    private List<DriveShockApp> getDataFromApi() throws IOException {

        String q = "'" + folderId+ "' in parents " +
                "and trashed = false " +
                "and mimeType contains 'application/vnd.android.package-archive'";

        FileList result = service.files().list()
                .setQ(q)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        return createDriveAppsFromFileFields(extractFileFields(result.getFiles()));
    }

    @Override
    protected void onPostExecute(List<DriveShockApp> output) {
        listener.onDriveFilesAvailable(output);
    }

    @Override
    protected void onCancelled() {
        listener.onDriveDownloadCancelled(lastException);
    }

    private List<String[]> extractFileFields(List<File> files) {
        List<String[]> fileInfo = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                fileInfo.add(new String[] {file.getId(), file.getName()});
            }
        }
        return fileInfo;
    }

    private List<DriveShockApp> createDriveAppsFromFileFields(List<String[]> fileInfo) {

        List<DriveShockApp> driveApps = new ArrayList<>();

        for (String[] apkInfo : fileInfo) {
            String apkFileId = apkInfo[0];
            String appName = apkInfo[1].substring(0, apkInfo[1].lastIndexOf("."));
            driveApps.add(new DriveShockApp(appName, apkFileId));
        }

        return driveApps;
    }

}