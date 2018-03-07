/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.sikuli.ide;

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
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Arrays;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import javax.swing.JMenuItem;
import org.sikuli.basics.Debug;
import org.sikuli.scriptrunner.IScriptRunner;
import org.sikuli.basics.Settings;
import org.sikuli.scriptrunner.ScriptingSupport;

public class EditorConsolePane extends JPanel implements Runnable {

  private static final String me = "EditorConsolePane: ";
  static boolean ENABLE_IO_REDIRECT = true;

  static {
    String flag = System.getProperty("sikuli.console");
    if (flag != null && flag.equals("false")) {
      ENABLE_IO_REDIRECT = false;
    }
  }
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
    textArea = new JTextPane();
    textArea.setEditorKit(new HTMLEditorKit());
    textArea.setTransferHandler(new JTextPaneHTMLTransferHandler());
    String css = PreferencesUser.getInstance().getConsoleCSS();
    ((HTMLEditorKit) textArea.getEditorKit()).getStyleSheet().addRule(css);
    textArea.setEditable(false);

    setLayout(new BorderLayout());
    add(new JScrollPane(textArea), BorderLayout.CENTER);

    if (ENABLE_IO_REDIRECT) {
			Debug.log(3, "EditorConsolePane: starting redirection to message area");
      int npipes = 2;
      NUM_PIPES = npipes * ScriptingSupport.scriptRunner.size();
      pin = new PipedInputStream[NUM_PIPES];
      reader = new Thread[NUM_PIPES];
      for (int i = 0; i < NUM_PIPES; i++) {
        pin[i] = new PipedInputStream();
      }

      int irunner = 0;
      for (IScriptRunner srunner : ScriptingSupport.scriptRunner.values()) {
				Debug.log(3, "EditorConsolePane: redirection for %s", srunner.getName());
        if (srunner.doSomethingSpecial("redirect", Arrays.copyOfRange(pin, irunner*npipes, irunner*npipes+2))) {
          Debug.log(3, "EditorConsolePane: redirection success for %s", srunner.getName());
          quit = false; // signals the Threads that they should exit
//TODO Hack to avoid repeated redirect of stdout/err
          ScriptingSupport.systemRedirected = true;

          // Starting two seperate threads to read from the PipedInputStreams
          for (int i = irunner * npipes; i < irunner * npipes + npipes; i++) {
            reader[i] = new Thread(this);
            reader[i].setDaemon(true);
            reader[i].start();
          }
          irunner++;
        }
      }
    }

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

  private String htmlize(String msg) {
    StringBuilder sb = new StringBuilder();
    Pattern patMsgCat = Pattern.compile("\\[(.+?)\\].*");
    msg = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">","&gt;");
    for (String line : msg.split(lineSep)) {
      Matcher m = patMsgCat.matcher(line);
      String cls = "normal";
      if (m.matches()) {
        cls = m.group(1);
      }
      line = "<span class='" + cls + "'>" + line + "</span>";
      sb.append(line).append("<br>");
    }
    return sb.toString();
  }

  @Override
  public synchronized void run() {
    try {
      for (int i = 0; i < NUM_PIPES; i++) {
        while (Thread.currentThread() == reader[i]) {
          try {
            this.wait(100);
          } catch (InterruptedException ie) {
          }
          if (pin[i].available() != 0) {
            String input = this.readLine(pin[i]);
            appendMsg(htmlize(input));
            if (textArea.getDocument().getLength() > 0) {
              textArea.setCaretPosition(textArea.getDocument().getLength() - 1);
            }
          }
          if (quit) {
            return;
          }
        }
      }

    } catch (Exception e) {
      Debug.error(me + "Console reports an internal error:\n%s", e.getMessage());
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
