package org.tigris.gef.base;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.FilteredImageSource;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Icon;

import org.tigris.gef.util.Localizer;

import Acme.JPM.Encoders.GifEncoder;

/**
 * Action to save a diagram as a GIF image in a supplied OutputStream. Requires
 * the Acme.JPM.Encoders.GifEncoder class. Operates on the diagram in the
 * current editor.
 *
 * Code loosely adapted from PrintAction.
 *
 * @author Steve Poole, stevep@wrq.com
 */
public class SaveGIFAction extends SaveGraphicsAction {

    private static final long serialVersionUID = 2390013810162720448L;

    /**
     * Used as background color in image and set transparent. Chosen because
     * it's unlikely to be selected by the user, and leaves the diagram readable
     * if viewed without transparency.
     */
    public static final int TRANSPARENT_BG_COLOR = 0x00efefef;

    /**
     * Creates a new SaveGIFAction
     *
     * @param name The name of the action
     */
    public SaveGIFAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new SaveGIFAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SaveGIFAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new SaveGIFAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SaveGIFAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new SaveGIFAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SaveGIFAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    /**
     * Write the diagram contained by the current editor into an OutputStream as
     * a GIF image.
     */
    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {

        // Create an offscreen image and render the diagram into it.
        Image i = ce.createImage(drawingArea.width * scale, drawingArea.height
                * scale);
        Graphics g = i.getGraphics();
        if (g instanceof Graphics2D) {
            ((Graphics2D) g).scale(scale, scale);
        }
        g.setColor(new Color(TRANSPARENT_BG_COLOR));
        g.fillRect(0, 0, drawingArea.width * scale, drawingArea.height * scale);
        // a little extra won't hurt
        g.translate(-drawingArea.x, -drawingArea.y);
        ce.print(g);

        // Tell the Acme GIF encoder to save the image as a GIF into the
        // output stream. Use the TransFilter to make the background
        // color transparent.
        try {
            FilteredImageSource fis = new FilteredImageSource(i.getSource(),
                    new TransFilter(TRANSPARENT_BG_COLOR));
            GifEncoder ge = new GifEncoder(fis, s);
            // GifEncoder ge = new GifEncoder( i, s );
            ge.encode();
        } catch (IOException e) {
            e.printStackTrace();
        }

        g.dispose();
        // force garbage collection, to prevent out of memory exceptions
        g = null;
        i = null;
    }

} /* end class SaveGIFAction */
