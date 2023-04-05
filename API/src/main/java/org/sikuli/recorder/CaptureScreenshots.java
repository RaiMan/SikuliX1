package org.sikuli.recorder;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * Repeats the capture with a delay, during which other calls to this method are ignored.
 * Screenshots are taken with a time interval (i.e. every second)
 *   and not based on user interaction (mouse movement, key press, etc.). There may be a number
 *   of user interactions between two screenshots. Time intervals are used for the following reasons:
 *   - User interactions can take place in short time intervals, which would result in a large number of screenshots.
 *   - Playback, especially slow-motion playback, is much easier to implement if the screenshots are taken
 *     at regular time intervals.
 *   - Screenshots could be used for a machine learning algorithm, in which the outputs are the user's
 *     interactions for a given time interval and the inputs are screenshots. Since we
 *     need to match screenshots with future interactions, and interactions do not take place at regular time intervals,
 *     user interactions do not provide any useful information to help us determine when to take screenshots.
 *     We need to have some regularization of our inputs and outputs, and time intervals is a practical choice.
 *
 * @author jspinak
 */
public class CaptureScreenshots {

    private final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public synchronized void startCapturing(SaveToFile saveToFile, String baseFilename, int delayInMilliseconds) {
        CaptureScreenshot captureScreenshot = new CaptureScreenshot(saveToFile);
        SCHEDULER.scheduleAtFixedRate((() -> {
            captureScreenshot.saveScreenshot(baseFilename);
        }), 0, delayInMilliseconds, MILLISECONDS);
    }

    public synchronized void stopCapturing() {
        SCHEDULER.shutdown();
    }
}
