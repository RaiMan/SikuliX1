#!/usr/bin/env python
#TODO to be implemneted in Java (-> FileManager.java)
import sys, cgi
import glob
import re
import keyword, token, tokenize
import string, cStringIO



SIKULI_KEYWORDS = [
      "find", "wait",
      "click", "clickAll", "repeatClickAll", "doubleClick",
      "doubleClickAll", "repeatDoubleClickAll", "rightClick",
      "dragDrop", "type", "sleep", "popup", "capture", "input",
      "assertExist", "assertNotExist" ]


HEADER = """
<html>
   <head>
      <style type="text/css">
         .sikuli-code {
            font-size: 20px;
            font-family: "Osaka-mono", Monospace;
            line-height: 1.5em;
            display:table-cell;
            white-space: pre-wrap;       /* css-3 */
            white-space: -moz-pre-wrap !important;  /* Mozilla, since 1999 */
            white-space: -pre-wrap;      /* Opera 4-6 */
            white-space: -o-pre-wrap;    /* Opera 7 */
            word-wrap: break-word;       /* Internet Explorer 5.5+ */
            width: 99%;   /* remove horizontal scroll-bar when viewing in IE7 */
         }
         .sikuli-code img {
            vertical-align: middle;
            margin: 2px;
            border: 1px solid #ccc;
            padding: 2px;
            -moz-border-radius: 5px;
            -webkit-border-radius: 5px;
            -moz-box-shadow: 1px 1px 1px gray;
            -webkit-box-shadow: 1px 1px 2px gray;
         }
         .kw {
            color: blue;
         }
         .skw {
            color: rgb(63, 127, 127);
         }

         .str {
            color: rgb(128, 0, 0);
         }

         .dig {
            color: rgb(128, 64, 0);
         }

         .cmt {
            color: rgb(200, 0, 200);
         }

         h2 {
            display: inline;
            font-weight: normal;
         }

         .info {
            border-bottom: 1px solid #ddd;
            padding-bottom: 5px;
            margin-bottom: 20px;
            $HIDE_INFO
         }

         a {
            color: #9D2900;
         }

         body {
            font-family: "Trebuchet MS", Arial, Sans-Serif;
         }

      </style>
   </head>
<body>
<div class="info">
<h2>$FILE.sikuli</h2> <a href="$FILE.zip">(Download this script)</a>
</div>
<pre class="sikuli-code">
"""

FOOTER = """
</pre>
</body>
</html>
"""

_KEYWORD = token.NT_OFFSET + 1
_SIKULI_KEYWORD = token.NT_OFFSET + 3

_colors = {
      token.NUMBER:       'dig',
      token.STRING:       'str',
      tokenize.COMMENT:   'cmt',
      _KEYWORD:           'kw',
      _SIKULI_KEYWORD:    'skw',
}

if locals().has_key('local_convert'):
   LOCAL_CONVERT = True
else:
   LOCAL_CONVERT = False

class Parser:

    def __init__(self, raw, out = sys.stdout):
        self.raw = string.strip(string.expandtabs(raw))
        self.out = out

    def format(self, filename):
        global HEADER
        # store line offsets in self.lines
        self.lines = [0, 0]
        pos = 0
        while 1:
           pos = string.find(self.raw, '\n', pos) + 1
           if not pos: break
           self.lines.append(pos)
        self.lines.append(len(self.raw))

        # parse the source and write it
        self.pos = 0
        text = cStringIO.StringIO(self.raw)
        HEADER = HEADER.replace("$FILE", filename)
        if LOCAL_CONVERT:
           HEADER = HEADER.replace("$HIDE_INFO", "display: none;")
        self.out.write(HEADER)
        try:
           tokenize.tokenize(text.readline, self)
        except tokenize.TokenError, ex:
           msg = ex[0]
           line = ex[1][0]
           self.out.write("<h3>ERROR: %s</h3>%s\n" % (
              msg, self.raw[self.lines[line]:]))
           self.out.write('</font></pre>')

        self.out.write(FOOTER)

    def __call__(self, toktype, toktext, (srow,scol), (erow,ecol), line):
        if 0:
           print "type", toktype, token.tok_name[toktype], "text", toktext,
           print "start", srow,scol, "end", erow,ecol, "<br>"

        # calculate new positions
        oldpos = self.pos
        newpos = self.lines[srow] + scol
        self.pos = newpos + len(toktext)

        # handle newlines
        if toktype in [token.NEWLINE, tokenize.NL]:
           self.out.write('\n')
           return

        # send the original whitespace, if needed
        if newpos > oldpos:
           self.out.write(self.raw[oldpos:newpos])

        # skip indenting tokens
        if toktype in [token.INDENT, token.DEDENT]:
           self.pos = newpos
           return

        # map token type to a color group
        if token.LPAR <= toktype and toktype <= token.OP:
           toktype = token.OP
        elif toktype == token.NAME and keyword.iskeyword(toktext):
           toktype = _KEYWORD
        elif toktype == token.NAME and toktext in SIKULI_KEYWORDS:
           toktype = _SIKULI_KEYWORD
        color = ''
        if toktype in _colors:
           color = _colors.get(toktype)

        if toktype == token.STRING and toktext.endswith(".png\""):
           self.out.write('<img src=' + toktext + ' />')
           return

        if color:
           self.out.write('<span class="%s">' % (color))
           self.out.write(cgi.escape(toktext))
           self.out.write('</span>')
        else:
           self.out.write(cgi.escape(toktext))


srcs = []
if locals().has_key('sikuli_src'):
   srcs.append(sikuli_src)
#if len(sys.argv) > 1:
#   srcs += sys.argv[1:]

for sikuli in srcs:
   f_py = glob.glob(sikuli + "/*.py")
   for py in f_py:
      src = open(py, "r")
      dest = open(py.replace(".py", ".html"), "w")
      filename = re.search(r'/([^/]*)\.py', f_py[0])
      if filename:
         filename = filename.group(1)
      else:
         filename = re.search(r'\\([^\\]*)\.py', f_py[0]).group(1)

      Parser(src.read(), dest).format(filename)
      dest.close()
      src.close()

