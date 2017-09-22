/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package at.shockbytes.apps.fcm;

import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import at.shockbytes.apps.drive.webapi.GoogleWebDriveManager;
import at.shockbytes.apps.drive.webapi.WebDriveManager;


public class AppsFbInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "Apps";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        sendRegistrationToServer(refreshedToken);
    }

    private void sendRegistrationToServer(String token) {
        WebDriveManager driveManager = new GoogleWebDriveManager(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));
        driveManager.setup(null);
        driveManager.synchronizeFCMToken(getApplicationContext(), token);
    }
}
