/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;

import com.danielau.pop.GameView.GameState;

public class Bubble extends Velocity {

    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = Bubble.class.getSimpleName();
    // Initialize the scale divider used for movement increments, increasing
    // this will result in slower velocities
    private static final float SCALE_DIVIDER = 256;
    // Maximum updates allowed for drawing a bonus
    private static final float MAX_DRAW_MESSAGE_COUNTER = 34;
    // The percentage in decimals of how small we allow the bubble to shrink the
    // player has failed
    private static final float MINIMUM_SIZE = (float) 0.05;
    // Tolerance percentages for setting a new location
    private static final float NEW_LOCATION_X_TOLERANCE = (float) 0.2;
    private static final float NEW_LOCATION_Y_TOLERANCE = (float) 0.1;
    // We make this static so game view can access it
    public static float modifierS;
    // Game over string for limited pops
    public boolean completed;
    // Since we want to grab resources from this object, it needs the context
    private Context context;
    private Bitmap bitmap, original;
    // Define variables for the location, size of bitmap and size of canvas
    // properties
    private int x, y, width, height, canvasWidth, canvasHeight, message_X,
            message_Y;
    private float scale, scaleIncrement, drawMessageCounter, drawMessageAlpha,
            message_X_Boundary, message_Y_Boundary;
    private Boolean touched, drawMessage;
    // For the floating messages
    private String message;

    // This is a constructor for our object, taking inputs and assigning them
    // to this object's properties
    public Bubble(Context context, int canvasWidth, int canvasHeight) {
        this.context = context;
        // We re-create the bitmap so it is no longer scaled and randomize the colour
        createBubbleBitmap();
        // Our new bubbles start in the center
        x = canvasWidth / 2;
        y = canvasHeight / 2;
        // We make our bubble aware of the size of the screen
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        touched = false;
        // We make our bubble aware of it's own current size
        width = bitmap.getWidth();
        height = bitmap.getHeight();
        // This is used to scale down the bubble
        scale = (float) width;
        scaleIncrement = scale / SCALE_DIVIDER;
        // We set the modifier dependent on the scale to 1 by default
        modifierS = 1;
        // Make our draw bonus variables
        drawMessage = false;
        drawMessageCounter = 0;
        message_X_Boundary = canvasWidth;
        message_Y_Boundary = canvasHeight;
    }

    public static float getModifierS() {
        return modifierS;
    }

    // We update the bubble's private properties with it's velocity extension
    // properties that might have been set by the game view
    public void update() {
        // Shrink (scale down) our bubble
        decreaseScale();

        // Check if the bubble is small enough to cause game over
        checkGameOver();

        // Create the scaled down bitmap image
        scaleDownBitmap();

        // Update the bubble's new coordinates that are partially handled in
        // game view
        updateLocation();

        // Deal with the draw bonus counter
        updateDrawMessage();

    }

    // Our object's own draw function so that it draws itself when handed a
    // canvas
    public void draw(Canvas canvas, Paint paint) {

        // Draw the bonus message
        if (drawMessage) {
            paint.setTextAlign(Align.CENTER);
            paint.setAlpha((int) drawMessageAlpha);
            canvas.drawText(message, message_X, message_Y, paint);
            paint.setAlpha(255);
        }

        // We draw it the bitmap such that the center of the bitmap will be at
        // the center of the canvas since bitmap's point of reference is at the
        // center
        canvas.drawBitmap(bitmap, x - (bitmap.getWidth() / 2),
                y - (bitmap.getHeight() / 2), paint);
    }

    // Used to determine if the touch event is touching our circle object
    public void handleActionDown(int eventX, int eventY) {
        if (eventX >= (x - bitmap.getWidth() / 2)
                && (eventX <= (x + bitmap.getWidth() / 2))) {
            if (eventY >= (y - bitmap.getHeight() / 2)
                    && (eventY <= (y + bitmap.getHeight() / 2))) {
                touched = true;
            }
        } else {
            // Ensuring that touched is false
            touched = false;
        }
    }

    // Used to direct changes involved with event action up
    public void handleActionUp() {
        // Set the modifier before we reset the scale
        updateModifierS();
        // Check for message
        if (!GameView.isLimitedPopsON()) {
            checkBonus();
        }
        // Reset the scale of the bubble
        scale = (float) width;

        // We reset the bubble's position and speed
        randomizeLocation();
        // This function found in Velocity class
        randomizeVelocity();
        // We reset the bubble's bitmap so it is no longer scaled and randomize the colour
        createBubbleBitmap();
        // Recognize that it is no longer touched
        setTouched(false);
    }

    // This is used to *respawn* the bubble at a new location while keeping it's
    // edges within the screen
    public void randomizeLocation() {
        // Get the current location so we can relocate the bubble to
        // somewhere different 100% of the time
        int previousLocationX = getX();
        int previousLocationY = getY();
        // First, give it a new location then check that it is not touching the
        // edges
        setX((int) (r.nextFloat() * canvasWidth));
        setY((int) (r.nextFloat() * canvasHeight));
        // The condition below is to re-calculate the locations if any edges
        // of the bitmap is outside the screen OR if it is within the tolerance
        // of the old location
        while (x - this.width / 2 < 0 ||
                x + this.width / 2 > canvasWidth ||
                Math.abs(getX() - previousLocationX) <=
                        (int) (NEW_LOCATION_X_TOLERANCE * canvasWidth)) {
            setX((int) (r.nextFloat() * canvasWidth));
        }
        while (y - this.height / 2 < 0 ||
                y + this.height / 2 > canvasHeight ||
                Math.abs(getY() - previousLocationY) <=
                        (int) (NEW_LOCATION_Y_TOLERANCE * canvasHeight)) {
            setY((int) (r.nextFloat() * canvasHeight));
        }

    }

    // This will decode the bubble bitmap and clone it for scaling
    // It also triggers a new colour
    public void createBubbleBitmap() {
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.black);
        // We duplicate the bitmap so we have an original to scale from
        original = bitmap;
        ColourManager.getRandomColour();

    }

    public void reset() {
        randomizeVelocity();
        // We recreate the bitmap so it is no longer scaled and randomize the colour
        createBubbleBitmap();
        // Our new bubbles start in the center
        x = canvasWidth / 2;
        y = canvasHeight / 2;
        touched = false;
        scale = (float) width;
        // We set the modifier dependent on the scale to 1 by default
        modifierS = 1;
        // Make our draw bonus variables
        drawMessage = false;
        drawMessageCounter = 0;
        message_X_Boundary = canvasWidth;
        message_Y_Boundary = canvasHeight;
    }

    private void decreaseScale() {

        // We want to make a scaled image that is scaled directly from the
        // original to reduce loss of image definition
        scale -= scaleIncrement;

        if (!GameView.isLimitedPopsON()) {
            // Shrink the bubble faster depending on the modifier which is set
            // upon popping a bubble
            /*if (modifierS > 1.3) {
                scale -= (scaleIncrement * 2);
                if (modifierS > 1.5) {
                    scale -= (scaleIncrement * 2);
                }
            }*/
            if (modifierS < 1.4) {
                scale -= (scaleIncrement / 2);
                if (modifierS < 1.2) {
                    scale -= (scaleIncrement / 2);
                }
            }
        }

    }

    private void scaleDownBitmap() {
        // If the bubble's size is still greater than the minimum...
        if (scale > (this.width * MINIMUM_SIZE)
                || scale > (this.height * MINIMUM_SIZE)) {
            // Then we scale down the bitmap by the new decreased values
            int scaleInt = (int) scale;
            bitmap = Bitmap.createScaledBitmap(original, scaleInt, scaleInt,
                    true);
        }
    }

    private void checkGameOver() {
        // If the bubble's size is smaller than the minimum...
        if (scale <= (this.width * MINIMUM_SIZE)
                || scale <= (this.height * MINIMUM_SIZE)) {
            // Induce game over
            completed = false;
            GameView.setGameOverMessage("game over");
            GameView.gameState = GameState.GAMEOVER;

            // Play game over sound
            if (GameView.isAudioON()) {
                Effects.play(Effects.SOUNDEFFECT_GAMEOVER, 0.3f, 0);
            }
        }
        // Or for limited pops mode
        if (GameView.isLimitedPopsON()) {
            if (GameView.getPopped() >= GameView.POP_LIMIT) {
                completed = true;
                GameView.setGameOverMessage("victory");
                GameView.gameState = GameState.GAMEOVER;
            }
        }
    }

    private void updateLocation() {
        // Only update the location if it is not touched
        if (!touched) {
            x += (width * getSpeedX() * getDirectionX());
            y += (height * getSpeedY() * getDirectionY());
        }
    }

    private void checkBonus() {
        if (modifierS <= 1.2) {
            message = "2x!";
            drawMessage = true;
            drawMessageCounter = 0;
            drawMessageAlpha = 255;
            // Set the location of the bonus
            if (x < (message_X_Boundary * 0.3)) {
                message_X = (int) (x + (message_X_Boundary * 0.1));
            } else if (x > message_X_Boundary * 0.7) {
                message_X = (int) (x - (message_X_Boundary * 0.1));
            } else {
                message_X = x;
            }
            if (y < (message_Y_Boundary * 0.2)) {
                message_Y = (int) (y + (message_Y_Boundary * 0.1));
            } else if (y > message_Y_Boundary * 0.8) {
                message_Y = (int) (y - (message_Y_Boundary * 0.1));
            } else {
                message_Y = y;
            }
        } else {
            drawMessage = false;
        }
    }

    public void addPenalty() {
        message = "+" + Integer.toString(GameView.TIME_PENALTY / 1000) + " s!";
        drawMessage = true;
        drawMessageCounter = 0;
        drawMessageAlpha = 255;
        // Set the location of the bonus
        if (x < (message_X_Boundary * 0.3)) {
            message_X = (int) (x + (message_X_Boundary * 0.1));
        } else if (x > message_X_Boundary * 0.7) {
            message_X = (int) (x - (message_X_Boundary * 0.1));
        } else {
            message_X = x;
        }
        if (y < (message_Y_Boundary * 0.2)) {
            message_Y = (int) (y + (message_Y_Boundary * 0.1));
        } else if (y > message_Y_Boundary * 0.8) {
            message_Y = (int) (y - (message_Y_Boundary * 0.1));
        } else {
            message_Y = y;
        }
    }

    private void updateDrawMessage() {
        if (drawMessage) {
            drawMessageCounter++;
            drawMessageAlpha = 255 * (1 - (drawMessageCounter / MAX_DRAW_MESSAGE_COUNTER));
        }
        if (drawMessageCounter > MAX_DRAW_MESSAGE_COUNTER) {
            drawMessage = false;
        }
    }

    private void updateModifierS() {
        // This calculation results in a percentage + 1 representative of the
        // size the bubble was popped at; big bubble -> small number
        modifierS = ((1 - (scale / (float) width)) + 1);
    }

    // Auto-generated Getters and Setters below for this object's properties

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Boolean isTouched() {
        return touched;
    }

    public void setTouched(Boolean touched) {
        this.touched = touched;
    }

    public Boolean isCompleted() {
        return completed;
    }

}
