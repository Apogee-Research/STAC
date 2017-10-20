package org.tigris.gef.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTextField;

public class Notify extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = -3943708263259356434L;

    public Notify() {
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                dispose();
            }

            public void windowClosed(WindowEvent event) {
                System.exit(0);
            }
        });

        Container cont = getContentPane();
        cont.setLayout(new BorderLayout());
        JTextField text = new JTextField(
                "GEF is a pure library to enable graph modelling, not an application.\nSee http://gef.tigris.org and http://gefdemo.tigris.org for to see details\nof how to use GEF and see example applications.");
        cont.add(text);
        setBounds(10, 10, 300, 200);
        setVisible(true);
    }

    /*public static void main(String args[]) {
        new Notify();
    }*/
}
