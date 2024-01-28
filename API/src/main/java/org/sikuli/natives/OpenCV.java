package org.sikuli.natives;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.sikuli.basics.Debug;
import org.sikuli.script.support.Commons;
import org.sikuli.script.support.RunTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class OpenCV {

  static enum OS {
    OSX("^[Mm]ac OS X$"),
    LINUX("^[Ll]inux$"),
    WINDOWS("^[Ww]indows.*");

    private final Set<Pattern> patterns;

    private OS(final String... patterns) {
      this.patterns = new HashSet<Pattern>();

      for (final String pattern : patterns) {
        this.patterns.add(Pattern.compile(pattern));
      }
    }

    private boolean is(final String id) {
      for (final Pattern pattern : patterns) {
        if (pattern.matcher(id).matches()) {
          return true;
        }
      }
      return false;
    }

    public static OS getCurrent() {
      final String osName = System.getProperty("os.name");

      for (final OS os : OS.values()) {
        if (os.is(osName)) {
          return os;
        }
      }

      throw new UnsupportedOperationException(String.format("Operating system \"%s\" is not supported.", osName));
    }
  }

  static enum Arch {
    X86_32("i386", "i686", "x86"),
    X86_64("amd64", "x86_64"),
    ARMv7("arm"),
    ARMv8("aarch64", "arm64");

    private final Set<String> patterns;

    private Arch(final String... patterns) {
      this.patterns = new HashSet<String>(Arrays.asList(patterns));
    }

    private boolean is(final String id) {
      return patterns.contains(id);
    }

    public static Arch getCurrent() {
      final String osArch = System.getProperty("os.arch");

      for (final Arch arch : Arch.values()) {
        if (arch.is(osArch)) {
          return arch;
        }
      }

      throw new UnsupportedOperationException(String.format("Architecture \"%s\" is not supported.", osArch));
    }
  }

  private static class UnsupportedPlatformException extends RuntimeException {
    private UnsupportedPlatformException(final OS os, final Arch arch) {
      super(String.format("Operating system \"%s\" and architecture \"%s\" are not supported.", os, arch));
    }
  }

  /**
   * Extracts the packaged binary (if available) to the given destination
   */
  public static File extractNativeBinary(File destination) {
    final OS os = OS.getCurrent();
    final Arch arch = Arch.getCurrent();
    File outFile = null;
    try {
      outFile = extractNativeBinary(os, arch, destination);
    } catch (UnsupportedPlatformException e) {
    }
    return outFile;
  }

  /**
   * Extracts the packaged binary for the specified platform to the given destination
   */
  private static File extractNativeBinary(final OS os, final Arch arch, File destination) throws UnsupportedPlatformException {
    String location;
    String libName = Commons.getLibFilename(libOpenCV);

    switch (os) {
      case LINUX:
        switch (arch) {
          case X86_64:
            location = "/nu/pattern/opencv/linux/x86_64/";
            break;
          case ARMv8:
            location = "/nu/pattern/opencv/linux/ARMv8/";
            break;
          default:
            throw new UnsupportedPlatformException(os, arch);
        }
        break;
      case OSX:
        switch (arch) {
          case X86_64:
            location = "/nu/pattern/opencv/osx/x86_64/";
            break;
          case ARMv8:
            location = "/nu/pattern/opencv/osx/ARMv8/";
            break;
          default:
            throw new UnsupportedPlatformException(os, arch);
        }
        break;
      case WINDOWS:
        switch (arch) {
          case X86_64:
            location = "/nu/pattern/opencv/windows/x86_64/";
            break;
          default:
            throw new UnsupportedPlatformException(os, arch);
        }
        break;
      default:
        throw new UnsupportedPlatformException(os, arch);
    }

    location += libName;

    File outFile = new File(destination, libName);
    if (!outFile.exists()) {
      try (FileOutputStream outStream = new FileOutputStream(outFile);
           InputStream inStream = Commons.class.getResourceAsStream(location)) {
        Commons.copy(inStream, outStream);
        Debug.log(3, "OpenCV lib: %s (%s %s)", outFile, os, arch);
      } catch (Exception ex) {
        return null;
      }
    }
    return outFile;
  }

  private static final String libOpenCV = Core.NATIVE_LIBRARY_NAME;

  public static File load() {
    File lib = extractNativeBinary(Commons.getLibsFolder());
    if(null != lib) {
      lib = Commons.loadLib(lib);
      if (null == lib) {
        Debug.error("Trying system-wide load for: opencv_java");
        lib = Commons.loadLib(new File("opencv_java"));
      }
      if (null != lib) {
        try {
          new Mat();
          Debug.log(3,"OpenCV::load: %s", lib);
        } catch (Exception e) {
          Debug.error("FATAL: loadOpenCV: %s not not useable (%s)", lib, e.getMessage());
          lib = null;
        }
      }
    }
    return lib;
  }
}