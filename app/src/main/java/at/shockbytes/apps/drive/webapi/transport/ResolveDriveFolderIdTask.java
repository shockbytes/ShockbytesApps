package at.shockbytes.apps.drive.webapi.transport;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.IOException;
import java.util.List;

public class ResolveDriveFolderIdTask extends AsyncTask<String, Void, String> {

    public interface OnResolveFolderIdListener {

        void onDriveFilesAvailable(String folderId);
    }

    private Drive service;
    private OnResolveFolderIdListener listener;

    public ResolveDriveFolderIdTask(@NonNull Drive service,
                                    @NonNull OnResolveFolderIdListener listener) {

        this.service = service;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            return getDataFromApi(params[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getDataFromApi(String folder) throws IOException {

        String q = "trashed = false" +
                "and mimeType contains 'application/vnd.google-apps.folder'";

        FileList result = service.files().list()
                .setQ(q)
                .setFields("nextPageToken, files(id, name)")
                .execute();

        List<File> files = result.getFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(folder)) {
                    return file.getId();
                }
            }
        }
        return null;
    }


    @Override
    protected void onPostExecute(String folderId) {
        listener.onDriveFilesAvailable(folderId);
    }

    @Override
    protected void onCancelled() {
        listener.onDriveFilesAvailable(null);
    }
}