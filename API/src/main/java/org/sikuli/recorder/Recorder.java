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

    private boolean recording = false;
    private CaptureScreenshots captureScreenshots;
    private CaptureUserInputs captureUserInputs;

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
        captureScreenshots.startCapturing("sikulix-recorder","sikuliximage", 1000);
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
        captureUserInputs.stopRecording("\\sikulix-recorder\\input-history.xml");
    }

}
