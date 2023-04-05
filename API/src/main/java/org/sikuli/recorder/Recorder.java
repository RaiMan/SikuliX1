package org.sikuli.recorder;

import org.sikuli.script.support.Commons;
import org.w3c.dom.Document;

import java.io.File;
import java.util.Date;

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
    private SaveToFile saveToFile;
    private ProcessRecording processRecording;

    private boolean recording = false;
    private File recordingFolder = new File(Commons.getTempFolder(), "Recorder");
    private int screenshotDelay = 1000;

    Recorder() {
        captureScreenshots = new CaptureScreenshots();

        captureUserInputs = new CaptureUserInputs(new RecordInputsXML());

        processRecording = new ProcessRecording(new ProcessNodePauses());
    }

    /**
     * Starts capturing screenshots and recording the user's actions<br>
     * The screenshots are saved in the directory defined below as sikuliximage_TIMESTAMP.png.
     */
    public void startRecording() {
        if (recording) return;
        recording = true;

        File folderWithDate = Commons.asFolder(new File(recordingFolder, "recording"  + "-" + new Date().getTime()).getAbsolutePath());
        saveToFile = new RecorderSaveToFile(folderWithDate);

        captureScreenshots.startCapturing(saveToFile, "sikuliximage", screenshotDelay);
        captureUserInputs.startRecording();
    }

    /**
     * Stops recording the user's actions and capturing screenshots.<br>
     * The user's actions raw data are saved in the path defined below as: sikulixrawinputs.xml<br>
     * Finally the raw actions are reduced to relevant actions: sikulixinputs.xml
     */
    public void stopRecording() {
        if (!recording) return;
        recording = false;

        captureScreenshots.stopCapturing();

        Document rawDoc = captureUserInputs.finalizeRecording();
        saveToFile.saveXML(rawDoc, "sikulixrawinputs.xml");
        Document simplifiedDoc = processRecording.process(rawDoc);
        saveToFile.saveXML(simplifiedDoc, "sikulixinputs.xml");
    }

    public boolean setRecordingDirectory(String directory) {
        if (recording) return false;
        recordingFolder = new File(directory);
        return true;
    }

    public boolean setRecordingFolder(File folder) {
        if (recording) return false;
        recordingFolder = folder;
        return true;
    }
    public boolean setScreenshotDelay(int delay) {
        if (recording) return false;
        screenshotDelay = delay;
        return true;
    }

}
