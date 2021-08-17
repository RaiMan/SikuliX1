tell application "System Events"
  set prog to first process whose name is "safari"
  set win to item 1 of windows of prog
  value of win
  #perform action "AXRaise" of win
  #set frontmost of prog to true
  #windows of prog
end tell