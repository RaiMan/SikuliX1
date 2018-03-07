popup("hello")
print "********* sys.path"
for e in sys.path: print e
print "********* sys.argv", len(sys.argv)
for e in sys.argv: print "|" + e + "|"
print "********* end sys.argv"
exit(3)
