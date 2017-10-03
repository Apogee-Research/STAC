package org.tigris.gef.base;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.Icon;

import org.tigris.gef.util.Localizer;
import user.commands.image.DrawingCanvas;

/**
 * This is a rewrite of SaveGIFAction to use the JDK 1.4 ImageIO library to
 * write PNG files, with both better performance and memory efficiency.
 * Unfortunately though, this is only available to those with JRE1.4 and above.
 */
public class SavePNGAction extends SaveGraphicsAction {

    private static final long serialVersionUID = 162553935201931332L;

    /**
     * Used as background color in image and set transparent. Chosen because
     * it's unlikely to be selected by the user, and leaves the diagram readable
     * if viewed without transparency.
     */
    public static final int TRANSPARENT_BG_COLOR = 0x00efefef;

    /**
     * Creates a new SavePNGAction
     *
     * @param name The name of the action
     */
    public SavePNGAction(String name) {
        this(name, false);
    }

    /**
     * Creates a new SavePNGAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     */
    public SavePNGAction(String name, Icon icon) {
        this(name, icon, false);
    }

    /**
     * Creates a new SavePNGAction
     *
     * @param name The name of the action
     * @param localize Whether to localize the name or not
     */
    public SavePNGAction(String name, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name);
    }

    /**
     * Creates a new SavePNGAction
     *
     * @param name The name of the action
     * @param icon The icon of the action
     * @param localize Whether to localize the name or not
     */
    public SavePNGAction(String name, Icon icon, boolean localize) {
        super(localize ? Localizer.localize("GefBase", name) : name, icon);
    }

    /**
     * Write the diagram contained by the current editor into an OutputStream as
     * a PNG image.
     */
    protected void saveGraphics(OutputStream s, Editor ce, Rectangle drawingArea)
            throws IOException {

        // Create an offscreen image and render the diagram into it.
        Image i = new BufferedImage(drawingArea.width * scale,
                drawingArea.height * scale, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) i.getGraphics();
        g.scale(scale, scale);
        g.setColor(new Color(TRANSPARENT_BG_COLOR, true));
        Composite c = g.getComposite();
        g.setComposite(AlphaComposite.Src);
        g.fillRect(0, 0, drawingArea.width * scale, drawingArea.height * scale);
        g.setComposite(c);
        // a little extra won't hurt
        g.translate(-drawingArea.x, -drawingArea.y);
        ce.print(g);

        ImageIO.write((RenderedImage) i, "png", s);

        g.dispose();
        // force garbage collection, to prevent out of memory exceptions
        g = null;
        i = null;
    }
}
