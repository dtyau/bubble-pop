/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;

import java.util.Random;

public class Velocity {

    // We define the constants for direction and speed
    public static final float RELATIVE_SPEED = 256; //64 // Lower is faster
    public static final float LIMIT_MODE_BASE_SPEED = (float) 3.5; // Higher is faster
    public static final float DIRECTION_LEFT = -1;
    public static final float DIRECTION_RIGHT = 1;
    public static final float DIRECTION_UP = -1;
    public static final float DIRECTION_DOWN = 1;
    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = Velocity.class.getSimpleName();
    public Random r;
    private float speedX, speedY, directionX, directionY;

    // Constructs initial values that are random
    public Velocity() {
        r = new Random();
        randomizeVelocity();
    }

    public void randomizeVelocity() {
        // We need some score numbers to set the speed appropriately
        float popped = GameView.getPopped();
        float multiplier = GameView.getMultiplier();
        float modifier = Bubble.getModifierS();
        // We give our bubble the ability to not move only at 45 degree angles
        float anglesX = r.nextFloat();
        float anglesY = 1 - anglesX;
        if (GameView.isLimitedPopsON()) {
            speedX = (((anglesX * popped) * LIMIT_MODE_BASE_SPEED)
                    / (popped + RELATIVE_SPEED));
            speedY = (((anglesY * popped) * LIMIT_MODE_BASE_SPEED)
                    / (popped + RELATIVE_SPEED));
        } else if (!GameView.isLimitedPopsON()) {
            /*speedX = (float) ((anglesX * popped) / ((Math.pow(multiplier + popped
                    + RELATIVE_SPEED, (modifier)))));
            speedY = (float) ((anglesY * popped) / ((Math.pow(multiplier + popped
                    + RELATIVE_SPEED, (modifier)))));*/
            speedX = (anglesX * popped * modifier)
                    / (multiplier + popped + RELATIVE_SPEED);
            speedY = (anglesY * popped * modifier)
                    / (multiplier + popped + RELATIVE_SPEED);
        }
        // We randomize the direction (velocity is a vector!)
        if (r.nextBoolean()) {
            directionX = DIRECTION_RIGHT;
        } else {
            directionX = DIRECTION_LEFT;
        }
        if (r.nextBoolean()) {
            directionY = DIRECTION_DOWN;
        } else {
            directionY = DIRECTION_UP;
        }
    }

    // We use these to quickly change the direction
    public void toggleDirectionX() {
        directionX *= -1;
    }

    public void toggleDirectionY() {
        directionY *= -1;
    }

    // Auto-generated Getters and Setters

    public float getSpeedX() {
        return speedX;
    }

    public float getSpeedY() {
        return speedY;
    }

    public float getDirectionX() {
        return directionX;
    }

    public float getDirectionY() {
        return directionY;
    }

}
