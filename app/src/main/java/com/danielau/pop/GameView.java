/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 * Typography font, 'Fins', created by Jake Kho.
 *
 */

package com.danielau.pop;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    // Max number of pops for for limited pop mode
    public static final int POP_LIMIT = 21;
    // Time penalty for missing the bubble
    public static final int TIME_PENALTY = 1000; // milliseconds
    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = GameView.class.getSimpleName();
    // Initialize static final variables used in the game for easy tuning
    private static final int RELATIVE_TEXT = 14; // Smaller is larger text
    // This defines the increase when incrementing the collision multiplier
    private static final double MULTIPLIERC_INCREMENT = 0.05;
    // Default time in milliseconds for vibration before adjustment
    private static final int VIBRATION_TIME = 500;
    // The following are keys for the shared preferences
    private static final String KEY_AUDIO_MODE = "AudioMode";
    private static final String KEY_VIBRATE_MODE = "VibrateMode";
    private static final String KEY_DAY_MODE = "DayOrNight";
    private static final String KEY_LIMITEDPOPS_MODE = "LimitedPopsMode";
    private static final String KEY_HIGHSCORE = "HighestScore";
    private static final String KEY_HIGHPOP = "HighestPop";
    private static final String KEY_HIGHMULTIPLIER = "HighestMultiplier";
    private static final String KEY_BESTTIME = "BestTIme";
    // The following are ids for achievements
    private static final String INCREMENTAL_ACHIEVEMENT_PLAY100ROUNDS = "CgkIjcr5lN8fEAIQBw";
    private static final String INCREMENTAL_ACHIEVEMENT_PLAY1000ROUNDS = "CgkIjcr5lN8fEAIQDg";
    private static final String ACHIEVEMENT_POP0BUBBLES = "CgkIjcr5lN8fEAIQCQ";
    private static final String ACHIEVEMENT_POP25BUBBLES = "CgkIjcr5lN8fEAIQAw";
    private static final String ACHIEVEMENT_POP100BUBBLES = "CgkIjcr5lN8fEAIQBA";
    private static final String INCREMENTAL_ACHIEVEMENT_POP300BUBBLES = "CgkIjcr5lN8fEAIQCg";
    private static final String ACHIEVEMENT_1337SCORE = "CgkIjcr5lN8fEAIQBQ";
    private static final String ACHIEVEMENT_25000SCORE = "CgkIjcr5lN8fEAIQBg";
    private static final String ACHIEVEMENT_50000SCORE = "CgkIjcr5lN8fEAIQCw";
    private static final String INCREMENTAL_ACHIEVEMENT_100000SCORE = "CgkIjcr5lN8fEAIQDA";
    private static final String ACHIEVEMENT_DARKNESS = "CgkIjcr5lN8fEAIQCA";
    private static final String ACHIEVEMENT_VICTORY = "CgkIjcr5lN8fEAIQEA";
    private static final String ACHIEVEMENT_30SECONDS = "CgkIjcr5lN8fEAIQEQ";
    private static final String ACHIEVEMENT_26SECONDS = "CgkIjcr5lN8fEAIQEg";
    private static final String ACHIEVEMENT_22SECONDS = "CgkIjcr5lN8fEAIQEw";
    private static final String ACHIEVEMENT_18SECONDS = "CgkIjcr5lN8fEAIQFA";
    private static final String ACHIEVEMENT_14SECONDS = "CgkIjcr5lN8fEAIQFQ";
    private static final String ACHIEVEMENT_12SECONDS = "CgkIjcr5lN8fEAIQFg";
    // IDs for leaderboards
    private static final String LEADERBOARD_SCORE = "CgkIjcr5lN8fEAIQAQ";
    private static final String LEADERBOARD_POPPED = "CgkIjcr5lN8fEAIQDQ";
    private static final String LEADERBOARD_TIME = "CgkIjcr5lN8fEAIQDw";
    // Request codes for Google Play
    private static final int REQUEST_ACHIEVEMENTS = 7891;
    private static final int REQUEST_LEADERBOARDS = 6891;
    // Define our game state enum object
    public static GameState gameState;
    // Define our other static variables
    public static int rollingScore, score, popped;
    public static float multiplier, multiplierC, multiplierM, allMultipliers,
            highestMultiplier;
    // Define boolean for day or night mode and audio mode
    public static boolean dayMode, audioON, vibrateON;
    // Define boolean for limited pops mode
    public static boolean limitedPopsON;
    // Define String for game over message for limited pops mode
    public static String gameOverMessage;
    // Define our thread for executing updates and rendering
    private GameThread gameThread;
    // Define our bubble object
    private Bubble bubble;
    // Define non-static variables
    private int canvasWidth, canvasHeight, local_HighestScore,
            local_HighestPop, achievements_x, achievements_y, leaderboard_x, leaderboard_y;
    private long startTime, elapsedTime, local_BestTime, penaltyTime;
    // We currently are not displaying local highest multiplier
    private String displayMultipliers, displaylocal_HighestMultipliers;
    // The following are for local high scores
    private Float local_HighestMultiplier;
    private boolean new_Pop, new_Multiplier, new_Score, paused, started,
            googlePlayServicesChecked, localScoreUpdated;
    // Define our paint object and typeface object
    private Paint scorePaint;
    private Typeface tf;
    private Bitmap achievements, leaderboard;
    // Define our float formatter for display representation
    private DecimalFormat dF, decimalFormatTime;
    // Initialize the Google Api Client
    private GoogleApiClient mGoogleApiClient;

    public GameView(Context context) {
        super(context);

        started = false;

        // Adds the callback to the surface holder allowing us to intercept
        // events (eg. surface destruction or orientation changes)
        getHolder().addCallback(this);


        // Create the game loop thread by getting the surface view's holder and
        // using this game view (so the thread can access both)
        gameThread = new GameThread(getHolder(), this);

        // Make the GameView focusable so it can handle events (eg. touch)
        setFocusable(true);

        // Load the device's shared preferences
        // We want to load this before the sound effects constructor in case
        // audio is disabled
        loadSharedPreferences();

        // Construct our sound effects class
        if (audioON || vibrateON) {
            Effects.initSoundEffects(context);
        }

    }

    public static int getPopped() {
        return popped;
    }

    public static float getMultiplier() {
        return multiplier;
    }

    public static boolean isAudioON() {
        return audioON;
    }

    public static boolean isLimitedPopsON() {
        return limitedPopsON;
    }

    public static void setGameOverMessage(String message) {
        gameOverMessage = message;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Determine the screen size
        canvasWidth = getWidth();
        canvasHeight = getHeight();

        // After surface creation, set the running flag and start the thread
        gameState = GameState.RUNNING;
        gameThread.setRunning(true);

		/*
         * Create our bubble object and feed it the activity context and screen
		 * size. This is done here so that the surface exists without question
		 * and the screen size is known
		 */
        bubble = new Bubble(getContext(), canvasWidth, canvasHeight);

        // Instantiate decimal formatter used to change the multiplier float to
        // something displayable
        dF = new DecimalFormat("#.#");
        // Instantiate another for elapsed time
        decimalFormatTime = new DecimalFormat("#.##");

        // Create and set properties of our paint object
        createPaint();

        // Since the surface already exists when this is called, it is safe to
        // start the game thread
        if (!started) {
            gameThread.start();
            started = true;
        }

        // Reset the game values to ensure all variables are set for game play
        reset();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        // Before the surface is destroyed, this tells the thread to shut down
        // by blocking input
        // and allowing it to finish it's current task. It is a clean shutdown.
        boolean retry = true;
        while (retry) {
            try {
                gameThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // Catch an error and prints it if thread is some how still
                // active
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // If we find that a touch pressed gesture has occurred...
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            if (gameState == GameState.RUNNING) {
                // Otherwise, delegate the touch event handling to the bubble
                bubble.handleActionDown((int) event.getX(), (int) event.getY());
            }
        }

        // If we find that a touch released gesture has occurred...
        if (event.getAction() == MotionEvent.ACTION_UP) {

            if (gameState == GameState.RUNNING) {

                // And the bubble was touched
                if (bubble.isTouched()) {

                    // Play the sound!
                    if (audioON) {
                        Effects.play(Effects.SOUNDEFFECT_POP, 1.0f, 0);
                    }

                    // Deal with scoring
                    updateHighestMultiplier();
                    addScore();
                    // Add to popped since a bubble was popped
                    popped++;

                    // Delegate the touch event handling to the bubble
                    bubble.handleActionUp();
                    // Update the colour filter since the bubble will have changed it
                    scorePaint.setColorFilter(ColourManager.getColourFilter());

                } else if (!bubble.isTouched()) {
                    // Punish the player for missing the bubble by reducing
                    // multipliers
                    reduceMultipliers();

                    // We don't want to exclude the multiplier business above since it affects speed
                    if (limitedPopsON) {
                        bubble.addPenalty();
                        startTime -= TimeUnit.MILLISECONDS.toNanos(TIME_PENALTY);
                    }
                }
            } else if (gameState == GameState.GAMEOVER) {
                // If game over, trigger a new game by resetting all values
                if ((event.getY() > (float) canvasHeight * 0.05)
                        && (event.getY() < (float) canvasHeight * 0.15)
                        && (event.getX() > (float) canvasWidth * 0.2)
                        && (event.getX() < (float) canvasWidth * 0.8)) {
                    playEffects();
                    reset();
                }

                if (MainActivity.isGooglePlaySignedIn()) {

                    if ((event.getY() > (float) canvasHeight * 0.9)
                            && (event.getX() > (float) canvasWidth * 0.35)
                            && (event.getX() < (float) canvasWidth * 0.45)) {
                        playEffects();

                        if (mGoogleApiClient.isConnected()) {
                            ((Activity) getContext()).startActivityForResult(Games.Achievements
                                    .getAchievementsIntent(mGoogleApiClient), REQUEST_ACHIEVEMENTS);
                        }
                    }

                    if ((event.getY() > (float) canvasHeight * 0.9)
                            && (event.getX() > (float) canvasWidth * 0.55)
                            && (event.getX() < (float) canvasWidth * 0.65)) {
                        playEffects();

                        if (mGoogleApiClient.isConnected()) {
                            ((Activity) getContext()).startActivityForResult(Games.Leaderboards
                                    .getAllLeaderboardsIntent(mGoogleApiClient), REQUEST_LEADERBOARDS);
                        }
                    }
                }

            }
        }

        // return super.onTouchEvent(event);
        return true; // No idea why we return true instead of super
    }

    public void update() {
        if (gameState == GameState.RUNNING) {

            // If our bubble exists...
            if (bubble != null) {
                if (limitedPopsON) {
                    elapsedTime = System.nanoTime() - startTime;
                }

                // Collision checking for our bubble
                checkCollision();

                // Update the multiplier since there may have been collisions
                updateMultipliers();

                // Update the rolling score
                updateRollingScore();

                // Delegate bubble updates to itself
                bubble.update();
            }
        } else if (gameState == GameState.GAMEOVER) {
            /*
             * Update the local high scores and keep popped and score tied
			 * together note that the booleans are set to false in reset()
			 */
            if (!localScoreUpdated) {
                updateLocalHighScores();
            }

            if (MainActivity.isGooglePlaySignedIn() && !googlePlayServicesChecked) {
                checkGooglePlayServices();
            }

        }
    }

    protected void render(Canvas canvas) {

        // Fill the canvas background falling back to WHITE
        if (!dayMode) {
            canvas.drawColor(Color.BLACK);
        } else {
            canvas.drawColor(Color.WHITE);
        }

        if (gameState == GameState.RUNNING) {

            if (bubble != null) {

                // Tell the bubble object to draw itself
                bubble.draw(canvas, scorePaint);

                // Draw the score, popped and multiplier
                if (limitedPopsON) {
                    drawLimitedPopScores(canvas);
                } else {
                    drawScores(canvas);
                }

                // Formerly an onDraw method but renamed to be render
                // super.onDraw(canvas);
            }
        } else if (gameState == GameState.GAMEOVER) {

            // Draw game over screen
            if (limitedPopsON) {
                drawLimitedPopGameOver(canvas);
            } else {
                drawGameOver(canvas);
            }
        }
    }

    public void reset() {
        score = 0;
        rollingScore = 0;
        popped = 0;
        multiplier = 1;
        multiplierC = 1;
        multiplierM = 1;
        highestMultiplier = 1;
        allMultipliers = 1;
        new_Score = false;
        new_Pop = false;
        new_Multiplier = false;
        // Resetting the bubbles calls randomize velocity which depends on above values
        bubble.reset();
        scorePaint.setColorFilter(ColourManager.getColourFilter());
        gameState = GameState.RUNNING;
        startTime = System.nanoTime();
        /*
        * We need to set GameState.RUNNING before score check flags to ensure score checking
        * is only performed after a new game has started. This avoid checking in scores of 0
        * or the local score not being updated due to it having been checked before the new
        * game has started.
        */
        googlePlayServicesChecked = false;
        localScoreUpdated = false;
    }

    public void onPause() {
        if (gameThread.isAlive()) {
            gameThread.setRunning(false);
        }
        paused = true;

        if (mGoogleApiClient != null) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
            }
        }

    }

    public void onResume() {
        if (!gameThread.isAlive()) {
            if (paused) {
                getHolder().addCallback(this);
                gameThread = new GameThread(getHolder(), this);
                gameThread.setRunning(true);
                gameThread.start();
                paused = false;
            }
        }


        if (mGoogleApiClient != null) {
            if (MainActivity.isGooglePlaySignedIn()) {
                mGoogleApiClient.connect();
            }
        }

    }

    private void createPaint() {
        // Create and set the initial paint properties
        tf = Typeface.createFromAsset(getContext().getAssets(),
                "Fins-Regular.ttf");
        scorePaint = new Paint();
        scorePaint.setTypeface(tf);
        scorePaint.setColorFilter(ColourManager.getColourFilter());
        scorePaint.setTextSize(canvasWidth / RELATIVE_TEXT);
        scorePaint.setTextAlign(Align.CENTER);
        scorePaint.setAntiAlias(true);

        // Create assets for painting the google play buttons
        achievements = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.achievements);
        achievements_x = (int) (canvasWidth * 0.4);
        achievements_y = (int) (canvasHeight * 0.95);
        leaderboard = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.leaderboards);
        leaderboard_x = (int) (canvasWidth * 0.6);
        leaderboard_y = (int) (canvasHeight * 0.95);

    }

    private void loadSharedPreferences() {
        // Instantiate the shared preferences and grab the local records
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        dayMode = pref.getBoolean(KEY_DAY_MODE, true);
        audioON = pref.getBoolean(KEY_AUDIO_MODE, true);
        vibrateON = pref.getBoolean(KEY_VIBRATE_MODE, true);
        limitedPopsON = pref.getBoolean(KEY_LIMITEDPOPS_MODE, true);
        local_HighestScore = pref.getInt(KEY_HIGHSCORE, 0);
        local_HighestMultiplier = pref.getFloat(KEY_HIGHMULTIPLIER, 0);
        local_HighestPop = pref.getInt(KEY_HIGHPOP, 0);
        local_BestTime = pref.getLong(KEY_BESTTIME, 0);
    }

    private void editSharedPreferencesHighScores() {
        // Instantiate the shared preferences and update the local records
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(KEY_HIGHSCORE, local_HighestScore);
        editor.putInt(KEY_HIGHPOP, popped);
        editor.putFloat(KEY_HIGHMULTIPLIER, local_HighestMultiplier);
        editor.apply();
    }

    private void editSharedPreferencesBestTime() {
        // Instantiate the shared preferences and update the local records
        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = pref.edit();
        editor.putLong(KEY_BESTTIME, local_BestTime);
        editor.apply();
    }

    private void updateHighestMultiplier() {
        // Update the highest multiplier if higher
        if (allMultipliers > highestMultiplier) {
            highestMultiplier = allMultipliers;
        }
    }

    private void updateMultipliers() {
        // Update all multipliers while changing it into string at the
        // same time
        allMultipliers = multiplier * multiplierC;
        displayMultipliers = dF.format(allMultipliers);
    }

    private void reduceMultipliers() {
        // Make sure the multipliers will never go below 1 before reduction
        if (multiplier > 1) {
            multiplier--;
        }
        // For the collision multiplier, we punish them by 10 times the
        // increment
        if (multiplierC >= (1 + MULTIPLIERC_INCREMENT)) {
            multiplierC -= (10 * MULTIPLIERC_INCREMENT);
            // We want the minimum multiplier to be 1x instead of 1.#x so we allow
            // the reduction above and we save it from going below 1 here
            if (multiplierC < 1) {
                multiplierC = 1;
            }

        }
    }

    private void increaseMultiplierC() {
        multiplierC += MULTIPLIERC_INCREMENT;
    }

    private void updateLocalHighScores() {
        if (limitedPopsON && bubble.isCompleted()) {
            if (local_BestTime == 0) {
                local_BestTime = elapsedTime;
                editSharedPreferencesBestTime();
            } else if (elapsedTime < local_BestTime) {
                local_BestTime = elapsedTime;
                editSharedPreferencesBestTime();
            }
        } else if (!limitedPopsON && score > local_HighestScore) {
            new_Score = true; // is this used...?
            local_HighestScore = score;
            local_HighestPop = popped;
            local_HighestMultiplier = highestMultiplier;
            // Update the field used to display the highest multiplier
            displaylocal_HighestMultipliers = dF
                    .format(local_HighestMultiplier);
            // Update the scores saved in shared preferences
            editSharedPreferencesHighScores();
        }
        localScoreUpdated = true;
    }

    private void addScore() {
        // Check if the player gets a bonus
        if (Bubble.getModifierS() <= 1.2) {
            // Double the multiplier if bubble is popped before shrinking 20%
            multiplierM = 2;
        }
        // Add 1 point adjusted by the multipliers to the score
        score += (int) multiplier * multiplierC * multiplierM;
        // Increase the base multiplier
        multiplier++;
        // Reset the bonus
        multiplierM = 1;

    }

    private void updateRollingScore() {
        if (rollingScore < score) {
            rollingScore += ((score - rollingScore) / 42) + 1;
        }
    }

    private void drawScores(Canvas canvas) {
        // Draw the rolling score
        scorePaint.setTextAlign(Align.LEFT);
        canvas.drawText(Integer.toString(rollingScore),
                (float) (canvasWidth * 0.03), (float) (canvasHeight * 0.05),
                scorePaint);
        // Draw the current multiplier
        scorePaint.setTextAlign(Align.CENTER);
        canvas.drawText(displayMultipliers + "x", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.05), scorePaint);
        // Draw the bubbles popped
        scorePaint.setTextAlign(Align.RIGHT);
        canvas.drawText(Integer.toString(popped), (float) (canvasWidth * 0.97),
                (float) (canvasHeight * 0.05), scorePaint);
    }

    private void drawLimitedPopScores(Canvas canvas) {
        // Draw the bubbles popped
        scorePaint.setTextAlign(Align.RIGHT);
        canvas.drawText(Integer.toString((POP_LIMIT - popped)), (float) (canvasWidth * 0.97),
                (float) (canvasHeight * 0.05), scorePaint);
        // Draw the elapsed time
        scorePaint.setTextAlign(Align.LEFT);
        canvas.drawText(decimalFormatTime.format(TimeUnit.NANOSECONDS.toMillis(elapsedTime) / (float) 1000) + " s",
                (float) (canvasWidth * 0.03),
                (float) (canvasHeight * 0.05), scorePaint);
    }

    private void drawGameOver(Canvas canvas) {
        // Set paint object to center align draws
        scorePaint.setTextAlign(Align.CENTER);
        // Add the underline for 'popped' and 'score'
        scorePaint.setUnderlineText(true);
        // Draw the 'popped' and the 'score'
        canvas.drawText("popped", (float) (canvasWidth * 0.25),
                (float) (canvasHeight * 0.28), scorePaint);
        canvas.drawText("score", (float) (canvasWidth * 0.75),
                (float) (canvasHeight * 0.28), scorePaint);
        // Draw them again for the personal records
        canvas.drawText("popped", (float) (canvasWidth * 0.25),
                (float) (canvasHeight * 0.79), scorePaint);
        canvas.drawText("score", (float) (canvasWidth * 0.75),
                (float) (canvasHeight * 0.79), scorePaint);

        // Take off the underline
        scorePaint.setUnderlineText(false);
        // Draw the values of popped and score
        canvas.drawText(Integer.toString(popped), (float) (canvasWidth * 0.25),
                (float) (canvasHeight * 0.33), scorePaint);
        canvas.drawText(Integer.toString(score), (float) (canvasWidth * 0.75),
                (float) (canvasHeight * 0.33), scorePaint);
        // Draw 'personal record' and the corresponding values for popped and
        // score
        canvas.drawText("personal record", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.73), scorePaint);
        canvas.drawText(Integer.toString(local_HighestPop),
                (float) (canvasWidth * 0.25), (float) (canvasHeight * 0.85),
                scorePaint);
        canvas.drawText(Integer.toString(local_HighestScore),
                (float) (canvasWidth * 0.75), (float) (canvasHeight * 0.85),
                scorePaint);

        // Draw the 'play again' text
        scorePaint.setTextSize(canvasWidth / 10);
        canvas.drawText("play again", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.1), scorePaint);

        // Set the different size and scale for 'game over'
        scorePaint.setTextSize(canvasWidth / 6);
        canvas.drawText("game over", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.54), scorePaint);
        // Restore the original values for the paint object
        scorePaint.setTextSize(canvasWidth / RELATIVE_TEXT);

        // Draw the Google Play stuff
        if (MainActivity.isGooglePlaySignedIn()) {
            canvas.drawBitmap(achievements, achievements_x - (achievements.getWidth() / 2),
                    achievements_y - (achievements.getHeight() / 2), scorePaint);
            canvas.drawBitmap(leaderboard, leaderboard_x - (leaderboard.getWidth() / 2),
                    leaderboard_y - (leaderboard.getHeight() / 2), scorePaint);
        }

    }

    private void drawLimitedPopGameOver(Canvas canvas) {

        scorePaint.setTextAlign(Align.CENTER);
        scorePaint.setUnderlineText(true);

        // Draw the 'elapsed time' for round and personal record
        canvas.drawText("total time", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.28), scorePaint);
        canvas.drawText("personal record", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.73), scorePaint);

        scorePaint.setUnderlineText(false);

        // Draw the values of elapsed time
        canvas.drawText(decimalFormatTime.format(TimeUnit.NANOSECONDS.toMillis(elapsedTime) / (float) 1000) + " s",
                (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.33), scorePaint);
        // Draw personal record time
        if (local_BestTime == 0) {
            canvas.drawText("no record...yet", (float) (canvasWidth * 0.5),
                    (float) (canvasHeight * 0.79), scorePaint);
        } else {
            canvas.drawText(decimalFormatTime.format(TimeUnit.NANOSECONDS.toMillis(local_BestTime) / (float) 1000) + " s",
                    (float) (canvasWidth * 0.5),
                    (float) (canvasHeight * 0.79), scorePaint);
        }

        // Draw the 'play again' text
        scorePaint.setTextSize(canvasWidth / 10);
        canvas.drawText("play again", (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.1), scorePaint);

        // Set the different size and scale for 'game over'
        scorePaint.setTextSize(canvasWidth / 6);
        canvas.drawText(gameOverMessage, (float) (canvasWidth * 0.5),
                (float) (canvasHeight * 0.54), scorePaint);
        // Restore the original values for the paint object
        scorePaint.setTextSize(canvasWidth / RELATIVE_TEXT);

        // Draw the Google Play stuff
        if (MainActivity.isGooglePlaySignedIn()) {
            canvas.drawBitmap(achievements, achievements_x - (achievements.getWidth() / 2),
                    achievements_y - (achievements.getHeight() / 2), scorePaint);
            canvas.drawBitmap(leaderboard, leaderboard_x - (leaderboard.getWidth() / 2),
                    leaderboard_y - (leaderboard.getHeight() / 2), scorePaint);
        }

    }

    private void checkCollision() {
        // Check for collision against right wall if heading right
        if (bubble.getDirectionX() == Velocity.DIRECTION_RIGHT
                && bubble.getX() + bubble.getBitmap().getWidth() / 2 >= canvasWidth) {
            vibrateOnCollision();
            bubble.toggleDirectionX();
            increaseMultiplierC();
        }
        // Check for collision against left wall if heading left
        if (bubble.getDirectionX() == Velocity.DIRECTION_LEFT
                && bubble.getX() - bubble.getBitmap().getWidth() / 2 <= 0) {
            vibrateOnCollision();
            bubble.toggleDirectionX();
            increaseMultiplierC();
        }
        // Check for collision against bottom wall if heading down
        if (bubble.getDirectionY() == Velocity.DIRECTION_DOWN
                && bubble.getY() + bubble.getBitmap().getHeight() / 2 >= canvasHeight) {
            vibrateOnCollision();
            bubble.toggleDirectionY();
            increaseMultiplierC();
        }
        // Check for collision against top wall if heading up
        if (bubble.getDirectionY() == Velocity.DIRECTION_UP
                && bubble.getY() - bubble.getBitmap().getHeight() / 2 <= 0) {
            vibrateOnCollision();
            bubble.toggleDirectionY();
            increaseMultiplierC();
        }
    }

    private void vibrateOnCollision() {
        if (vibrateON) {
            float averageSpeed = (bubble.getSpeedX() + bubble.getSpeedY()) / 2;
            float vibrationTime = averageSpeed * VIBRATION_TIME;
            Effects.vibrate((int) vibrationTime);
        }
    }

    private void playEffects() {
        if (audioON) {
            Effects.play(Effects.SOUNDEFFECT_POP, 1.0f, 1);
        }
        if (vibrateON) {
            Effects.vibrate(50);
        }
    }

    // This checks for a connection and deals with the achievements
    private void checkGooglePlayServices() {
        while (mGoogleApiClient == null) {
            mGoogleApiClient = MainActivity.mGoogleApiClient;
        }
        while (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }

        unlockAchievements();

        submitToLeaderboards();

        googlePlayServicesChecked = true;

    }

    private void unlockAchievements() {

        // Deal with playing in the dark achievement
        if (!dayMode) {
            Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_DARKNESS);
        }

        // Increment once for each round played
        Games.Achievements.increment(mGoogleApiClient, INCREMENTAL_ACHIEVEMENT_PLAY100ROUNDS, 1);
        Games.Achievements.increment(mGoogleApiClient, INCREMENTAL_ACHIEVEMENT_PLAY1000ROUNDS, 1);

        // Deal with game mode twenty one achievements
        if (limitedPopsON && bubble.isCompleted()) {
            // Achievement for completing
            Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_VICTORY);

            if (elapsedTime <= TimeUnit.SECONDS.toNanos(30)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_30SECONDS);
            }
            if (elapsedTime <= TimeUnit.SECONDS.toNanos(26)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_26SECONDS);
            }
            if (elapsedTime <= TimeUnit.SECONDS.toNanos(22)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_22SECONDS);
            }
            if (elapsedTime <= TimeUnit.SECONDS.toNanos(18)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_18SECONDS);
            }
            if (elapsedTime <= TimeUnit.SECONDS.toNanos(14)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_14SECONDS);
            }
            if (elapsedTime <= TimeUnit.SECONDS.toNanos(12)) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_12SECONDS);
            }


        } else {
            // Deal with game mode normal achievements
            // Deal with bubbles popped achievements
            if (popped == 0) {
                // Unlock this if the user popped 0 bubbles
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_POP0BUBBLES);
            }
            if (popped >= 25) {
                // Unlock this if the user popped 25 or more bubbles
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_POP25BUBBLES);
            }
            if (popped >= 100) {
                // Unlock this if the user popped 100 or more bubbles
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_POP100BUBBLES);
            }
            // Set steps for the incremental achievement 300 bubbles popped
            if (popped > 1) {
                Games.Achievements.setSteps(mGoogleApiClient, INCREMENTAL_ACHIEVEMENT_POP300BUBBLES, popped);
            }

            // Deal with score achievements
            if (score == 1337) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_1337SCORE);
            }
            if (score > 25000) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_25000SCORE);
            }
            if (score > 50000) {
                Games.Achievements.unlock(mGoogleApiClient, ACHIEVEMENT_50000SCORE);
            }
            // Set steps for the incremental achievement 100,000 score
            if (score > 1000) {
                int steps = (int) Math.floor(score / 1000);
                Games.Achievements.setSteps(mGoogleApiClient, INCREMENTAL_ACHIEVEMENT_100000SCORE, steps);
            }
        }

    }

    private void submitToLeaderboards() {

        // Submit time to leaderboard
        if (limitedPopsON && bubble.isCompleted()) {
            Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_TIME,
                    TimeUnit.NANOSECONDS.toMillis(elapsedTime));
        } else {
            // Submit score to leaderboard
            Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_SCORE, score);
            // Submit popped to leaderboard
            Games.Leaderboards.submitScore(mGoogleApiClient, LEADERBOARD_POPPED, popped);
        }

    }

    // Our game state definition
    enum GameState {
        RUNNING, GAMEOVER
    }

}
