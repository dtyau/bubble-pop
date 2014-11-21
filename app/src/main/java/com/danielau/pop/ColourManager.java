/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;


import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

import java.util.ArrayList;
import java.util.Random;

public class ColourManager {

    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = ColourManager.class.getSimpleName();

    // The number of available colours starting the count from 1
    private static final int NUMBER_OF_COLOURS = 9;
    private static final int MAX_NUMBER_RECORDED_COLOURS = NUMBER_OF_COLOURS / 2;

    private static String colourHex;
    private static ColorFilter colourFilter;
    private static ArrayList<Integer> previousColours = new ArrayList<Integer>();
    private static int colour;
    private static Random r;

    public static void initColourManager() {
        r = new Random();
        colour = r.nextInt(NUMBER_OF_COLOURS);
        getRandomColour();
    }

    /*
    private static void log() {
        String colourInformation = "ColourManager: ";
        if (previousColours.size() > 1) {
            for (Integer previousColour : previousColours) {
                colourInformation = colourInformation + previousColour.toString();
            }
            Log.d(TAG, colourInformation);
        }
    }
    */

    public static void getRandomColour() {
        while (previousColours.contains(colour)) {
            colour = r.nextInt(NUMBER_OF_COLOURS);
        }
        previousColours.add(colour);
        if (previousColours.size() > MAX_NUMBER_RECORDED_COLOURS) {
            previousColours.remove(0);
        }
        switch (colour) {
            case 0: // Green
                colourHex = "#81C784";
                break;
            case 1: // Orange
                colourHex = "#FFB74D";
                break;
            case 2: // Purple
                colourHex = "#9575CD";
                break;
            case 3: // Red
                colourHex = "#E57373";
                break;
            case 4: // Teal
                colourHex = "#4DB6AC";
                break;
            case 5: // Yellow
                colourHex = "#FFEB3B";
                break;
            case 6: // Pink
                colourHex = "#F06292";
                break;
            case 7: // Blue
                colourHex = "#64B5F6";
                break;
            case 8: // Lime
                colourHex = "#CDDC39";
                break;
            default: // Green
                colourHex = "#81C784";
                break;
        }
        colourFilter = new LightingColorFilter(Color.BLACK, Color.parseColor(colourHex));

    }

    public static String getColourHex() {
        return colourHex;
    }

    public static ColorFilter getColourFilter() {
        return colourFilter;
    }

}
