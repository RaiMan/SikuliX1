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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.sikuli.script.support.IScriptRunner;
import org.sikuli.script.support.Runner;

/**
 * Runs a packed sikulix script
 *
 * @author mbalmer
 */

public class SKLRunner extends ZipRunner {

  public static final String NAME = "PackedSikulix";
  public static final String TYPE = "text/skl";
  public static final String[] EXTENSIONS = new String[] { "skl" };
  
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
}
