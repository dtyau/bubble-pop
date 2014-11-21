/**
 *
 * Author: Daniel Tak Yin Au
 *
 * Version 1.0
 *
 */

package com.danielau.pop;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameThread extends Thread {

    // Initialize the tag for Android's logging framework LogCat
    //private static final String TAG = GameThread.class.getSimpleName();

    // We need the surface holder in order to control and draw to the surface
    // view
    private final SurfaceHolder surfaceHolder;

    // We need the game view in order to interact with the input
    private GameView gameView;

    // Flag to hold the game state
    private boolean running;

    public GameThread(SurfaceHolder surfaceHolder, GameView gameView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gameView = gameView;
    }

    // Setter to define game state
    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {

        // Run our thread on high priority to keep from getting slowed down
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_DISPLAY);

        // Initiate the canvas that will be drawn upon
        Canvas canvas;

        while (running) {

            // Clear our canvas of drawings from previous loops
            canvas = null;
            // Try locking the canvas so it cannot be access by others when
            // editing the surface
            try {
                canvas = this.surfaceHolder.lockCanvas();
                // To avoid the null canvas pointer, execute the below block only if canvas is not null
                if (canvas != null) {
                    // This puts our code into a synchronized block so that nothing
                    // can modify what is inside while we are using it
                    synchronized (surfaceHolder) {

                        this.gameView.update();
                        this.gameView.render(canvas);

                    }
                }
            } finally {
                // In case of an exception, we avoid leaving the surface in an
                // inconsistent state
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }

        }
    }

}
