/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphviz;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import org.tigris.gef.presentation.FigCircle;

/**
 *
 * @author burkep
 */
public class TestCircleMath {
    /*
     public static void mainx(String[] args) {

     int rw = 100;
     int rh = 100;

     Rectangle drawspace = new Rectangle(0, 0, rw, rh);

     FigCircle n = new FigCircle(Color.BLUE);
     n.x = 7;
     n.y = 17;
     n.size = 15;
     n.scale = 1;

     org.graph.commons.logging.LogFactory.getLog(null).info("newpath");
     System.out.print("" + n.x + " " + n.y + " " + n.size + " " + n.size + " 0 360 ");
     org.graph.commons.logging.LogFactory.getLog(null).info("ellipse");
     org.graph.commons.logging.LogFactory.getLog(null).info("stroke");
     org.graph.commons.logging.LogFactory.getLog(null).info("");

     Rectangle rect = new Rectangle(0, 0, 255, 255);

     ScaledRectangle innerBox = TestCircleMath.getInnerBounds(drawspace, rect, n);

     FigCircle ns = new FigCircle(Color.BLUE);
     ns.x = innerBox.x + innerBox.scale * 7;
     ns.y = innerBox.y + innerBox.scale * 17;
     ns.size = 15;//innerBox.scale*15;//print node
     ns.scale = innerBox.scale;
     //need startpoint, scaling factor
     double inversescale = (double) 1 / (double) innerBox.scale;
     org.graph.commons.logging.LogFactory.getLog(null).info(inversescale + " " + inversescale + " scale");
     org.graph.commons.logging.LogFactory.getLog(null).info("newpath");
     System.out.print("" + ns.x + " " + ns.y + " " + ns.size + " " + ns.size + " 0 360 ");
     org.graph.commons.logging.LogFactory.getLog(null).info("ellipse");
     org.graph.commons.logging.LogFactory.getLog(null).info("stroke");
     org.graph.commons.logging.LogFactory.getLog(null).info(innerBox.scale + " " + innerBox.scale + " scale");

     org.graph.commons.logging.LogFactory.getLog(null).info("rect:" + innerBox.toString());

     org.graph.commons.logging.LogFactory.getLog(null).info("-------------------------------------");
     FigCircle ns3 = new FigCircle(Color.BLUE);
     //ns.x= innerBox.x + innerBox.scale*7;
     //ns.y=innerBox.y + innerBox.scale*17;       

     Rectangle brect = new Rectangle(0, 0, 255, 255);
     ScaledRectangle newrect = TestCircleMath.getInnerBounds(drawspace, brect, ns);
     FigCircle cs = new FigCircle(Color.RED);
     cs.x = 13;
     cs.y = 15;
     ns.size = 15;
     printScaledFig(cs, newrect, drawspace);

     }*/

    /*public static void main(String[] args) {

        FigCircle n = new FigCircle(108, 54, 150, 150);
        n.scale = 1;

        ScaledRectangle brect = new ScaledRectangle(0, 0, 255, 255);
        ScaledRectangle newrect = TestCircleMath.getInnerBounds(brect, n);
        FigCircle cs = new FigCircle(108, 54, 150, 150);
        printScaledFig(cs, newrect);

        ScaledRectangle brect1 = new ScaledRectangle(0, 0, 1255, 1255);
        ScaledRectangle newrect1 = TestCircleMath.getInnerBounds(brect1, cs);
        FigCircle cs1 = new FigCircle(108, 54, 150, 150);

        printScaledFig(cs1, newrect1);

        ScaledRectangle brect2 = new ScaledRectangle(0, 0, 255, 255);
        ScaledRectangle newrect2 = TestCircleMath.getInnerBounds(brect2, cs1);
        FigCircle cs2 = new FigCircle(108, 54, 150, 150);
        printScaledFig(cs2, newrect2);

    }*/

    /*static void printwholegraph(List<FigCircle> nodes, FigCircle enclosingCircle, Rectangle drawspace){
     ScaledRectangle brect1 = new ScaledRectangle(0, 0, 255, 255);
     ScaledRectangle newrect1 = TestCircleMath.getInnerBounds( brect1, enclosingCircle);
     FigCircle cs1 = new FigCircle(Color.RED);
     cs1.x = 13;
     cs1.y = 15;
     cs1.size = 15;
     printScaledFig(cs1, newrect1);
     }*/
    public static void printScaledFig(FigCircle ns, ScaledRectangle childdrawspace) {

        double inversescale = (double) 1 / (double) childdrawspace.scale;
        ns.scale = childdrawspace.scale;
        DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        df.setMaximumFractionDigits(340); //340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

        org.graph.commons.logging.LogFactory.getLog(null).info(df.format(inversescale) + " " + df.format(inversescale) + " scale");
        org.graph.commons.logging.LogFactory.getLog(null).info("newpath");
        System.out.print("" + (childdrawspace.x + ns.getX()) + " -" + (childdrawspace.y + ns.getY()) + " " + ns.getHeight() + " -" + ns.getWidth() + " 0 360 ");
        org.graph.commons.logging.LogFactory.getLog(null).info("ellipse");
        org.graph.commons.logging.LogFactory.getLog(null).info("stroke");
        org.graph.commons.logging.LogFactory.getLog(null).info(childdrawspace.scale + " " + childdrawspace.scale + " scale");

    }

    public static ScaledRectangle getInnerBounds(ScaledRectangle innerBox, FigCircle node) {

        int innerbox_x_lessdiff;
        int innerbox_y_lessdiff;
        int innerBoxHeight;
        {
            double innerbox_Scaleminus_node;
            int nx = node.getX();
            int ny = node.getX();
            int nheight = node.getHeight();
            double notscaled_newrect_widthheight = Math.sqrt((nheight * nheight) / 2);
            innerbox_Scaleminus_node = (innerBox.height / notscaled_newrect_widthheight);
            innerbox_x_lessdiff = (int) (innerbox_Scaleminus_node * nx);
            innerbox_y_lessdiff = (int) (innerbox_Scaleminus_node * ny);
            innerBoxHeight = (int) (innerbox_Scaleminus_node * nheight);
            innerBox.scale = node.scale * innerbox_Scaleminus_node;
        }

        double newrect_inner_widthheight = Math.sqrt((innerBoxHeight * innerBoxHeight) / 2);
        int inner_diff = (int) ((innerBoxHeight - newrect_inner_widthheight) / 2);

        innerBox.x = innerbox_x_lessdiff + inner_diff;
        innerBox.y = innerbox_y_lessdiff + inner_diff;

        org.graph.commons.logging.LogFactory.getLog(null).info("innerBox.scale:" + innerBox.scale);
        org.graph.commons.logging.LogFactory.getLog(null).info("###");

        return innerBox;
    }

    public static class ScaledRectangle extends Rectangle {

        public double scale = 1.0;

        public ScaledRectangle(int x, int y, int w, int h) {
            super(x, y, w, h);
        }

        public ScaledRectangle(Rectangle rect, boolean makeSquare) {
            this(rect.x, rect.y, rect.width, rect.height);
            if (makeSquare) {
                if (rect.height > rect.width) {
                    x = rect.height;
                    y = rect.height;
                } else {
                    x = rect.width;
                    y = rect.width;
                }
            }
        }
    }

}
