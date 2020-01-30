/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;
import org.sikuli.basics.FileManager;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;
import org.sikuli.util.AbortableScriptRunnerWrapper;

/**
 * Runs a packed sikulix script
 *
 * @author mbalmer
 */

public class ZipRunner extends AbstractLocalFileScriptRunner {

  public static final String NAME = "PackedSikulix";
  public static final String TYPE = "application/zip";
  public static final String[] EXTENSIONS = new String[] { "zip" };

  private AbortableScriptRunnerWrapper wrapper = new AbortableScriptRunnerWrapper();

  @Override
  public boolean isSupported() {
    return true;
  }

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public String[] getExtensions() {
    return EXTENSIONS.clone();
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public boolean canHandle(String identifier) {
    if (super.canHandle(identifier)) {
      try (ZipFile file = openZipFile(identifier)) {
          ZipEntry innerScriptFile = getScriptEntry(file);
          return null != innerScriptFile;
      } catch (IOException e) {
        log(-1, "Error opening file %s: %s", identifier, e.getMessage());
        return false;
      }
    }
    return false;
  }

  public boolean hasTempBundle() {
    return true;
  }

  protected ZipFile openZipFile(String identifier) throws IOException {
    return new ZipFile(identifier);
  }

  protected String getScriptEntryName(ZipFile file) {
    return FilenameUtils.getBaseName(file.getName());
  }

  @Override
  protected int doRunScript(String zipFile, String[] scriptArgs, IScriptRunner.Options options) {
    EffectiveRunner runnerAndFile = getEffectiveRunner(zipFile);
    IScriptRunner runner = runnerAndFile.getRunner();
    String innerScriptFile = runnerAndFile.getScript();
    try {
      if(null != innerScriptFile) {
        wrapper.setRunner(runner);
        return runner.runScript(innerScriptFile, scriptArgs, options);
      }
      return Runner.FILE_NOT_FOUND;
    } finally {
      wrapper.clearRunner();
      if (null != innerScriptFile) {
        boolean success = FileManager.deleteFileOrFolder(new File(innerScriptFile).getParentFile());
        if (!success) {
          log(-1, "Error deleting tmp dir %s", new File(innerScriptFile).getParentFile());
        }
      }
    }
  }

  public EffectiveRunner getEffectiveRunner(String zipFile) {
    String innerScriptFilePath = null;
    try (ZipFile file = new ZipFile(zipFile)) {
      ZipEntry innerScriptFile = getScriptEntry(file);
      if(null != innerScriptFile) {
        File dir = extract(file);
        innerScriptFilePath = dir.getAbsolutePath() + File.separator + innerScriptFile.getName();
      }
    } catch (IOException e) {
      log(-1, "Error opening file %s: %s", zipFile, e.getMessage());
    }
    if (null != innerScriptFilePath) {
      return new EffectiveRunner(Runner.getRunner(innerScriptFilePath), innerScriptFilePath, null);
    }
    return new EffectiveRunner();
  }

  private ZipEntry getScriptEntry(ZipFile file) {
    Enumeration<? extends ZipEntry> entries = file.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();

      if (FilenameUtils.getBaseName(entry.getName()).equals(getScriptEntryName(file))) {
        for (IScriptRunner runner : Runner.getRunners()) {
          if (runner.canHandle(entry.getName())) {
            return entry;
          }
        }
      }
    }
    return null;
  }

  private File extract(ZipFile jar) throws IOException {

    File dir = Files.createTempDirectory("sikulix").toFile();

    Enumeration<? extends ZipEntry> entries = jar.entries();

    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();

      File f = new File(dir.getAbsolutePath() + File.separator + entry.getName());

      if (entry.isDirectory()) {
        f.mkdirs();
      } else {
        try (InputStream is = jar.getInputStream(entry)) {
          try (FileOutputStream fo = new java.io.FileOutputStream(f)) {
            while (is.available() > 0) {
              fo.write(is.read());
            }
          }
        }
      }
    }

    return dir;
  }

  @Override
  public boolean isAbortSupported() {
    return wrapper.isAbortSupported();
  }

  @Override
  protected void doAbort() {
    wrapper.doAbort();
  }
}
