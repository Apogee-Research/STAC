package org.tigris.gef.base;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Icon;

import org.tigris.gef.persistence.PostscriptWriter;
import org.tigris.gef.util.Localizer;

/**
 * Action to save a diagram as PostScript in a supplied OutputStream. Requires
 * the CH.ifa.draw.util.PostscriptWriter class. Operates on the diagram in the
 * current editor.
 *
 * Code loosely adapted from SaveGIFAction.
 *
 * @author Frank Wienberg, wienberg@informatik.uni-hamburg.de
 */
public class SaveEPSAction extends SaveGraphicsAction {

    private static final long serialVersionUID = -4098178463187704859L;

    /**
     * Creates a new SaveEPSAction
     *
     * @param name The name of the action
     */
    public SaveEPSAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new SaveEPSAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SaveEPSAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new SaveEPSAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SaveEPSAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new SaveEPSAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SaveEPSAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {
        PostscriptWriter ps = new PostscriptWriter(s, drawingArea);
        ps.translate(-drawingArea.x, -drawingArea.y);
        ce.print(ps);
        ps.dispose();
    }
}
