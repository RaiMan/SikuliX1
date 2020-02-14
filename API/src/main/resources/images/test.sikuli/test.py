img = "img.png"

img = r"\Users\rmhde\IdeaProjects\SikuliX1\API\src\main\resources\images\test.sikuli\img.png"

simg = SCREEN.cmdCapture(SCREEN)
print "capture:", simg
finder = Finder(simg, Region(0,0,1,1))
finder.find(img)
if finder.hasNext():
  finder.next().highlight(2)