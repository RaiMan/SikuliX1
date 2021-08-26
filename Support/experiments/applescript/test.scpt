tell application "System Events"
	set prog to first process whose name is "safari"
	set win to first item of (windows of prog whose title is "Startseite")
	set (size of win) to {500, 500}
	#perform action "AXRaise" of win
	#set frontmost of prog to true
	#windows of prog
end tell
