/*
 * Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.ParagraphView;
import javax.swing.text.html.*;

import org.sikuli.basics.Debug;
//
// A simple Java Console for your application (Swing version)
// Requires Java 1.1.5 or higher
//
// Disclaimer the use of this source is at your own risk.
//
// Permision to use and distribute into your own applications
//
// RJHM van den Bergh , rvdb@comweb.nl
import org.sikuli.basics.PreferencesUser;
import org.sikuli.support.Commons;
import org.sikuli.util.CommandArgsEnum;

public class EditorConsolePane extends JPanel implements Runnable {

  private static final String me = "EditorConsolePane: ";
  //static boolean ENABLE_IO_REDIRECT = true;

  private int NUM_PIPES;
  private JTextPane textArea;
  private Thread[] reader;
  private boolean quit;
  private PipedInputStream[] pin;
  private JPopupMenu popup;
  Thread errorThrower; // just for testing (Throws an Exception at this Console)

  class PopupListener extends MouseAdapter {
    JPopupMenu popup;

    PopupListener(JPopupMenu popupMenu) {
      popup = popupMenu;
    }

    public void mousePressed(MouseEvent e) {
      maybeShowPopup(e);
    }

    public void mouseReleased(MouseEvent e) {
      maybeShowPopup(e);
    }

    private void maybeShowPopup(MouseEvent e) {
      if (e.isPopupTrigger()) {
        popup.show(e.getComponent(), e.getX(), e.getY());
      }
    }
  }

  public EditorConsolePane() {
    super();
  }

  public void init(boolean SHOULD_WRAP_LINE) {
    textArea = new JTextPane();
    HTMLEditorKit kit;
    if (SHOULD_WRAP_LINE) {
      kit = editorKitWithLineWrap();
    } else {
      kit = new HTMLEditorKit();
    }
    textArea.setEditorKit(kit);
    textArea.setTransferHandler(new JTextPaneHTMLTransferHandler());
    textArea.setEditable(false);
    setLayout(new BorderLayout());
    add(new JScrollPane(textArea), BorderLayout.CENTER);

    //Create the popup menu.
    popup = new JPopupMenu();
    JMenuItem menuItem = new JMenuItem("Clear messages");
    // Add ActionListener that clears the textArea
    menuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        textArea.setText("");
      }
    });
    popup.add(menuItem);

    //Add listener to components that can bring up popup menus.
    MouseListener popupListener = new PopupListener(popup);
    textArea.addMouseListener(popupListener);
  }

  private HTMLEditorKit editorKitWithLineWrap() {
    HTMLEditorKit kit = new HTMLEditorKit() {
      @Override
      public ViewFactory getViewFactory() {
        return new HTMLFactory() {
          public View create(Element e) {
            View v = super.create(e);
            if (v instanceof InlineView) {
              return new InlineView(e) {
                public int getBreakWeight(int axis, float pos, float len) {
                  return GoodBreakWeight;
                }

                public View breakView(int axis, int p0, float pos, float len) {
                  if (axis == View.X_AXIS) {
                    checkPainter();
                    int p1 = getGlyphPainter().getBoundedPosition(this, p0, pos, len);
                    if (p0 == getStartOffset() && p1 == getEndOffset()) {
                      return this;
                    }
                    return createFragment(p0, p1);
                  }
                  return this;
                }
              };
            } else if (v instanceof ParagraphView) {
              return new ParagraphView(e) {
                protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
                  if (r == null) {
                    r = new SizeRequirements();
                  }
                  float pref = layoutPool.getPreferredSpan(axis);
                  float min = layoutPool.getMinimumSpan(axis);
                  // Don't include insets, Box.getXXXSpan will include them.
                  r.minimum = (int) min;
                  r.preferred = Math.max(r.minimum, (int) pref);
                  r.maximum = Integer.MAX_VALUE;
                  r.alignment = 0.5f;
                  return r;
                }
              };
            }
            return v;
          }
        };
      }
    };
    return kit;
  }

  public void initRedirect() {
    Debug.log(3, "EditorConsolePane: starting redirection to message area");
    int npipes = 2;
    NUM_PIPES = npipes; //npipes * Runner.getRunners().size() + npipes;
    pin = new PipedInputStream[NUM_PIPES];
    reader = new Thread[NUM_PIPES];
    for (int i = 0; i < NUM_PIPES; i++) {
      pin[i] = new PipedInputStream();
    }

    try {
/*
      int irunner = 1;
      for (IRunner srunner : Runner.getRunners()) {
        Debug.log(3, "EditorConsolePane: redirection for %s", srunner.getName());

        PipedOutputStream pout = new PipedOutputStream(pin[irunner * npipes]);
        PrintStream psout = new PrintStream(pout, true);

        PipedOutputStream perr = new PipedOutputStream(pin[irunner * npipes + 1]);
        PrintStream pserr = new PrintStream(perr, true);

        srunner.redirect(psout, pserr);

        quit = false; // signals the Threads that they should exit
        // Starting two seperate threads to read from the PipedInputStreams
        for (int i = irunner * npipes; i < irunner * npipes + npipes; i++) {
          reader[i] = new Thread(EditorConsolePane.this);
          reader[i].setDaemon(true);
          reader[i].start();
        }
        irunner++;
      }
*/

      // redirect System IO to IDE message area
      PipedOutputStream oout = new PipedOutputStream(pin[0]);
      PrintStream ops = new PrintStream(oout, true);
      System.setOut(ops);
      reader[0] = new Thread(EditorConsolePane.this);
      reader[0].setDaemon(true);
      reader[0].start();

      PipedOutputStream eout = new PipedOutputStream(pin[1]);
      PrintStream eps = new PrintStream(eout, true);
      System.setErr(eps);

      reader[1] = new Thread(EditorConsolePane.this);
      reader[1].setDaemon(true);
      reader[1].start();
    } catch (IOException e1) {
      Debug.log(-1, "Redirecting System IO failed", e1.getMessage());
    }
  }

  private void appendMsg(String msg) {
    HTMLDocument doc = (HTMLDocument) textArea.getDocument();
    HTMLEditorKit kit = (HTMLEditorKit) textArea.getEditorKit();
    try {
      kit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
    } catch (Exception e) {
      Debug.error(me + "Problem appending text to message area!\n%s", e.getMessage());
    }
  }

  /*
   public synchronized void windowClosed(WindowEvent evt)
   {
   quit=true;
   this.notifyAll(); // stop all threads
   try { reader.join(1000);pin.close();   } catch (Exception e){}
   try { reader2.join(1000);pin2.close(); } catch (Exception e){}
   System.exit(0);
   }

   public synchronized void windowClosing(WindowEvent evt)
   {
   frame.setVisible(false); // default behaviour of JFrame
   frame.dispose();
   }
   */
  static final String lineSep = System.getProperty("line.separator");

  public final static String CSS_Colors =
      ".normal{ color: black; }"
          + ".debug { color:#505000; }"
          + ".info  { color: blue; }"
          + ".log   { color: #09806A; }"
          + ".error { color: red; }";

  private String htmlize(String msg) {
    StringBuilder sb = new StringBuilder();
    Pattern patMsgCat = Pattern.compile("\\[(.+?)\\].*");
    msg = msg.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;");

    String color = "color: black;";

    for (String line : msg.split(lineSep)) {
      Matcher m = patMsgCat.matcher(line);
      if (m.matches()) {
        String logType = m.group(1).toLowerCase();
        if (logType.contains("error")) color = "color: red;";
        else if (logType.contains("debug")) color = "color: #505000;";
        else if (logType.contains("log")) color = "color: #09806A;";
        else if (logType.contains("info")) color = "color: blue;";
      }
      String font = "font-family:monospace; font-size: medium;";
      int margin = 0;
      line = String.format("<pre style=\"margin: %d; %s %s\">%s</pre>", margin, font, color, line);
      sb.append(line);
    }
    return sb.toString();
  }

  @Override
  public synchronized void run() {
    for (int i = 0; i < NUM_PIPES; i++) {
      while (Thread.currentThread() == reader[i]) {
        try {
          this.wait(100);
        } catch (InterruptedException ie) {
        }

        int availableToRead = 0;
        try {
          availableToRead = pin[i].available();
        } catch (IOException e) {
        }
        if (availableToRead != 0) {
          String input = null;
          try {
            input = this.readLine(pin[i]);
          } catch (IOException e) {
          }
          if (null != input) {
            final String finalInput = input;
            EventQueue.invokeLater(() -> {
              synchronized (textArea) {
                appendMsg(htmlize(finalInput));
                int textLen = textArea.getDocument().getLength();
                if (textLen > 0) {
                  int textPosEnd = textLen - 1;
                  int rowStart;
                  try {
                    rowStart = Math.max(0, Utilities.getRowStart(textArea, textPosEnd));
                  } catch (Exception e) {
                    rowStart = textPosEnd;
                  }
                  textArea.setCaretPosition(rowStart);
                }
              }
            });
          }
        }
        if (quit) {
          return;
        }
      }
    }
  }

  public synchronized String readLine(PipedInputStream in) throws IOException {
    String input = "";
    do {
      int available = in.available();
      if (available == 0) {
        break;
      }
      byte b[] = new byte[available];
      in.read(b);
      input = input + new String(b, 0, b.length);
    } while (!input.endsWith("\n") && !input.endsWith("\r\n") && !quit);
    return input;
  }

  public void clear() {
    textArea.setText("");
  }
}

class JTextPaneHTMLTransferHandler extends TransferHandler {
  private static final String me = "EditorConsolePane: ";

  public JTextPaneHTMLTransferHandler() {
  }

  @Override
  public void exportToClipboard(JComponent comp, Clipboard clip, int action) {
    super.exportToClipboard(comp, clip, action);
  }

  @Override
  public int getSourceActions(JComponent c) {
    return COPY_OR_MOVE;
  }

  @Override
  protected Transferable createTransferable(JComponent c) {
    JTextPane aTextPane = (JTextPane) c;

    HTMLEditorKit kit = ((HTMLEditorKit) aTextPane.getEditorKit());
    StyledDocument sdoc = aTextPane.getStyledDocument();
    int sel_start = aTextPane.getSelectionStart();
    int sel_end = aTextPane.getSelectionEnd();

    int i = sel_start;
    StringBuilder output = new StringBuilder();
    while (i < sel_end) {
      Element e = sdoc.getCharacterElement(i);
      Object nameAttr = e.getAttributes().getAttribute(StyleConstants.NameAttribute);
      int start = e.getStartOffset(), end = e.getEndOffset();
      if (nameAttr == HTML.Tag.BR) {
        output.append("\n");
      } else if (nameAttr == HTML.Tag.CONTENT) {
        if (start < sel_start) {
          start = sel_start;
        }
        if (end > sel_end) {
          end = sel_end;
        }
        try {
          String str = sdoc.getText(start, end - start);
          output.append(str);
        } catch (BadLocationException ble) {
          Debug.error(me + "Copy-paste problem!\n%s", ble.getMessage());
        }
      }
      i = end;
    }
    return new StringSelection(output.toString());
  }
}
