/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.basics;

import org.sikuli.script.RunTime;

import java.io.File;
import java.net.InetAddress;
import java.net.Proxy;
import java.util.Date;

import org.sikuli.script.Image;

/**
 * This is the container for all
 */
public class Settings {

    public static boolean experimental = false;

    private static String me = "Settings: ";
    private static int lvl = 3;
    public static boolean noPupUps = false;
    public static boolean FindProfiling = false;

    public static boolean InputFontMono = false;
    public static int InputFontSize = 14;

    private static void log(int level, String message, Object... args) {
        Debug.logx(level, me + message, args);
    }

    public static boolean runningSetupInValidContext = false;
    public static String runningSetupInContext = null;
    public static String runningSetupWithJar = null;
    public static boolean isRunningIDE = false;

    public static int breakPoint = 0;
    public static boolean handlesMacBundles = true;
    public static boolean runningSetup = false;
    private static PreferencesUser prefs;

    /**
     * location of folder Tessdata
     */
    public static String OcrDataPath = null;
    /**
     * standard place in the net to get information about extensions<br>
     * needs a file extensions.json with content<br>
     * {"extension-list":<br>
     * &nbsp;{"extensions":<br>
     * &nbsp;&nbsp;[<br>
     * &nbsp;&nbsp;&nbsp;{<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"name":"SikuliGuide",<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"version":"0.3",<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"description":"visual annotations",<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"imgurl":"somewhere in the net",<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"infourl":"http://doc.sikuli.org",<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;"jarurl":"---extensions---"<br>
     * &nbsp;&nbsp;&nbsp;},<br>
     * &nbsp;&nbsp;]<br>
     * &nbsp;}<br>
     * }<br>
     * imgurl: to get an icon from<br>
     * infourl: where to get more information<br>
     * jarurl: where to download the jar from (no url: this standard place)<br>
     */

//<editor-fold defaultstate="collapsed" desc="moved to RunTime">
//  public static int SikuliVersionMajor;
//	public static int SikuliVersionMinor;
//	public static int SikuliVersionSub;
//	public static int SikuliVersionBetaN;
//	public static String SikuliProjectVersionUsed = "";
//	public static String SikuliProjectVersion = "";
//	public static String SikuliVersionBuild;
//	public static String SikuliVersionType;
//	public static String SikuliVersionTypeText;
//	public static String downloadBaseDirBase;
//	public static String downloadBaseDirWeb;
//	public static String downloadBaseDir;
//	// used for download of production versions
//	private static final String dlProdLink = "https://launchpad.net/raiman/sikulix2013+/";
//	private static final String dlProdLink1 = ".0";
//	private static final String dlProdLink2 = "/+download/";
//	// used for download of development versions (nightly builds)
//	private static final String dlDevLink = "http://nightly.sikuli.de/";
//	public static String SikuliRepo;
//	public static String SikuliLocalRepo = "";
//	public static String[] ServerList = {"http://dl.dropboxusercontent.com/u/42895525/SikuliX"};
//	private static String sversion;
//	private static String bversion;
//	public static String SikuliVersionDefault;
//	public static String SikuliVersionBeta;
//	public static String SikuliVersionDefaultIDE;
//	public static String SikuliVersionBetaIDE;
//	public static String SikuliVersionDefaultScript;
//	public static String SikuliVersionBetaScript;
//	public static String SikuliVersion;
//	public static String SikuliVersionIDE;
//	public static String SikuliVersionScript;
//	public static String SikuliJythonVersion;
//	public static String SikuliJythonVersion25 = "2.5.4-rc1";
//	public static String SikuliJythonMaven;
//	public static String SikuliJythonMaven25;
//	public static String SikuliJython;
//	public static String SikuliJRubyVersion;
//	public static String SikuliJRuby;
//	public static String SikuliJRubyMaven;
//	public static String dlMavenRelease = "https://repo1.maven.org/maven2/";
//	public static	String dlMavenSnapshot = "https://oss.sonatype.org/content/groups/public/";
//
//	public static Map<String, String> tessData = new HashMap<String, String>();
//
//	//TODO needed ???
//	public static final String libOpenCV = "libopencv_java248";
//
//	public static String osName;
//  public static String SikuliVersionLong;
//  public static String SikuliSystemVersion;
//  public static String SikuliJavaVersion;
//</editor-fold>

    public static final float FOREVER = Float.POSITIVE_INFINITY;
    public static final int JavaVersion = makeJavaVersion();

    private static int makeJavaVersion() {
        int major = 0;
        String jversion = java.lang.System.getProperty("java.version");
        if (jversion.startsWith("1.")) {
            try {
                major = Integer.parseInt(jversion.substring(2, 3));
            } catch (Exception ex) {
            }
        } else if (jversion.startsWith("9.")) {
            major = 9;
        }
        return major;
    }

    public static final String JREVersion = java.lang.System.getProperty("java.runtime.version");
    public static final String JavaArch = System.getProperty("os.arch");

    /**
     * Resource types to be used with IResourceLoader implementations
     */
    public static final String SIKULI_LIB = "*sikuli_lib";

    public static String proxyName = "";
    public static String proxyIP = "";
    public static InetAddress proxyAddress = null;
    public static String proxyPort = "";
    public static boolean proxyChecked = false;
    public static Proxy proxy = null;

    /**
     * INTERNAL USE: to trigger the initialization
     */
    public static synchronized void init() {
// TODO check existence of an extension repository
//TODO Windows:
//Mrz 23, 2015 12:25:43 PM java.util.prefs.WindowsPreferences <init>
//WARNING: Could not open/create prefs root node Software\JavaSoft\Prefs
//at root 0x80000002. Windows RegCreateKeyEx(...) returned error code 5.
//    prefs = PreferencesUser.getInstance();
//    proxyName = prefs.get("ProxyName", null);
//    String proxyIP = prefs.get("ProxyIP", null);
//    InetAddress proxyAddress = null;
//    String proxyPort = prefs.get("ProxyPort", null);

//<editor-fold defaultstate="collapsed" desc="moved to RunTime">
//    SikuliRepo = null;
//		Properties prop = new Properties();
//		String svf = "sikulixversion.txt";
//		try {
//			InputStream is;
//			is = Settings.class.getClassLoader().getResourceAsStream("Settings/" + svf);
//			prop.load(is);
//			is.close();
//			String svt = prop.getProperty("sikulixdev");
//			SikuliVersionMajor = Integer.decode(prop.getProperty("sikulixvmaj"));
//			SikuliVersionMinor = Integer.decode(prop.getProperty("sikulixvmin"));
//			SikuliVersionSub = Integer.decode(prop.getProperty("sikulixvsub"));
//			SikuliVersionBetaN = Integer.decode(prop.getProperty("sikulixbeta"));
//			String ssxbeta = "";
//			if (SikuliVersionBetaN > 0) {
//				ssxbeta = String.format("-Beta%d", SikuliVersionBetaN);
//			}
//			SikuliVersionBuild = prop.getProperty("sikulixbuild");
//			log(lvl + 1, "%s version from %s: %d.%d.%d%s build: %s", svf,
//							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, ssxbeta,
//							SikuliVersionBuild, svt);
//			sversion = String.format("%d.%d.%d",
//							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub);
//			bversion = String.format("%d.%d.%d-Beta%d",
//							SikuliVersionMajor, SikuliVersionMinor, SikuliVersionSub, SikuliVersionBetaN);
//			SikuliVersionDefault = "SikuliX " + sversion;
//			SikuliVersionBeta = "Sikuli " + bversion;
//			SikuliVersionDefaultIDE = "SikulixIDE " + sversion;
//			SikuliVersionBetaIDE = "SikulixIDE " + bversion;
//			SikuliVersionDefaultScript = "SikulixScript " + sversion;
//			SikuliVersionBetaScript = "SikulixScript " + bversion;
//
//			if ("release".equals(svt)) {
//				downloadBaseDirBase = dlProdLink;
//				downloadBaseDirWeb = downloadBaseDirBase + getVersionShortBasic() + dlProdLink1;
//				downloadBaseDir = downloadBaseDirWeb + dlProdLink2;
//        SikuliVersionType = "";
//        SikuliVersionTypeText = "";
//			} else {
//				downloadBaseDirBase = dlDevLink;
//				downloadBaseDirWeb = dlDevLink;
//				downloadBaseDir = dlDevLink;
//        SikuliVersionTypeText = "nightly";
//        SikuliVersionBuild += SikuliVersionTypeText;
//        SikuliVersionType = svt;
//			}
//			if (SikuliVersionBetaN > 0) {
//				SikuliVersion = SikuliVersionBeta;
//				SikuliVersionIDE = SikuliVersionBetaIDE;
//				SikuliVersionScript = SikuliVersionBetaScript;
//        SikuliVersionLong = bversion + "(" + SikuliVersionBuild + ")";
//			} else {
//				SikuliVersion = SikuliVersionDefault;
//				SikuliVersionIDE = SikuliVersionDefaultIDE;
//				SikuliVersionScript = SikuliVersionDefaultScript;
//        SikuliVersionLong = sversion + "(" + SikuliVersionBuild + ")";
//			}
//			SikuliProjectVersionUsed = prop.getProperty("sikulixvused");
//			SikuliProjectVersion = prop.getProperty("sikulixvproject");
//      String osn = "UnKnown";
//      String os = System.getProperty("os.name").toLowerCase();
//      if (os.startsWith("mac")) {
//        osn = "Mac";
//      } else if (os.startsWith("windows")) {
//        osn = "Windows";
//      } else if (os.startsWith("linux")) {
//        osn = "Linux";
//      }
//
//			SikuliLocalRepo = FileManager.slashify(prop.getProperty("sikulixlocalrepo"), true);
//			SikuliJythonVersion = prop.getProperty("sikulixvjython");
//			SikuliJythonMaven = "org/python/jython-standalone/" +
//							 SikuliJythonVersion + "/jython-standalone-" + SikuliJythonVersion + ".jar";
//			SikuliJythonMaven25 = "org/python/jython-standalone/" +
//							 SikuliJythonVersion25 + "/jython-standalone-" + SikuliJythonVersion25 + ".jar";
//			SikuliJython = SikuliLocalRepo + SikuliJythonMaven;
//			SikuliJRubyVersion = prop.getProperty("sikulixvjruby");
//			SikuliJRubyMaven = "org/jruby/jruby-complete/" +
//							 SikuliJRubyVersion + "/jruby-complete-" + SikuliJRubyVersion + ".jar";
//			SikuliJRuby = SikuliLocalRepo + SikuliJRubyMaven;
//
//      SikuliSystemVersion = osn + System.getProperty("os.version");
//      SikuliJavaVersion = "Java" + JavaVersion + "(" + JavaArch + ")" + JREVersion;
////TODO this should be in RunSetup only
////TODO debug version: where to do in sikulixapi.jar
////TODO need a function: reveal all environment and system information
////      log(lvl, "%s version: downloading from %s", svt, downloadBaseDir);
//		} catch (Exception e) {
//			Debug.error("Settings: load version file %s did not work", svf);
//			Sikulix.terminate(999);
//		}
//		tessData.put("eng", "http://tesseract-ocr.googlecode.com/files/tesseract-ocr-3.02.eng.tar.gz");
//</editor-fold>

        getOS();
    }

    @Deprecated
    public static String getInstallBase() {
        return RunTime.get().fSxBase.getAbsolutePath();
    }

    public static boolean isValidImageFilename(String fname) {
        String validEndings = ".png.jpg.jpeg";
        String defaultEnding = ".png";
        int dot = fname.lastIndexOf(".");
        String ending = defaultEnding;
        if (dot > 0) {
            ending = fname.substring(dot);
            if (validEndings.contains(ending.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static String getValidImageFilename(String fname) {
        if (isValidImageFilename(fname)) {
            return fname;
        }
        return fname + ".png";
    }

    public static final int ISWINDOWS = 0;
    public static final int ISMAC = 1;
    public static final int ISLINUX = 2;
    public static final int ISNOTSUPPORTED = 3;
    public static boolean isMacApp = false;
    public static boolean isWinApp = false;
    public static final String appPathMac = "/Applications/SikuliX-IDE.app/Contents";

    public static boolean ThrowException = true; // throw FindFailed exception
    public static float AutoWaitTimeout = 3f; // in seconds
    public static float WaitScanRate = 3f; // frames per second
    public static float ObserveScanRate = 3f; // frames per second
    public static int ObserveMinChangedPixels = 50; // in pixels
    public static int RepeatWaitTime = 1; // wait 1 second for visual to vanish after action
    public static double MinSimilarity = 0.7;
    public static boolean CheckLastSeen = true;
    public static float CheckLastSeenSimilar = 0.95f;
    public static boolean UseImageFinder = false;

    private static int ImageCache = 64;

    /**
     * set the maximum to be used for the {@link Image} cache
     * <br>the start up value is 64 (meaning MB)
     * <br>using 0 switches off caching and clears the cache in that moment
     *
     * @param max cache size in MB
     */
    public static void setImageCache(int max) {
        if (ImageCache > max) {
            Image.clearCache(max);
        }
        ImageCache = max;
    }

    public static int getImageCache() {
        return ImageCache;
    }

    public static double DelayValue = 0.3;
    public static double DelayBeforeMouseDown = DelayValue;
    @Deprecated
    // use DelayBeforeDrag instead
    public static double DelayAfterDrag = DelayValue;
    public static double DelayBeforeDrag = -DelayValue;
    public static double DelayBeforeDrop = DelayValue;

    /**
     * Specify a delay between the key presses in seconds as 0.nnn. This only
     * applies to the next type and is then reset to 0 again. A value &gt; 1 is cut
     * to 1.0 (max delay of 1 second)
     */
    public static double TypeDelay = 0.0;
    /**
     * Specify a delay between the mouse down and up in seconds as 0.nnn. This
     * only applies to the next click action and is then reset to 0 again. A value
     * &gt; 1 is cut to 1.0 (max delay of 1 second)
     */
    public static double ClickDelay = 0.0;
    public static boolean ClickFast = false;
    public static boolean RobotFake = true;

    public static String BundlePath = null;
    public static boolean OcrTextSearch = true;
    public static boolean OcrTextRead = true;
    public static String OcrLanguage = "eng";

    /**
     * true = start slow motion mode, false: stop it (default: false) show a
     * visual for SlowMotionDelay seconds (default: 2)
     */
    public static boolean TRUE = true;
    public static boolean FALSE = false;

    private static boolean ShowActions = false;
    public static boolean OverwriteImages = false;

    public static boolean isShowActions() {
        return ShowActions;
    }

    public static void setShowActions(boolean ShowActions) {
        if (ShowActions) {
            MoveMouseDelaySaved = MoveMouseDelay;
        } else {
            MoveMouseDelay = MoveMouseDelaySaved;
        }
        Settings.ShowActions = ShowActions;
    }

    public static float SlowMotionDelay = 2.0f; // in seconds
    public static float MoveMouseDelay = 0.5f; // in seconds
    private static float MoveMouseDelaySaved = MoveMouseDelay;

    /**
     * true = highlight every match (default: false) (show red rectangle around)
     * for DefaultHighlightTime seconds (default: 2)
     */
    public static boolean Highlight = false;
    public static float DefaultHighlightTime = 2f;
    public static float WaitAfterHighlight = 0.3f;
    public static boolean ActionLogs = true;
    public static boolean InfoLogs = true;
    public static boolean DebugLogs = false;
    public static boolean ProfileLogs = false;
    public static boolean LogTime = false;
    public static boolean UserLogs = true;
    public static String UserLogPrefix = "user";
    public static boolean UserLogTime = true;
    public static boolean TraceLogs = false;
    /**
     * default pixels to add around with nearby() and grow()
     */
    public static final int DefaultPadding = 50;

    public static boolean isJava7() {
        return JavaVersion > 6;
    }

    public static boolean isJava6() {
        return JavaVersion < 7;
    }

//  public static void showJavaInfo() {
//		Debug.log(1, "Running on Java " + JavaVersion + " (" + JREVersion + ")");
//	}

    public static String getFilePathSeperator() {
        return File.separator;
    }

    public static String getPathSeparator() {
        if (isWindows()) {
            return ";";
        }
        return ":";
    }

    public static String getSikuliDataPath() {
        String home, sikuliPath;
        if (isWindows()) {
            home = System.getenv("APPDATA");
            sikuliPath = "Sikulix";
        } else if (isMac()) {
            home = System.getProperty("user.home")
                    + "/Library/Application Support";
            sikuliPath = "Sikulix";
        } else {
            home = System.getProperty("user.home");
            sikuliPath = ".Sikulix";
        }
        File fHome = new File(home, sikuliPath);
        return fHome.getAbsolutePath();
    }

    /**
     * @return absolute path to the user's extension path
     */
    public static String getUserExtPath() {
        String ret = getSikuliDataPath() + File.separator + "Extensions";
        File f = new File(ret);
        if (!f.exists()) {
            f.mkdirs();
        }
        return ret;
    }

    public static int getOS() {
        int osRet;
        String os = System.getProperty("os.name").toLowerCase();
        if (os.startsWith("mac")) {
            osRet = ISMAC;
        } else if (os.startsWith("windows")) {
            osRet = ISWINDOWS;
        } else {
            osRet = ISLINUX;
        }
        return osRet;
    }

    public static boolean isWindows() {
        return getOS() == ISWINDOWS;
    }

    public static boolean isLinux() {
        return getOS() == ISLINUX;
    }

    public static boolean isMac() {
        return getOS() == ISMAC;
    }

    public static boolean isMac10() {
        if (isMac() && Settings.getOSVersion().startsWith("10.1")) {
            return true;
        }
        return false;
    }

    public static String getShortOS() {
        if (isWindows()) {
            return "win";
        }
        if (isMac()) {
            return "mac";
        }
        return "lux";
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static String getTimestamp() {
        return (new Date()).getTime() + "";
    }
}
