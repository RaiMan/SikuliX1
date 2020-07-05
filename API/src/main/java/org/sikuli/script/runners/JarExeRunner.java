/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.script.runners;

import org.apache.commons.io.FilenameUtils;

import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * Runs a sikulix script packed in a jar file
 *
 * @author mbalmer
 *
 */

public class JarExeRunner extends ZipRunner {

  public static final String NAME = "SikulixExecutableJar";
  public static final String TYPE = "text/jar";
  public static final String[] EXTENSIONS = new String[] {"executablejar"};

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

  public EffectiveRunner getEffectiveRunner(String zipFile) {
    return super.getEffectiveRunner(zipFile.replace(".executablejar", ".jar"));
  }

  @Override
  protected ZipFile openZipFile(String identifier) throws IOException {
    return new JarFile(identifier.replace(".executablejar", ".jar"));
  }

  @Override
  protected String getScriptEntryName(ZipFile file) {
    return FilenameUtils.getBaseName(file.getName()).replace("_sikuli", "") + "$py";
  }
}
