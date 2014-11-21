/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class GameActivity extends Activity {

    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = GameActivity.class.getSimpleName();
    // Ad unit id for ADMOB to know who is requesting ads
    private static final String AD_UNIT_ID = "ca-app-pub-5750653837551984/9381471952";
    // Define the AdView object for our activity
    AdView admobView;
    // Define the GameView object for our activity
    GameView gameView;
    // Define the Linear Layout for our activity
    LinearLayout layout;
    LinearLayout.LayoutParams layoutParameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // We remove the title bar that appears at the top of the application
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // We make the application full screen by choosing the flag and mask
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // We set up the activity's content
        gameView = new GameView(this);

        createAdmobView();

        createLinearLayout();

        layout.addView(admobView, layoutParameters);
        layout.addView(gameView, layoutParameters);

        setContentView(layout);

        createAndLoadAdRequest();

    }

    private void createLinearLayout() {
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        // Set the background of our layout to match the settings
        if (GameView.dayMode) {
            layout.setBackgroundColor(Color.WHITE);
        } else if (!GameView.dayMode) {
            layout.setBackgroundColor(Color.BLACK);
        }
        // Create the parameters for our layout
        layoutParameters = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        admobView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        // We override the activity's onPause to include our game view's onPause
        super.onPause();
        admobView.pause();
        gameView.onPause();
    }

    @Override
    protected void onResume() {
        // We override the activity's onResume to include our game view's
        // onResume
        super.onResume();
        admobView.resume();
        gameView.onResume();
    }

    private void createAdmobView() {
        admobView = new AdView(this);
        admobView.setAdUnitId(AD_UNIT_ID);
        admobView.setAdSize(AdSize.SMART_BANNER);

    }

    private void createAndLoadAdRequest() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("04CE99C831F1558D759E784AA1661BBF")
                .build();
        // Start loading the ad in the background.
        admobView.loadAd(adRequest);
    }
}
