/*
 * Copyright (c) 2010-2017, sikuli.org, sikulix.com - MIT license
 */
package org.jdesktop.swingx.decorator;

import java.awt.Color;
import java.awt.Component;

import javax.swing.UIManager;

import org.jdesktop.swingx.decorator.HighlightPredicate.NotHighlightPredicate;
import org.jdesktop.swingx.decorator.HighlightPredicate.RowGroupHighlightPredicate;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.UIColorHighlighterAddon;
import org.jdesktop.swingx.plaf.UIDependent;
import org.jdesktop.swingx.util.PaintUtils;

/**
 * A Factory which creates common Highlighters. <p>
 *
 * PENDING JW: really need the alternate striping? That's how the
 * old AlternateRowHighlighter did it, but feels a bit wrong to
 * have one stripe hardcoded to WHITE. Would prefer to remove.
 *
 * @author Jeanette Winzenburg
 */
public final class HighlighterFactory {
    private static Highlighter COMPUTED_FOREGROUND_HIGHLIGHTER = new AbstractHighlighter() {
        @Override
        protected Component doHighlight(Component component, ComponentAdapter adapter) {
            component.setForeground(PaintUtils.computeForeground(component.getBackground()));

            return component;
        }
    };

    /**
     * Creates a highlighter that sets the foreground color to WHITE or BLACK by computing the best
     * match based on the current background color. It is recommended that no background changing
     * highlighters be added after this highlighter, lest the computation be incorrect.
     *
     * @return a highlighter that computes the appropriate foreground color
     */
    public static Highlighter createComputedForegroundHighlighter() {
        return COMPUTED_FOREGROUND_HIGHLIGHTER;
    }

    /**
     * Creates and returns a Highlighter which highlights every second row
     * background with a color depending on the LookAndFeel. The rows between
     * are not highlighted, that is typically, they will show the container's
     * background.
     *
     * @return a Highlighter striping every second row background.
     */
    public static Highlighter createSimpleStriping() {
        ColorHighlighter hl = new UIColorHighlighter(HighlightPredicate.ODD);
        return hl;
    }

    /**
     * Creates and returns a Highlighter which highlights every second row group
     * background with a color depending on LF. The row groups between are not
     * highlighted, that is typically, they will show the container's
     * background.
     *
     * @param rowsPerGroup the number of rows in a group
     * @return a Highlighter striping every second row group background.
     */
    public static Highlighter createSimpleStriping(int rowsPerGroup) {
        return new UIColorHighlighter(new RowGroupHighlightPredicate(
                rowsPerGroup));
    }

    /**
     * Creates and returns a Highlighter which highlights every second row
     * background with the given color. The rows between are not highlighted
     * that is typically, they will show the container's background.
     *
     * @param stripeBackground the background color for the striping.
     * @return a Highlighter striping every second row background.
     */
    public static Highlighter createSimpleStriping(Color stripeBackground) {
        ColorHighlighter hl = new ColorHighlighter(HighlightPredicate.ODD, stripeBackground, null);
        return hl;
    }

    /**
     * Creates and returns a Highlighter which highlights every second row group
     * background with the given color. The row groups between are not
     * highlighted, that is they typically will show the container's background.
     *
     * @param stripeBackground the background color for the striping.
     * @param rowsPerGroup the number of rows in a group
     * @return a Highlighter striping every second row group background.
     */
    public static Highlighter createSimpleStriping(Color stripeBackground,
            int rowsPerGroup) {
        HighlightPredicate predicate = new RowGroupHighlightPredicate(
                rowsPerGroup);
        ColorHighlighter hl = new ColorHighlighter(predicate, stripeBackground,
                null);
        return hl;
    }

    /**
     * Creates and returns a Highlighter which highlights
     * with alternate background. The first is Color.WHITE, the second
     * with the color depending on LF.
     *
     * @return a Highlighter striping every second row background.
     */
    public static Highlighter createAlternateStriping() {
        ColorHighlighter first = new ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, null);
        ColorHighlighter hl = new UIColorHighlighter(HighlightPredicate.ODD);
        return new CompoundHighlighter(first, hl);
    }

    /**
     * Creates and returns a Highlighter which highlights
     * with alternate background. the first Color.WHITE, the second
     * with the color depending on LF.
     *
     * @param rowsPerGroup the number of rows in a group
     * @return a Highlighter striping every second row group background.
     */
    public static Highlighter createAlternateStriping(int rowsPerGroup) {
        HighlightPredicate predicate = new RowGroupHighlightPredicate(rowsPerGroup);
        ColorHighlighter first = new ColorHighlighter(new NotHighlightPredicate(predicate), Color.WHITE, null);
        ColorHighlighter hl = new UIColorHighlighter(predicate);
        return new CompoundHighlighter(first, hl);
    }

    /**
     * Creates and returns a Highlighter which highlights with
     * alternating background, starting with the base.
     *
     * @param baseBackground the background color for the even rows.
     * @param alternateBackground background color for odd rows.
     * @return a Highlighter striping alternating background.
     */
    public static Highlighter createAlternateStriping(Color baseBackground, Color alternateBackground) {
        ColorHighlighter base = new ColorHighlighter(HighlightPredicate.EVEN, baseBackground, null);
        ColorHighlighter alternate = new ColorHighlighter(HighlightPredicate.ODD, alternateBackground, null);
        return new CompoundHighlighter(base, alternate);
    }

    /**
     * Creates and returns a Highlighter which highlights with
     * alternating background, starting with the base.
     *
     * @param baseBackground the background color for the even rows.
     * @param alternateBackground background color for odd rows.
     * @param linesPerStripe the number of rows in a group
     * @return a Highlighter striping every second row group background.
     */
    public static Highlighter createAlternateStriping(Color baseBackground, Color alternateBackground, int linesPerStripe) {
        HighlightPredicate predicate = new RowGroupHighlightPredicate(linesPerStripe);
        ColorHighlighter base = new ColorHighlighter(new NotHighlightPredicate(predicate), baseBackground, null);
        ColorHighlighter alternate = new ColorHighlighter(predicate, alternateBackground, null);

        return new CompoundHighlighter(base, alternate);
    }

//--------------------------- UI dependent

    /**
     * A ColorHighlighter with UI-dependent background.
     *
     * PENDING JW: internally install a AND predicate to check for LFs
     *   which provide striping on the UI-Delegate level?
     *
     */
    public static class UIColorHighlighter extends ColorHighlighter
        implements UIDependent {

        static {
            LookAndFeelAddons.contribute(new UIColorHighlighterAddon());
        }

     /**
      * Instantiates a ColorHighlighter with LF provided unselected
      * background and default predicate. All other colors are null.
      *
      */
     public UIColorHighlighter() {
         this(null);
     }

     /**
      * Instantiates a ColorHighlighter with LF provided unselected
      * background and the given predicate. All other colors are null.
     * @param odd the predicate to use
     */
    public UIColorHighlighter(HighlightPredicate odd) {
        super(odd, null, null);
        updateUI();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void updateUI() {
         setBackground(getUIColor());
     }

    /**
     * Looks up and returns the LF specific color to use for striping
     * background highlighting.
     *
     * Lookup strategy:
     * <ol>
     * <li> in UIManager for key = "UIColorHighlighter.stripingBackground", if null
     * <li> use hard-coded HighlighterFactory.GENERIC_GREY
     * </ol>
     *
     * PENDING: fallback or not?
     *
     * @return the LF specific color for background striping.
     */
     private Color getUIColor() {
         Color color = null;
         // JW: can't do - Nimbus stripes even rows (somewhere deep down the ui?)
         //, SwingX stripes odd rows
         // --> combined == no striping
//         color = UIManager.getColor("Table.alternateRowColor");
         if (color == null) {
             color = UIManager.getColor("UIColorHighlighter.stripingBackground");
         }
         if (color == null) {
             color = HighlighterFactory.GENERIC_GRAY;
         }
         return color;
     }
//     /**
//      * this is a hack until we can think about something better!
//      * we map all known selection colors to highlighter colors.
//      *
//      */
//     private void initColorMap() {
//         colorMap = new HashMap<Color, Color>();
//         // Ocean
//         colorMap.put(new Color(184, 207, 229), new Color(230, 238, 246));
//         // xp blue
//         colorMap.put(new Color(49, 106, 197), new Color(224, 233, 246));
//         // xp silver
//         colorMap.put(new Color(178, 180, 191), new Color(235, 235, 236));
//         // xp olive
//         colorMap.put(new Color(147, 160, 112), new Color(228, 231, 219));
//         // win classic
//         colorMap.put(new Color(10, 36, 106), new Color(218, 222, 233));
//         // win 2k?
//         colorMap.put(new Color(0, 0, 128), new Color(218, 222, 233));
//         // default metal
//         colorMap.put(new Color(205, 205, 255), new Color(235, 235, 255));
//         // mac OS X
//         colorMap.put(new Color(56, 117, 215), new Color(237, 243, 254));
//
//     }

 }

    /** predefined colors - from old alternateRow. */
    public final static Color BEIGE = new Color(245, 245, 220);
    public final static Color LINE_PRINTER = new Color(0xCC, 0xCC, 0xFF);
    public final static Color CLASSIC_LINE_PRINTER = new Color(0xCC, 0xFF, 0xCC);
    public final static Color FLORAL_WHITE = new Color(255, 250, 240);
    public final static Color QUICKSILVER = new Color(0xF0, 0xF0, 0xE0);
    public final static Color GENERIC_GRAY = new Color(229, 229, 229);
    public final static Color LEDGER = new Color(0xF5, 0xFF, 0xF5);
    public final static Color NOTEPAD = new Color(0xFF, 0xFF, 0xCC);

}
