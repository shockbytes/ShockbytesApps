package at.shockbytes.apps.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import at.shockbytes.apps.R;
import at.shockbytes.apps.drive.model.LocalShockApp;
import at.shockbytes.apps.storage.AppsApk;
import at.shockbytes.apps.util.ResourceManager;
import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author Martin Macheiner
 *         Date: 22.04.2017.
 */

public class AppsAdapter extends BaseAdapter<LocalShockApp> {

    private SharedPreferences prefs;

    public AppsAdapter(Context cxt, List<LocalShockApp> data) {
        super(cxt, data);
        prefs = PreferenceManager.getDefaultSharedPreferences(cxt);
    }

    @Override
    public BaseAdapter<LocalShockApp>.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_app, parent, false));
    }

    class ViewHolder extends BaseAdapter<LocalShockApp>.ViewHolder {

        @Bind(R.id.item_app_image)
        ImageView imgViewIcon;

        @Bind(R.id.item_app_txt_appname)
        TextView txtAppname;

        @Bind(R.id.item_app_txt_info)
        TextView txtInfo;

        @Bind(R.id.item_app_btn_install)
        Button btnInstall;

        private AppsApk appsApk;

        private int installedRevision;

        ViewHolder(final View itemView) {
            super(itemView);
        }

        @Override
        public void bind(LocalShockApp app) {
            content = app;
            installedRevision = prefs.getInt(content.getAppName(), -1);

            if (app.getApkPath() != null) {
                AppsApk apk = ResourceManager.extractInfosFromApk(context, app.getApkPath());
                if (apk != null) {
                    appsApk = apk;

                    txtAppname.setText(appsApk.appName);
                    imgViewIcon.setImageDrawable(appsApk.icon);

                    double mb = ResourceManager.roundDoubleWithDigits(new File(app.getApkPath()).length()/1024.0/1024.0, 2);
                    String info = context.getString(R.string.app_info, app.getRevision(), String.valueOf(mb));
                    txtInfo.setText(info);

                    int btnTextId;
                    if (installedRevision == content.getRevision()) {

                        if (appsApk.isInstalled) {
                            btnTextId = R.string.open;
                        } else {
                            btnTextId = R.string.install;
                            // Apps was uninstalled in meantime
                            prefs.edit().putInt(content.getAppName(), -1).apply();
                        }

                    } else {
                        btnTextId = appsApk.isInstalled ? R.string.update : R.string.install;
                    }
                    btnInstall.setText(btnTextId);
                }
            } else {
                deleteEntity(app);
            }
        }

        @OnClick(R.id.item_app_btn_install)
        void onClickInstall() {

            if (installedRevision == content.getRevision()) {
                ResourceManager.openApp(context, appsApk.packageName);
            } else {
                if (content.getApkPath() != null) {
                    ResourceManager.installApk(context, content.getApkPath());
                    prefs.edit().putInt(content.getAppName(), content.getRevision()).apply();
                } else {
                    Toast.makeText(context, "Cannot install app...", Toast.LENGTH_SHORT).show();
                }
            }


        }

    }


}
