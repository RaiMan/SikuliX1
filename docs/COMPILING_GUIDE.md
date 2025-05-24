# How to Compile SikuliX from Source

## Introduction

This guide provides instructions for developers and advanced users who wish to compile SikuliX from its source code. SikuliX is a Maven-based project, so Apache Maven is used for the build process.

Compiling from source allows you to access the latest unreleased changes, contribute to development, or create custom builds.

## Prerequisites

Before you begin, ensure you have the following software installed and configured:

### 1. Java Development Kit (JDK)

*   **Version:** JDK 11 or later. SikuliX is built using Java 11. Using JDK 11 is recommended for compilation, though later LTS versions like JDK 17 or 21 might also work.
*   **Recommended Sources:**
    *   [Adoptium Temurin](https://adoptium.net/) (Eclipse Foundation)
    *   [Azul Zulu OpenJDK](https://www.azul.com/downloads/?package=jdk)
    *   [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)
*   **Configuration:**
    *   Set the `JAVA_HOME` environment variable to your JDK installation directory.
    *   Ensure `\$JAVA_HOME/bin` (or `%JAVA_HOME%\bin` on Windows) is included in your system's `PATH` environment variable.

### 2. Apache Maven

*   **What it is:** A build automation and project management tool.
*   **Version:** Apache Maven 3.6.x or later is recommended.
*   **Download:** [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi)
*   **Configuration:**
    *   Download and extract Maven.
    *   Add the `bin` directory of your Maven installation to your system's `PATH` environment variable.

### 3. Git

*   **What it is:** A version control system required to download (clone) the SikuliX source code.
*   **Download:** [https://git-scm.com/downloads](https://git-scm.com/downloads)
*   **Configuration:** The installer usually adds Git to your system's `PATH`.

## Getting the Source Code

1.  **Clone the Repository:**
    Open your terminal or command prompt and run the following command to download the SikuliX source code:
    ```bash
    git clone https://github.com/RaiMan/SikuliX1.git
    ```
    This will create a new directory named `SikuliX1` containing the project files.

2.  **Navigate into the Directory:**
    ```bash
    cd SikuliX1
    ```

3.  **Check Out the Correct Branch:**
    The `master` branch may contain experimental or unstable code. It's crucial to switch to the active development or a stable release branch. As of this writing, `release_2.0.x` is the active development branch for upcoming 2.0.x versions.
    ```bash
    git checkout release_2.0.x
    ```
    For specific released versions, you can check out a tag (e.g., `git checkout tags/2.0.5`). Always refer to the official SikuliX GitHub page for the latest information on active branches.

## Project Structure Overview

SikuliX is a multi-module Maven project. The main modules you'll interact with are:
*   **`API`**: Contains the core SikuliX Java API. The output is `sikulixapi.jar`.
*   **`IDE`**: Contains the SikuliX Integrated Development Environment. The output is typically a platform-specific runnable JAR (e.g., `sikulixwin-...jar`, `sikulixmac-...jar`).
*   The parent `pom.xml` in the root `SikuliX1` directory manages the build for these modules.

## Building SikuliX

All Maven commands should be run from the root `SikuliX1` directory.

### General Maven Commands

*   **Clean Previous Builds (Recommended):**
    Before starting a new build, especially if switching branches or profiles, it's good practice to clean previous build artifacts:
    ```bash
    mvn clean
    ```
    This deletes the `target` directories in all modules.

*   **Compile All Modules (Validates Code):**
    ```bash
    mvn compile
    ```
    This compiles the source code for all modules but doesn't package them into JARs yet.

*   **Package All Modules (Create JARs):**
    This command compiles and packages all modules into their respective JAR files.
    ```bash
    mvn package
    ```

*   **Install All Modules (to Local Maven Repository):**
    This command compiles, packages, and then installs the artifacts (JARs, POMs) into your local Maven repository (usually `~/.m2/repository/`). This is useful if you plan to use the SikuliX JARs as dependencies in other local Maven projects.
    ```bash
    mvn install
    ```

### Building the API (`sikulixapi.jar`)

If you only need the SikuliX API library (e.g., for use in a separate Java project):
```bash
mvn -pl API package
```
Or, to also install it to your local Maven repository:
```bash
mvn -pl API install
```
*   **Artifact Location:** `SikuliX1/API/target/sikulixapi-<version>.jar`
    *(Replace `<version>` with the actual version, e.g., `2.0.6-SNAPSHOT`)*

### Building the IDE

The SikuliX IDE is typically distributed as platform-specific runnable JARs. These are built using Maven profiles.

*   **Generic IDE JAR (Thin JAR - Usually Not for Direct Use):**
    Running `mvn -pl IDE package` without a specific profile might produce a "thin" JAR in `SikuliX1/IDE/target/sikulixide-<version>.jar`. However, for running the IDE, use the platform-specific builds below.

*   **Building Platform-Specific IDE JARs (Recommended):**
    These commands use Maven profiles (`-P <profile-name>`) to build the complete, runnable IDE for your target operating system.

    *   **For Windows:**
        ```bash
        mvn -pl IDE -P complete-win-jar package
        ```
        Produces: `SikuliX1/IDE/target/sikulixide-<version>-complete-win.jar`

    *   **For macOS:**
        ```bash
        mvn -pl IDE -P complete-mac-jar package
        ```
        Produces: `SikuliX1/IDE/target/sikulixide-<version>-complete-mac.jar`
        *(Note: For Apple Silicon (M1/M2), this build should be tested. Official downloads may provide a specifically compiled `sikulixidemacm` JAR.)*

    *   **For Linux:**
        ```bash
        mvn -pl IDE -P complete-lux-jar package
        ```
        Produces: `SikuliX1/IDE/target/sikulixide-<version>-complete-lux.jar`

## Understanding Build Artifacts

All build outputs are placed in the `target/` directory within each module's folder (e.g., `API/target/`, `IDE/target/`).

*   **`API/target/sikulixapi-<version>.jar`**: The core API library.
*   **`IDE/target/sikulixide-<version>-complete-win.jar`**: Runnable IDE for Windows.
    *(Often distributed as `sikulixwin-<version>.jar`)*
*   **`IDE/target/sikulixide-<version>-complete-mac.jar`**: Runnable IDE for macOS.
    *(Often distributed as `sikulixmac-<version>.jar`)*
*   **`IDE/target/sikulixide-<version>-complete-lux.jar`**: Runnable IDE for Linux.
    *(Often distributed as `sikulixlux-<version>.jar`)*

Replace `<version>` with the actual version string found in the `pom.xml` of the branch you compiled (e.g., `2.0.6-SNAPSHOT`). You may also find `-sources.jar` and `-javadoc.jar` files, which contain the source code and API documentation, respectively.

## Running a Locally Built IDE

Once you have built a platform-specific IDE JAR, you can run it using Java:
```bash
# Example for Windows (replace <version> appropriately)
java -jar IDE/target/sikulixide-<version>-complete-win.jar 

# Example for macOS
# java -jar IDE/target/sikulixide-<version>-complete-mac.jar

# Example for Linux
# java -jar IDE/target/sikulixide-<version>-complete-lux.jar 
```
Make sure you use the actual JAR file name generated in your `IDE/target/` directory.

## Troubleshooting (Brief)

*   **JDK Version Issues:** Ensure `JAVA_HOME` points to JDK 11 or later and it's correctly picked up by Maven (`mvn -version` shows Java version).
*   **Maven Not Found:** Verify Maven's `bin` directory is in your system `PATH`.
*   **Native Dependencies (Linux):** For running the IDE on Linux, you'll need OpenCV and Tesseract installed separately. This guide focuses on compilation; refer to the main SikuliX documentation for runtime native library setup on Linux.
*   **Network Issues:** Maven downloads many dependencies. A stable internet connection is required during the first build. If you encounter download errors, try running the command again.

Happy Compiling!
```
