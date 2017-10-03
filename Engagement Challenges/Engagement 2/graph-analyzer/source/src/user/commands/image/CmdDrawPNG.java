/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package user.commands.image;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.tigris.gef.base.CmdSavePNG;
import org.tigris.gef.base.Globals;
import user.commands.Cmd;

/**
 *
 * @author user
 */
public class CmdDrawPNG implements Cmd {

    /**
     * Define a inner class called DrawCanvas which is a JPanel used for custom
     * drawing
     */
    private class DrawCanvas extends JPanel {

        // Override paintComponent to perform your own painting

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);     // paint parent's background
            setBackground(Color.BLACK);  // set background color for this JPanel

         // Your custom painting codes. For example,
            // Drawing primitive shapes
            g.setColor(Color.YELLOW);    // set the drawing color
            g.drawLine(30, 40, 100, 200);
            g.drawOval(150, 180, 10, 10);
            g.drawRect(200, 210, 20, 30);
            g.setColor(Color.RED);       // change the drawing color
            g.fillOval(300, 310, 30, 50);
            g.fillRect(400, 350, 60, 50);
            // Printing texts
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g.drawString("Testing custom drawing ...", 10, 20);
        }
    }

    @Override
    public Object runCmd(Map inputs) {

        DrawCanvas canvas = new DrawCanvas();    // Construct the drawing canvas
        canvas.setPreferredSize(new Dimension(Globals.displayheight, Globals.displaywidth));

        /*Container cp = getContentPane();
         cp.setLayout(new BorderLayout());
         cp.add(canvas, BorderLayout.CENTER);
         cp.add(btnPanel, BorderLayout.SOUTH);*/

      
            String imagef = (String) inputs.get("imageout");
            CmdSavePNG sps = new CmdSavePNG(imagef);


            Globals.curEditor().setJComponent(canvas);
            Globals.curEditor().executeCmd(sps, null);

       
        return null;
    }

}
