package org.sikuli.recorder;

/**
 * This package implements a recorder for SikuliX. The saved recordings can be used in SikuliX or other programs,
 *   e.g. for training a neural network.
 *
 * Features include:
 * - playback
 * - code generation for scripting
 * - a file containing mouse and keyboard actions
 * - screenshots saved at regular intervals during recording
 *
 * @author jspinak
 */
public enum Recorder {

    INSTANCE;

    private CaptureScreenshots captureScreenshots;
    private CaptureUserInputs captureUserInputs;
    private boolean recording = false;
    private String recordingDirectory = "sikulix-recorder";
    private String screenshotBaseFilename = "sikuliximage";
    private int screenshotDelay = 1000;

    Recorder() {
        captureScreenshots = new CaptureScreenshots();
        captureUserInputs = new CaptureUserInputs(new RecordInputsXML());
    }

    /**
     * Starts recording the user's actions and capturing screenshots.
     * The screenshots are saved in the directory defined below.
     */
    public void startRecording() {
        if (recording) return;
        recording = true;
        captureScreenshots.startCapturing(recordingDirectory, screenshotBaseFilename, screenshotDelay);
        captureUserInputs.startRecording();
    }

    /**
     * Stops recording the user's actions and capturing screenshots.
     * The user's actions are saved in the path defined below.
     */
    public void stopRecording() {
        if (!recording) return;
        recording = false;
        captureScreenshots.stopCapturing();
        captureUserInputs.stopRecording("\\" + recordingDirectory + "\\input-history.xml");
    }

    public boolean setRecordingDirectory(String directory) {
        if (recording) return false;
        recordingDirectory = directory;
        return true;
    }

    public boolean setScreenshotBaseFilename(String filename) {
        if (recording) return false;
        screenshotBaseFilename = filename;
        return true;
    }

    public boolean setScreenshotDelay(int delay) {
        if (recording) return false;
        screenshotDelay = delay;
        return true;
    }

}
