# SikuliX IDE Scripting API Reference

## Introduction

Welcome to the SikuliX IDE Scripting API! This document provides a reference for the common functions and methods you can use to create automation scripts in the SikuliX IDE. Scripts in the IDE are written in Jython (Python implemented in Java), allowing you to leverage both Python's ease of use and the power of the underlying Java-based SikuliX API.

Most of the actions you'll use are available as global functions (e.g., `click()`, `find()`, `type()`). Many of these global functions operate on a default `SCREEN` object, which represents the primary screen. You can also work with specific `Region` objects, `Match` objects (results of find operations), and other SikuliX objects.

**Note on Find Operations:** If a find operation (like `find()`, `wait()`, or `click()` on an image) cannot locate the target image on the screen, it will raise a `FindFailed` exception, which will stop your script unless you handle the exception using `try...except` blocks or by adjusting the failure response behavior (see `setThrowException()` or `setFindFailedResponse()` on Region objects).

## Core Global Functions

These are the most frequently used functions for interacting with GUI elements. Unless otherwise specified, they operate on the default screen.

### Finding Targets

These functions are used to locate images or patterns on the screen.

**`find(image_or_pattern)`**
*   **Description:** Finds the best match for the given target on the current screen. If not found, a `FindFailed` exception is raised.
*   **Parameters:**
    *   `image_or_pattern`: A string (path to an image file) or a `Pattern` object.
*   **Returns:** A `Match` object representing the found item.
*   **Example:**
    ```python
    match = find("start_button.png")
    click(match) # Clicks the found match
    ```

**`findAll(image_or_pattern)`**
*   **Description:** Finds all occurrences of the given target on the current screen.
*   **Parameters:**
    *   `image_or_pattern`: A string (path to an image file) or a `Pattern` object.
*   **Returns:** An iterator of `Match` objects. You can loop through these, e.g., `for m in findAll("icon.png"): click(m)`.
*   **Example:**
    ```python
    for icon in findAll("checkbox.png"):
        click(icon)
    ```

**`wait(target, timeout=None)`**
*   **Description:** Waits until the specified target appears on the screen. If the target does not appear within the timeout period, a `FindFailed` exception is raised.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern` object, or `Image` object. Can also be a number (float or int) representing seconds to simply pause execution (like `sleep()`).
    *   `timeout` (optional): Maximum time in seconds to wait for the target. If `None`, uses the default auto-wait timeout of the current region/screen.
*   **Returns:** A `Match` object.
*   **Example:**
    ```python
    try:
        login_button = wait("login_form.png", 10) # Wait up to 10 seconds
        click(login_button)
    except FindFailed:
        print "Login form did not appear in time."
    
    wait(5) # Pauses the script for 5 seconds
    ```

**`waitVanish(target, timeout=None)`**
*   **Description:** Waits until the specified target disappears from the screen.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern` object, or `Image` object.
    *   `timeout` (optional): Maximum time in seconds to wait for the target to vanish. If `None`, uses the default auto-wait timeout.
*   **Returns:** `True` if the target vanished within the timeout, `False` otherwise.
*   **Example:**
    ```python
    if waitVanish("loading_spinner.png", 30):
        print "Loading complete."
    else:
        print "Loading spinner still visible after 30 seconds."
    ```

**`exists(target, timeout=0)`**
*   **Description:** Checks if the specified target exists on the screen. This function does not raise `FindFailed`.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern` object, or `Image` object.
    *   `timeout` (optional): Time in seconds to keep checking. Default is 0 (check only once). For continuous checking up to a duration, provide a value greater than 0.
*   **Returns:** A `Match` object if the target is found, `None` otherwise.
*   **Example:**
    ```python
    if exists("error_message.png"):
        print "Error message found!"
        # Further actions...
    else:
        print "No error message."
    ```

**`has(target, timeout=0)`** 
*   **Description:** Similar to `exists`, but returns a boolean. Checks if the target appears within the specified time. Does not raise `FindFailed`.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern` object, or `Image` object.
    *   `timeout` (optional): Time in seconds to keep checking. Default is 0 (check only once).
*   **Returns:** `True` if found, `False` otherwise.
*   **Example:**
    ```python
    if has("optional_feature.png", 2): # Check for 2 seconds
        click("optional_feature.png")
    ```

### Mouse Actions

These functions simulate mouse interactions. Targets can be image strings, `Pattern` objects, `Match` objects, `Region` objects, or `Location` objects. If an image/pattern is provided, SikuliX will first find it and then perform the action on the center of the best match (or the `Pattern`'s target offset).

**`click(target, modifiers=0)`**
*   **Description:** Performs a left mouse click on the specified target.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern`, `Match`, `Region`, or `Location`.
    *   `modifiers` (optional): Integer representing key modifiers to hold during the click (e.g., `Key.CTRL`, `Key.SHIFT`). Combine multiple modifiers with `+` (e.g., `Key.CTRL + Key.ALT`).
*   **Returns:** `1` if successful, `0` otherwise (though often scripts rely on `FindFailed` for missing targets).
*   **Example:**
    ```python
    click("button.png")
    click(match_object)
    click(region_object.getCenter()) 
    click("settings.png", Key.SHIFT) # Shift-click
    ```

**`doubleClick(target, modifiers=0)`**
*   **Description:** Performs a left mouse double-click on the target.
*   **Parameters:** Same as `click()`.
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    doubleClick("icon.png")
    ```

**`rightClick(target, modifiers=0)`**
*   **Description:** Performs a right mouse click on the target.
*   **Parameters:** Same as `click()`.
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    rightClick("item_in_list.png")
    ```

**`hover(target)`**
*   **Description:** Moves the mouse cursor to the center of the target.
*   **Parameters:**
    *   `target`: A string (image path), `Pattern`, `Match`, `Region`, or `Location`.
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    hover("menu_item.png") 
    # often followed by another action like click() on a sub-menu item that appears
    ```

**`dragDrop(source_target, destination_target)`**
*   **Description:** Drags an item from the location of the `source_target` and drops it onto the location of the `destination_target`.
*   **Parameters:**
    *   `source_target`: The item to drag (image string, `Pattern`, `Match`, `Region`, or `Location`).
    *   `destination_target`: The location to drop onto (image string, `Pattern`, `Match`, `Region`, or `Location`).
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    dragDrop("file_icon.png", "folder_icon.png")
    ```

**`drag(source_target)`**
*   **Description:** Moves the mouse to the `source_target`, presses and holds the left mouse button.
*   **Parameters:**
    *   `source_target`: The item to start dragging (image string, `Pattern`, `Match`, `Region`, or `Location`).
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    drag("slider_handle.png")
    # Follow with mouseMove() and dropAt() or Mouse.up()
    ```

**`dropAt(destination_target)`**
*   **Description:** Moves the mouse to the `destination_target` and releases the left mouse button (completing a drag operation).
*   **Parameters:**
    *   `destination_target`: The location to drop onto (image string, `Pattern`, `Match`, `Region`, or `Location`).
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    # Assuming a drag() was already performed
    dropAt("target_area.png")
    ```

**`mouseDown(buttons)`**
*   **Description:** Presses and holds the specified mouse button(s). Use `Button.LEFT`, `Button.MIDDLE`, `Button.RIGHT`.
*   **Parameters:**
    *   `buttons`: Integer representing the mouse button(s).
*   **Returns:** None.
*   **Example:**
    ```python
    mouseDown(Button.LEFT) 
    # (usually followed by mouseMove and mouseUp)
    ```

**`mouseUp(buttons=0)`**
*   **Description:** Releases the specified mouse button(s). If no buttons are specified (or 0), releases all currently pressed buttons.
*   **Parameters:**
    *   `buttons` (optional): Integer representing the mouse button(s) to release.
*   **Returns:** None.
*   **Example:**
    ```python
    mouseUp(Button.LEFT)
    mouseUp() # Release any pressed buttons
    ```

**`wheel(direction, steps, stepDelay=Mouse.WHEEL_STEP_DELAY)` (on a Region/Screen) or `Mouse.wheel(direction, steps)` (static)**
*   **Description:** Scrolls the mouse wheel. The global `wheel()` function would need a target or operate at current mouse location if available via `Sikuli.py`'s exposure. More robustly, call on a region: `Screen().wheel(...)` or use `Mouse.wheel(...)`.
*   **Parameters:**
    *   `direction`: `WHEEL_UP` or `WHEEL_DOWN`.
    *   `steps`: Number of steps to scroll.
    *   `stepDelay` (optional): Delay in milliseconds between each wheel step.
*   **Returns:** `1` (for Region method).
*   **Example:**
    ```python
    # Assuming 'myRegion' is a Region object
    myRegion.wheel(WHEEL_DOWN, 5) 
    Mouse.wheel(WHEEL_UP, 3) # Static version, acts at current mouse position
    ```

### Keyboard Actions

These functions simulate keyboard input.

**`type(text, modifiers=0)`**
*   **Description:** Types the given text at the current focus. Can also click a target first if `type(target, text, modifiers)` is used. Special keys are represented by constants from the `Key` class.
*   **Parameters:**
    *   `text`: The string to type. For special keys, use `Key.KEY_NAME` (e.g., `Key.ENTER`, `Key.TAB`). Combine with `+` for chords like `Key.CTRL + "c"`.
    *   `modifiers` (optional): Integer for modifier keys (e.g., `Key.SHIFT`) to hold down for the *entire duration* of typing `text`.
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    type("hello world" + Key.ENTER)
    type("c", Key.CTRL) # Types 'c' while Ctrl is held (like Ctrl-C)
    type(Pattern("text_field.png").similar(0.9), "SikuliX rocks!") # Clicks field, then types
    ```

**`paste(text)`**
*   **Description:** Pastes the given text at the current focus using the system clipboard (Ctrl/Cmd+V). Can also click a target first if `paste(target, text)` is used.
*   **Parameters:**
    *   `text`: The string to paste.
*   **Returns:** `1` if successful, `0` otherwise.
*   **Example:**
    ```python
    paste("This text will be pasted.")
    paste(Pattern("input_area.png"), "Pasting here.")
    ```

**`keyDown(key_constants)`**
*   **Description:** Presses and holds the specified key(s). Use `Key.KEY_NAME` constants. Combine multiple keys with `+`.
*   **Parameters:**
    *   `key_constants`: String containing one or more `Key` constants (e.g., `Key.SHIFT`, `Key.CTRL + Key.ALT`).
*   **Returns:** None.
*   **Example:**
    ```python
    keyDown(Key.SHIFT)
    type("this will be in uppercase")
    keyUp(Key.SHIFT)
    ```

**`keyUp(key_constants=None)`**
*   **Description:** Releases the specified key(s). If no argument is given, releases all currently held keys.
*   **Parameters:**
    *   `key_constants` (optional): String containing `Key` constants to release.
*   **Returns:** None.
*   **Example:**
    ```python
    keyUp(Key.CTRL)
    keyUp() # Release all held keys
    ```

---
This is a starting draft. I will continue adding sections for Objects (Region, Match, Location, Pattern, App), Application/Environment Control, UI Dialogs, Image Path Management, and Settings/Advanced. I'll also need to add the `Key` constants list.

## Working with Objects

While many common operations can be performed using global functions, SikuliX also provides several objects that you can interact with directly. These objects often have their own methods that mirror or extend the global functions.

### Region Objects

A `Region` object represents a rectangular area on a screen. The default `SCREEN` object is a special kind of `Region`. You can define your own regions to limit the scope of searches and actions.

**Creating Regions:**
*   `my_region = Region(x, y, w, h)`: Creates a region with specified top-left coordinates (x,y) and dimensions (width, height).
*   `my_region = Screen().selectRegion("Select an area")`: Allows interactive selection.
*   A `Match` object (returned by `find`, `wait`, `exists`) is also a `Region`.

**Common Methods on Region Objects:**
Most global actions like `click()`, `find()`, `type()`, etc., can be called as methods on a `Region` object. When called on a specific region, the action is confined to that region's boundaries.

*   `region.click(target, modifiers=0)`
*   `region.find(image_or_pattern)`
*   `region.wait(target, timeout=None)`
*   `region.type(text, modifiers=0)`
*   ...and many others.

**Region Manipulation & Information:**
*   `region.setX(x)`, `region.setY(y)`, `region.setW(w)`, `region.setH(h)`: Set coordinates or dimensions.
*   `region.getX()`, `region.getY()`, `region.getW()`, `region.getH()`: Get coordinates or dimensions.
*   `region.getCenter()`: Returns a `Location` object for the center of the region.
*   `region.getTopLeft()`, `region.getTopRight()`, `region.getBottomLeft()`, `region.getBottomRight()`: Return `Location` objects for corners.
*   `region.offset(dx, dy)` or `region.offset(location_obj)`: Returns a new `Region` of the same size, offset by the given amounts or `Location`.
*   `region.grow(amount)` or `region.grow(dw, dh)`: Returns a new, larger `Region`.
*   `region.inside()`: Returns the region itself (can be used for chaining or clarity).
*   `region.contains(another_region_or_location)`: Checks if the region contains another element.
*   `region.right(width)`, `region.left(width)`, `region.above(height)`, `region.below(height)`: Returns a new region adjacent to the current one with the specified dimension. If no dimension is given, extends to the screen border.
*   `region.union(another_region)`, `region.intersection(another_region)`: Returns a new region based on set operations.
*   `region.highlight(seconds, color=None)`: Highlights this specific region.

**Example:**
```python
# Define a specific area of the screen
search_area = Region(100, 200, 300, 400)
search_area.highlight(2) # Highlight this area for 2 seconds

# Find an icon only within this search_area
try:
    icon = search_area.find("specific_icon.png")
    search_area.click(icon) # Click within the context of search_area
except FindFailed:
    print "Icon not found in the specified search area."

# Get the center of the region
center_loc = search_area.getCenter()
hover(center_loc)
```

### Match Objects

A `Match` object is returned by find operations (`find`, `wait`, `exists`). It represents the area where a target image was found. Importantly, a `Match` object *is also a `Region`*, so it inherits all `Region` methods.

**Key `Match` Methods:**
*   `match.getScore()`: Returns the similarity score of the match (a float between 0.0 and 1.0).
*   `match.getTarget()`: Returns a `Location` object representing the click point for this match. This can be different from the center if the `Pattern` used for finding had a target offset. All actions on the `match` (e.g., `match.click()`) use this target.
*   `match.setTargetOffset(dx, dy)`: Sets a new target offset for this match, relative to its center.
*   `match.getImage()`: Returns the `Image` object that was used to find this match.
*   `match.getText()`: If the match was found using OCR (e.g., `findText`), this returns the recognized text.

**Example:**
```python
button_match = wait("submit_button.png", 5)
print "Found button with score:", button_match.getScore()

# Click the button using its default target
button_match.click()

# Or, click slightly above the center of the match
offset_target = button_match.getTarget().offset(0, -10) # 10 pixels up
click(offset_target)

# Alternative way to click with offset from match center
button_match.setTargetOffset(0, -10)
button_match.click() 
```

### Location Objects

A `Location` object represents a specific point (x,y) on a screen.

**Creating Locations:**
*   `loc = Location(x, y)`
*   `loc = Mouse.at()` (or global `at()`): Gets the current mouse cursor position.
*   `loc = region_or_match.getCenter()`
*   `loc = region_or_match.getTarget()`

**Methods on Location Objects:**
*   `loc.getX()`, `loc.getY()`: Get coordinates.
*   `loc.setX(x)`, `loc.setY(y)`: Set coordinates.
*   `loc.offset(dx, dy)`: Returns a new `Location` offset from this one.
*   `loc.left(dx)`, `loc.right(dx)`, `loc.above(dy)`, `loc.below(dy)`: Returns new `Location`s offset in cardinal directions.
*   `loc.click()`: Performs a left click at this location.
*   `loc.doubleClick()`: Performs a double click at this location.
*   `loc.rightClick()`: Performs a right click at this location.
*   `loc.hover()`: Moves the mouse to this location.
*   `loc.getColor()`: Returns a `java.awt.Color` object for the pixel at this location.

**Example:**
```python
# Get current mouse position
current_pos = Mouse.at()
print "Mouse is at:", current_pos.getX(), current_pos.getY()

# Define a new location and click it
target_spot = Location(500, 300)
target_spot.hover()
target_spot.click()

# Get color of a pixel
color_at_point = Location(10, 20).getColor()
print "Red component:", color_at_point.getRed()
```

### Pattern Objects

A `Pattern` object is used to specify an image for searching, along with additional properties like similarity and target offset.

**Creating Patterns:**
*   `pat = Pattern("image_filename.png")`

**Key `Pattern` Methods:**
*   `pattern.similar(similarity_score)`: Sets the minimum similarity for a match (0.0 to 1.0). Default is usually around 0.7.
    *   Example: `Pattern("icon.png").similar(0.9)` (requires a 90% match).
*   `pattern.targetOffset(dx, dy)`: Sets the click point for matches found with this pattern, relative to the center of the image. `dx` and `dy` are pixel offsets.
    *   Example: `Pattern("button.png").targetOffset(0, 10)` (clicks 10 pixels below the center).
*   `pattern.exact()`: Sets similarity to 0.99 (very strict).
*   `pattern.resize(factor)`: Resizes the image for searching by a factor (e.g., 0.5 for half size, 2.0 for double size). *This does not change the original image file.*
*   `pattern.rotate(degrees)`: Rotates the image for searching. *This does not change the original image file.*

**Example:**
```python
# Find an image with high similarity and a specific click point
close_button_pattern = Pattern("close_X.png").similar(0.95).targetOffset(2, -3)
click(close_button_pattern)

# Find an image that might be slightly smaller
logo_pattern = Pattern("logo_on_page.png").resize(0.8)
if exists(logo_pattern):
    print "Found a logo, possibly scaled."
```

---
This section covers `Region`, `Match`, `Location`, and `Pattern` objects. The next part of "Working with Objects" would be `App` objects, but that might be better placed or duplicated in the "Application and Environment Control" section for thematic grouping. I'll proceed with adding the remaining top-level sections based on the plan.

## Application and Environment Control

These functions and classes help you manage applications, interact with the system environment, and control your script's execution.

### Application Management (`App` class and global functions)

SikuliX can launch, switch to, and close applications. You can work with `App` objects or use global convenience functions.

**Creating/Getting `App` Objects:**
*   `myApp = App("AppNameOrPath")`: Creates an `App` object. "AppNameOrPath" can be the application's name (e.g., "Firefox", "TextEdit"), part of its executable name, or the full path to the executable.
*   `myApp = openApp("AppNameOrPath")`: Opens or focuses the app and returns an `App` object.
*   `myApp = App.focus("Window Title")`: Focuses an app by its window title and returns an `App` object.

**Key `App` Object Methods:**
*   `app.focus()`: Brings the application's main window to the foreground.
*   `app.close()`: Attempts to close the application.
*   `app.open()`: Opens the application if not already running, then focuses it.
*   `app.window(windowIndex=0)`: Returns a `Region` object representing the application's main window (or a specific window by index).
*   `app.getPID()`, `app.getName()`, `app.getExecutable()`: Get process information.
*   `app.isRunning()`: Checks if the application is currently running.
*   `app.getTitle(windowIndex=0)`: Gets the title of the specified window.
*   `app.minimize()`, `app.maximize()`, `app.restore()`: Control window state.

**Global Application Functions:**
*   `openApp(appName)`: Opens or focuses the specified application. Returns an `App` object.
    ```python
    firefox = openApp("firefox") # Opens Firefox or focuses it if already running
    if firefox.isRunning():
        print "Firefox is now focused or was opened."
    ```
*   `switchApp(appName)`: Same as `openApp()`. Switches to the app, launching if necessary.
*   `closeApp(appName)`: Closes the specified application. Returns `True` if successful.
    ```python
    closeApp("notepad.exe")
    ```
*   `App.focusedWindow()`: Returns a `Region` object for the window that currently has focus on the system.
    ```python
    active_window = App.focusedWindow()
    active_window.highlight(1)
    ```

### System Interaction

*   `App.run(command_string)`: Executes a system command.
    *   **Returns:** The return code of the command (integer).
    *   **Note:** Output (stdout, stderr) is available via `App.lastRunStdout` and `App.lastRunStderr` after execution.
    ```python
    return_code = App.run("ls -l /tmp")
    if return_code == 0:
        print "Stdout:"
        print App.lastRunStdout
    else:
        print "Command failed. Stderr:"
        print App.lastRunStderr
    ```
*   `App.getClipboard()`: Gets the current text content from the system clipboard.
    *   **Returns:** String.
    ```python
    clipboard_text = App.getClipboard()
    print "Clipboard contains:", clipboard_text
    ```
*   `App.setClipboard(text)`: Sets the system clipboard to the given text.
    *   **Parameters:**
        *   `text`: The string to put on the clipboard.
    ```python
    App.setClipboard("Text to be copied by SikuliX!")
    ```

### Pausing & Exiting Script

*   `sleep(seconds)`: Pauses the script for the specified number of seconds. Can be a float for sub-second precision.
    ```python
    print "Waiting for 5 seconds..."
    sleep(5) 
    print "Done waiting."
    ```
*   `exit(return_code=0)`: Stops the script immediately and returns the given `return_code` to the operating system or calling process.
    ```python
    if not exists("critical_element.png"):
        print "Critical element not found, exiting script."
        exit(1) 
    ```

## User Interaction Dialogs

SikuliX provides functions to display simple dialogs for user interaction.

**`popup(message, title="Sikuli Info")`**
*   **Description:** Shows an informational message dialog with an "OK" button.
*   **Parameters:**
    *   `message`: The string to display.
    *   `title` (optional): The title for the dialog window.
*   **Example:**
    ```python
    popup("The script has completed successfully!")
    ```

**`popError(message, title="Sikuli Error")`**
*   **Description:** Shows an error message dialog (usually with an error icon) and an "OK" button.
*   **Parameters:** Same as `popup()`.
*   **Example:**
    ```python
    popError("Failed to connect to the server.")
    ```

**`popAsk(message, title="Sikuli Decision")`**
*   **Description:** Shows a dialog with the message and "Yes" and "No" buttons.
*   **Parameters:** Same as `popup()`.
*   **Returns:** `True` if the user clicks "Yes", `False` if "No" or closes the dialog.
*   **Example:**
    ```python
    if popAsk("Do you want to proceed with the update?"):
        print "User chose to proceed."
    else:
        print "User declined or closed the dialog."
    ```

**`input(message="", default_text="", title="Sikuli Input", hidden=False)`**
*   **Description:** Shows a dialog requesting text input from the user.
*   **Parameters:**
    *   `message` (optional): The message to display.
    *   `default_text` (optional): Pre-filled text in the input field.
    *   `title` (optional): The title for the dialog window.
    *   `hidden` (optional): If `True`, masks the input with bullets (for passwords). Default is `False`.
*   **Returns:** The string entered by the user, or `None` if the user cancels.
*   **Example:**
    ```python
    username = input("Please enter your username:")
    if username:
        print "Username entered:", username
    
    password = input("Enter password:", hidden=True)
    # Process password...
    ```

**`inputText(message="", title="", lines=0, width=0, initial_text="")`**
*   **Description:** Shows a dialog with a multi-line text area for input. Useful for pasting larger blocks of text.
*   **Parameters:**
    *   `message` (optional): The message to display.
    *   `title` (optional): The title for the dialog window.
    *   `lines` (optional): Preferred number of visible lines in the text area (default auto-sizes).
    *   `width` (optional): Preferred width in characters for the text area (default auto-sizes).
    *   `initial_text` (optional): Pre-filled text in the text area.
*   **Returns:** The string entered by the user, including line breaks.
*   **Example:**
    ```python
    notes = inputText("Enter any notes for this session:", lines=5, width=40)
    if notes:
        print "User notes:
", notes
    ```

**`select(message="", title="Sikuli Selection", options=(), default_selection=None)`**
*   **Description:** Shows a dialog with a dropdown list for the user to select an option.
*   **Parameters:**
    *   `message` (optional): The message to display.
    *   `title` (optional): The title for the dialog window.
    *   `options`: A tuple or list of strings representing the choices in the dropdown.
    *   `default_selection` (optional): The option to be pre-selected. Can be the string itself or its index in the `options` list.
*   **Returns:** The string of the selected option, or `None` if canceled.
*   **Example:**
    ```python
    choices = ("Option A", "Option B", "Option C")
    chosen = select("Choose your preference:", options=choices, default_selection=choices[0])
    if chosen:
        print "User selected:", chosen
    ```

**`popFile(title="Select File or Folder")`**
*   **Description:** Shows a standard operating system dialog for selecting a file or folder.
*   **Parameters:**
    *   `title` (optional): The title for the dialog window.
*   **Returns:** A string containing the absolute path to the selected file or folder, or `None` if canceled.
*   **Example:**
    ```python
    filePath = popFile("Please select the data file")
    if filePath:
        print "Selected file:", filePath
    ```

---
This covers Application/Environment Control and User Interaction Dialogs. The next sections will be Image Path Management and Settings/Advanced.

## Image Path Management

SikuliX scripts often rely on images. These functions help you manage where SikuliX looks for these images. Typically, SikuliX automatically looks for images in the same folder as your `.sikuli` script (the bundle path).

**`addImagePath(path)`**
*   **Description:** Adds a new path to the list of locations where SikuliX will search for images.
*   **Parameters:**
    *   `path`: A string representing the absolute or relative path to a directory containing images.
*   **Returns:** `True` if the path is valid and added, `False` otherwise.
*   **Example:**
    ```python
    # Add a subfolder named "icons" inside the script's bundle path
    addImagePath(getBundlePath() + "/icons") 
    # Add a globally accessible image library
    addImagePath("/Users/Shared/SikuliX_Images") 
    ```

**`removeImagePath(path)`**
*   **Description:** Removes a path from the image search list.
*   **Parameters:**
    *   `path`: The string path to remove.
*   **Returns:** `True` if the path was found and removed, `False` otherwise.
*   **Example:**
    ```python
    removeImagePath("/Users/Shared/SikuliX_Images")
    ```

**`getImagePath()`**
*   **Description:** Returns the current list of paths where SikuliX searches for images.
*   **Returns:** A list of strings, where each string is a path.
*   **Example:**
    ```python
    current_paths = getImagePath()
    for p in current_paths:
        print "Image search path:", p
    ```

**`setBundlePath(path)`**
*   **Description:** Sets the primary path for image searching (the "bundle path"). By default, this is the path to your `.sikuli` script folder. Changing this can be useful if your images are located elsewhere relative to your main script logic.
*   **Parameters:**
    *   `path`: The new bundle path string.
*   **Returns:** `True` if the path is valid and set, `False` otherwise.
*   **Example:**
    ```python
    # If your images are in a specific project folder
    setBundlePath("/path/to/my_project_images")
    ```

**`getBundlePath()`**
*   **Description:** Returns the absolute path to the current script's bundle (the `.sikuli` folder).
*   **Returns:** A string.
*   **Example:**
    ```python
    print "This script's bundle path is:", getBundlePath()
    ```

## Settings and Advanced Topics

### Settings

**`setShowActions(boolean_flag)`**
*   **Description:** Controls whether SikuliX visually highlights actions (like `click`, `find`) on the screen as they happen. This can be useful for debugging. Default is `False`.
*   **Parameters:**
    *   `boolean_flag`: `True` to enable visual highlighting, `False` to disable.
*   **Example:**
    ```python
    setShowActions(True) # Highlights will be shown
    click("some_button.png")
    setShowActions(False) # Turn off highlights
    ```

**`Settings.AutoWaitTimeout` (Attribute)**
*   **Description:** The default time in seconds that `wait()` and other find operations will wait for a target to appear if no explicit timeout is specified. Default is 3.0 seconds.
*   **Usage:** Can be read or set directly.
*   **Example:**
    ```python
    print "Current AutoWaitTimeout:", Settings.AutoWaitTimeout
    Settings.AutoWaitTimeout = 5.0 # Set to 5 seconds
    wait("image_that_takes_time.png") # Will use the new 5-second timeout
    ```

**`Settings.ClickDelay` (Attribute)**
*   **Description:** A delay in seconds (can be fractional) introduced between the mouse down and mouse up events of a click. Default is 0.0. You can set this to a small value (e.g., 0.1 or 0.2) if clicks are not registering properly in some applications. This affects subsequent `click`, `doubleClick`, `rightClick` actions.
*   **Usage:** Can be set directly.
*   **Example:**
    ```python
    Settings.ClickDelay = 0.2 # Introduce a 0.2 second delay within clicks
    click("sensitive_button.png")
    Settings.ClickDelay = 0.0 # Reset to default
    ```
    *   **Note:** `Region.delayClick(milliseconds)` can set this for the *next* click only.

### Key Constants (`Key` class)

When using keyboard functions like `type()`, `keyDown()`, or `keyUp()`, special keys are represented by constants available in the `Key` class.

**Common Key Constants:**
*   `Key.ENTER`
*   `Key.TAB`
*   `Key.ESC` (Escape key)
*   `Key.BACKSPACE`
*   `Key.DELETE`
*   `Key.UP`, `Key.DOWN`, `Key.LEFT`, `Key.RIGHT` (Arrow keys)
*   `Key.HOME`, `Key.END`
*   `Key.PAGE_UP`, `Key.PAGE_DOWN`
*   `Key.INSERT`
*   `Key.F1`, `Key.F2`, ... `Key.F15`
*   `Key.CAPS_LOCK`, `Key.NUM_LOCK`, `Key.SCROLL_LOCK`
*   `Key.PRINTSCREEN`, `Key.PAUSE`

**Modifier Keys:**
These are used with functions that accept `modifiers` (like `click()`, `type()`) or can be combined in `type()` strings (e.g., `Key.CTRL + "a"`).
*   `Key.SHIFT`
*   `Key.CTRL` (Control key)
*   `Key.ALT`
*   `Key.META` (Command key on macOS, Windows key on Windows)
*   `Key.CMD` (Alias for `Key.META`, primarily for macOS users)
*   `Key.WIN` (Alias for `Key.META`, primarily for Windows users)
*   `Key.ALTGR` (AltGr key, if present)

**Using Modifier Keys in `type()`:**
To type a combination like Ctrl+A:
```python
type("a", Key.CTRL)
```
To type a sequence like Shift then 'h' then 'e' then 'l' then 'l' then 'o':
```python
keyDown(Key.SHIFT)
type("hello")
keyUp(Key.SHIFT)
# Or for a single modified character: type("H") if shift is not already held.
```

### Mouse Pointer Location

**`Mouse.at()` or global `at()`**
*   **Description:** Gets the current location of the mouse pointer.
*   **Returns:** A `Location` object.
*   **Example:**
    ```python
    current_mouse_pos = Mouse.at()
    print "Mouse is at: X =", current_mouse_pos.getX(), "Y =", current_mouse_pos.getY()
    hover(Mouse.at().offset(0, 20)) # Hover 20 pixels below current mouse position
    ```

### Advanced: Observers (Brief Mention)

SikuliX allows you to react to events like an image appearing, disappearing, or a region changing. This is done using observers.

*   `onAppear(target, handler_function)`: When `target` appears, call `handler_function`.
*   `onVanish(target, handler_function)`: When `target` vanishes, call `handler_function`.
*   `onChange(handler_function)` or `onChange(minChangedPixels, handler_function)`: When a change of at least `minChangedPixels` occurs in a region, call `handler_function`.
*   `region.observe(seconds)`: Starts observing on a specific region for a duration.
*   `region.stopObserver()`: Stops the observer.

This is an advanced topic; refer to the official SikuliX documentation for detailed usage.

```
