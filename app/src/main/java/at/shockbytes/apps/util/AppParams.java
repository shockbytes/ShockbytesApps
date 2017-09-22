package at.shockbytes.apps.util;

/**
 * @author Martin Macheiner
 *         Date: 20.04.2017.
 */

public class AppParams {

    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_PERMISSION_ALL_PERMISSIONS = 1004;

    public static final String PREF_ACCOUNT_NAME = "accountName";
    public static final String PREF_FOLDER_ID = "folder_id";

    public static final String DRIVE_FOLDER_NAME = "apps";
    public static final String FCM_MESSAGE_ACTION = "apps_fcm_message_name";
    public static final String FCM_DRIVE_FILENAME = "apps_fcm_token";
    public static final String DATA_LOAD_FINISH_ACTION = "apps_data_load_finish_action";
}
