set theTitle to "Startseite"
tell application "System Events"
    tell first process of processes whose name is "safari"
        #set frontmost to true
        perform action "AXRaise" of (windows whose title is theTitle)
    end tell
end tell