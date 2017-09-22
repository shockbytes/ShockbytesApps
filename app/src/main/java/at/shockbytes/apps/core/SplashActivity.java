package at.shockbytes.apps.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;

import at.shockbytes.apps.R;
import at.shockbytes.apps.util.AppParams;

public class SplashActivity extends AppCompatActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, SplashActivity.class);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (handler != null) {
                handler.removeCallbacks(timeoutTask);
            }

            supportFinishAfterTransition();
        }
    };

    private IntentFilter intentFilter = new IntentFilter(AppParams.DATA_LOAD_FINISH_ACTION);

    private Handler handler;

    private Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            supportFinishAfterTransition();
        }
    };

    private static final int DELAY = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        getWindow().setExitTransition(new Fade(Fade.OUT));

        // Remove splash screen after 10 seconds of idle mode (prevent lock)
        handler = new Handler();
        handler.postDelayed(timeoutTask, DELAY);
    }

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

}
