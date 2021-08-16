tell application "System Events"
  set prog to first process in (processes where frontmost is true)
   count of windows of prog
end tell