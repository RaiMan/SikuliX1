/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.hyperlink;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;

import org.jdesktop.swingx.JXEditorPane;

/**
 * A ActionListener using a JXEditorPane to "visit" a LinkModel.
 *
 * adds an internal HyperlinkListener to visit links contained
 * in the document.
 *
 * @author Jeanette Winzenburg
 */
public class EditorPaneLinkVisitor implements ActionListener {
    private JXEditorPane editorPane;
    private HyperlinkListener hyperlinkListener;
    private LinkModel internalLink;

    public EditorPaneLinkVisitor() {
        this(null);
    }

    public EditorPaneLinkVisitor(JXEditorPane pane) {
        if (pane == null) {
            pane = createDefaultEditorPane();
        }
        this.editorPane = pane;
        pane.addHyperlinkListener(getHyperlinkListener());
    }

    public JXEditorPane getOutputComponent() {
        return editorPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof LinkModel) {
            final LinkModel link = (LinkModel) e.getSource();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    visit(link);

                }
            });
        }

    }

    public void visit(LinkModel link) {
        try {
            // make sure to reload
            editorPane.getDocument().putProperty(Document.StreamDescriptionProperty, null);
            // JW: editorPane defaults to asynchronous loading
            // no need to explicitly start a thread - really?
            editorPane.setPage(link.getURL());
            link.setVisited(true);
        } catch (IOException e1) {
            editorPane.setText("<html>Error 404: couldn't show " + link.getURL() + " </html>");
        }
    }

    protected JXEditorPane createDefaultEditorPane() {
        final JXEditorPane editorPane = new JXEditorPane();
        editorPane.setEditable(false);
        editorPane.setContentType("text/html");
        return editorPane;
    }

    protected HyperlinkListener getHyperlinkListener() {
        if (hyperlinkListener == null) {
            hyperlinkListener = createHyperlinkListener();
        }
        return hyperlinkListener;
    }

    protected HyperlinkListener createHyperlinkListener() {
        return new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (HyperlinkEvent.EventType.ACTIVATED == e.getEventType()) {
                    visitInternal(e.getURL());
                }

            }

        };
    }

    protected LinkModel getInternalLink() {
        if (internalLink == null) {
            internalLink = new LinkModel("internal");
        }
        return internalLink;
    }

    protected void visitInternal(URL url) {
        try {
            getInternalLink().setURL(url);
            visit(getInternalLink());
        } catch (Exception e) {
            // todo: error feedback
        }
    }

}
