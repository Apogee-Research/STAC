package com.stac.image.utilities;

import javax.swing.*;
import java.awt.image.BufferedImage;

/**
 *
 */
public class ShowImage extends JFrame {
    public ShowImage(String name, BufferedImage im) {
        this(name, im, true);
    }

    public ShowImage(String name, BufferedImage im, boolean vis) {
        this.setTitle(name);
        this.setSize(im.getWidth(), im.getHeight());
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JLabel lbl = new JLabel(new ImageIcon(im));
        JPanel jPanel = new JPanel();
        jPanel.add(lbl);
        add(jPanel);
        setVisible(vis);
    }
}