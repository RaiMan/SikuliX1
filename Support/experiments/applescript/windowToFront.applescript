set theTitle to "some title"
tell application "System Events"
    tell process "appIT"
        set frontmost to true
        perform action "AXRaise" of (windows whose title is theTitle)
    end tell
end tell