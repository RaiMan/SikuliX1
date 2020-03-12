reg = Region(106,108,370,160)
img1 = "source_activate.jpg"
img2 = "source_activated.jpg"
button = "buttonactivate.png"

"""
m = find(button)
m.highlight(2)

exit()
"""

ib = Finder(Image.create(button))
ib.find(button)
print "button:", ib.next().getScore()
ib = Finder(Image.create(img1))
ib.find(button)
print "img1:", ib.next().getScore()
ib = Finder(Image.create(img2))
ib.find(button)
print "img2:", ib.next().getScore()
"""

print "button:", Image(button).find(button)
print "img1:", Image(img1).find(button)
print "img2:", Image(img2).find(button)
"""
