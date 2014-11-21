/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.plus.Plus;
import com.google.example.games.basegameutils.BaseGameUtils;

public class MainActivity extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = MainActivity.class.getSimpleName();
    // Initialize the key for day mode shared preferences
    private static final String KEY_DAY_MODE = "DayOrNight";
    // Initialize the key for audio mode shared preferences
    private static final String KEY_AUDIO_MODE = "AudioMode";
    // Initialize the key for vibration mode shared preferences
    private static final String KEY_VIBRATE_MODE = "VibrateMode";
    // Initialize the key for limited pops mode shared preferences
    private static final String KEY_LIMITEDPOPS_MODE = "LimitedPopsMode";
    // Initialize the key for auto sign in shared preferences
    private static final String KEY_AUTOSIGNIN = "AutoSignIn";

    // Initialize the google api client
    public static GoogleApiClient mGoogleApiClient;
    // Some value used for connection failure to Google Api Client
    private static final int RC_SIGN_IN = 8891;
    private static final int REQUEST_ACHIEVEMENTS = 7891;
    private static final int REQUEST_LEADERBOARDS = 6891;
    private static boolean googlePlaySignedIn = false;
    private boolean mResolvingConnectionFailure = false;
    // Initialize the boolean used for shared preferences
    private boolean dayMode, audioON, vibrateON, autoSignIn, limitedPopsON;

    public static boolean isGooglePlaySignedIn() {
        return googlePlaySignedIn;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Effects.initSoundEffects(this);
        ColourManager.initColourManager();

        loadSharedPreferences();

        // We remove the title bar that appears at the top of the application
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // We make the application full screen by choosing the flag and mask
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        setDayNightMode();
        setAudioMode();
        setVibrateMode();
        setLimitedPopsMode();
        setGooglePlay();
        createGoogleApiClient();

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mResolvingConnectionFailure && autoSignIn) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        ColourManager.getRandomColour();
        setCustomTextViewColour(R.id.customtextview_title);
        setCustomTextViewColour(R.id.customtextview_login);
        setDayNightMode();
        setAudioMode();
        setVibrateMode();
        setLimitedPopsMode();
        setGooglePlay();
        super.onResume();
    }

    @Override
    public void onConnected(Bundle connectionHint) {

        googlePlaySignedIn = true;
        autoSignIn = true;
        editSharedPreferencesAutoSignIn();
        setGooglePlay();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        // Attempt to reconnect
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Set boolean of Google play sign in to false
        googlePlaySignedIn = false;
        autoSignIn = false;
        editSharedPreferencesAutoSignIn();

        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        mResolvingConnectionFailure = true;

        // Attempt to resolve the connection failure using BaseGameUtils.
        if (!BaseGameUtils.resolveConnectionFailure(this,
                mGoogleApiClient, connectionResult,
                RC_SIGN_IN, getString(R.string.signin_other_error))) {
            mResolvingConnectionFailure = false;
        }

        // Update the Google play sign in button
        setGooglePlay();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {

        if (requestCode == RC_SIGN_IN) {
            // Failure resolved
            mResolvingConnectionFailure = false;
            // If the resolution is good, we prompt sign-in
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in failed
                BaseGameUtils.showActivityResultError(this, requestCode,
                        resultCode, R.string.signin_failure,
                        R.string.signin_other_error);
            }
        }
    }

    public void startGame(View view) {
        playEffects();
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    private void playEffects() {
        if (audioON) {
            Effects.play(Effects.SOUNDEFFECT_POP, 1.0f, 1);
        }
        if (vibrateON) {
            Effects.vibrate(50);
        }
    }

    private void loadSharedPreferences() {
        // Load the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        dayMode = pref.getBoolean(KEY_DAY_MODE, true);
        audioON = pref.getBoolean(KEY_AUDIO_MODE, true);
        vibrateON = pref.getBoolean(KEY_VIBRATE_MODE, true);
        limitedPopsON = pref.getBoolean(KEY_LIMITEDPOPS_MODE, true);
        autoSignIn = pref.getBoolean(KEY_AUTOSIGNIN, true);
    }

    private void editSharedPreferencesDayMode() {
        // Edit the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_DAY_MODE, dayMode);
        editor.apply();
    }

    private void editSharedPreferencesAudioMode() {
        // Edit the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_AUDIO_MODE, audioON);
        editor.apply();
    }

    private void editSharedPreferencesVibrateMode() {
        // Edit the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_VIBRATE_MODE, vibrateON);
        editor.apply();
    }

    private void editSharedPreferencesLimitedPopsMode() {
        // Edit the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_LIMITEDPOPS_MODE, limitedPopsON);
        editor.apply();
    }

    private void editSharedPreferencesAutoSignIn() {
        // Edit the shared preferences for the game
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(KEY_AUTOSIGNIN, autoSignIn);
        editor.apply();
    }

    private void setDayNightMode() {
        // Load the proper activity layout depending on shared preferences
        if (!dayMode) {
            setImage(R.id.button_dayMode, R.drawable.sun);
            setImage(R.id.button_play, R.drawable.explosion);
            getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        } else {
            setImage(R.id.button_dayMode, R.drawable.moon);
            setImage(R.id.button_play, R.drawable.explosion);
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        }
    }

    private void setAudioMode() {
        if (audioON) {
            setImage(R.id.button_audio, R.drawable.audio);
        } else {
            setImage(R.id.button_audio, R.drawable.noaudio);
        }
    }

    private void setVibrateMode() {
        if (vibrateON) {
            setImage(R.id.button_vibrate, R.drawable.vibrate);
        } else {
            setImage(R.id.button_vibrate, R.drawable.novibrate);
        }
    }

    private void setLimitedPopsMode() {
        if (limitedPopsON) {
            setImage(R.id.button_limitedPops, R.drawable.limitedpops);
        } else {
            setImage(R.id.button_limitedPops, R.drawable.nolimitedpops);
        }
    }

    private void createGoogleApiClient() {
        // Create the Google Api Client for the play games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
    }

    private void setGooglePlay() {
        if (!googlePlaySignedIn) {

            setImage(R.id.button_googlePlay, R.drawable.googleplay);

        } else {

            ImageButton button_googleplay = (ImageButton) findViewById(R.id.button_googlePlay);
            button_googleplay.setVisibility(View.GONE);
            CustomTextView customTextView = (CustomTextView) findViewById(R.id.customtextview_login);
            customTextView.setVisibility(View.GONE);
            ImageButton button_achievements = (ImageButton) findViewById(R.id.button_achievements);
            button_achievements.setVisibility(View.VISIBLE);
            ImageButton button_leaderboards = (ImageButton) findViewById(R.id.button_leaderboard);
            button_leaderboards.setVisibility(View.VISIBLE);
            setImage(R.id.button_achievements, R.drawable.achievements);
            setImage(R.id.button_leaderboard, R.drawable.leaderboards);

        }
    }

    private void setImage(int RidButton, int RidDrawable) {
        ImageButton button = (ImageButton) findViewById(RidButton);
        button.setColorFilter(ColourManager.getColourFilter());
        button.setImageResource(RidDrawable);
    }

    private void setCustomTextViewColour(int RidTextView) {
        CustomTextView customTextView = (CustomTextView) findViewById(RidTextView);
        customTextView.setTextColor(Color.parseColor(ColourManager.getColourHex()));
    }

    private void actionsOnPress() {
        playEffects();
        ColourManager.getRandomColour();
        setDayNightMode();
        setAudioMode();
        setVibrateMode();
        setLimitedPopsMode();
        setGooglePlay();
        setCustomTextViewColour(R.id.customtextview_title);
        setCustomTextViewColour(R.id.customtextview_login);
    }

    public void toggleDayNightMode(View view) {
        // Put day night mode toggle here
        if (dayMode) {
            dayMode = false;
            editSharedPreferencesDayMode();
        } else {
            dayMode = true;
            editSharedPreferencesDayMode();
        }
        actionsOnPress();
    }

    public void toggleAudio(View view) {
        if (audioON) {
            audioON = false;
            editSharedPreferencesAudioMode();
        } else {
            audioON = true;
            editSharedPreferencesAudioMode();
        }
        actionsOnPress();
    }

    public void toggleVibration(View view) {
        if (vibrateON) {
            vibrateON = false;
            editSharedPreferencesVibrateMode();
        } else {
            vibrateON = true;
            editSharedPreferencesVibrateMode();
        }
        actionsOnPress();
    }

    public void toggleLimitedPops(View view) {
        if (limitedPopsON) {
            limitedPopsON = false;
            editSharedPreferencesLimitedPopsMode();
        } else {
            limitedPopsON = true;
            editSharedPreferencesLimitedPopsMode();
        }
        actionsOnPress();
    }

    public void toggleGooglePlay(View view) {
        if (mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
        actionsOnPress();
    }

    public void showAchievements(View view) {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIEVEMENTS);
        }
        actionsOnPress();
    }

    public void showLeaderboards(View view) {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Leaderboards
                    .getAllLeaderboardsIntent(mGoogleApiClient), REQUEST_LEADERBOARDS);
        }
        actionsOnPress();
    }

}
