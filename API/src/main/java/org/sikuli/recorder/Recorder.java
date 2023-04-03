package org.sikuli.recorder;

import org.sikuli.script.support.Commons;
import org.w3c.dom.Document;

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
    private SaveFile saveFile;
    private ProcessRecording processRecording;

    private boolean recording = false;
    private String recordingDirectory = Commons.getTempFolder().getName();
    private String dirWithDate;
    private String screenshotBaseFilename = "sikuliximage";
    private int screenshotDelay = 1000;

    Recorder() {
        captureScreenshots = new CaptureScreenshots();
        RecordInputs recordInputs = new RecordInputsXML();
        captureUserInputs = new CaptureUserInputs(recordInputs);
        saveFile = new StandardSaveToFile();
        ProcessNode processNode = new ProcessNodePauses();
        processRecording = new ProcessRecording(processNode);
    }

    /**
     * Starts recording the user's actions and capturing screenshots.
     * The screenshots are saved in the directory defined below.
     */
    public void startRecording() {
        if (recording) return;
        recording = true;
        dirWithDate = recordingDirectory  + "-" + new Date().getTime();
        captureScreenshots.startCapturing(dirWithDate, screenshotBaseFilename, screenshotDelay);
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
        String pathToSaveRawData = String.format("\\%s\\%s", dirWithDate, "sikulix_rawinputs.xml");
        Document rawDoc = captureUserInputs.finalizeRecording();
        saveFile.saveXML(rawDoc, pathToSaveRawData);
        Document simplifiedDoc = processRecording.process(rawDoc);
        String pathToSaveForSimplifiedDoc = String.format("\\%s\\%s", dirWithDate, "sikulix_inputs.xml");
        saveFile.saveXML(simplifiedDoc, pathToSaveForSimplifiedDoc);
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
