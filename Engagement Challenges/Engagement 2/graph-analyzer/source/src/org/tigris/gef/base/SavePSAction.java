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
public class SavePSAction extends SaveGraphicsAction {

    private static final long serialVersionUID = 1373664552595812631L;

    /**
     * Creates a new SavePSAction
     *
     * @param name The name of the action
     */
    public SavePSAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new SavePSAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SavePSAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new SavePSAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SavePSAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new SavePSAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SavePSAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {
        PostscriptWriter ps = new PostscriptWriter(s);
        ps.translate(32, 32 + 778);
        double scale = Math.min(535.0 / drawingArea.width,
                778.0 / drawingArea.height);
        if (scale < 1.0) {
            ps.scale(scale, scale);
        }
        ps.translate(-drawingArea.x, -drawingArea.y);
        ps.setClip(drawingArea.x, drawingArea.y, drawingArea.width,
                drawingArea.height);
        // java bug if using Rectangle.shape() ???
        ce.print(ps);
        ps.dispose();
    }
}
