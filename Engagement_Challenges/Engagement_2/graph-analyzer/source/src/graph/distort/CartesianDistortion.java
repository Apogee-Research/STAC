package graph.distort;

import graph.*;
import graph.filter.*;
import java.awt.Component;
import java.awt.Frame;

/**
 * A filter that distorts the graph, based on work by Sarkar and Brown at DEC
 * research. The transformations are:
 *
 * <PRE>
 *      T(x) = (d + 1)x / (dx + 1)      //translation
 *      M(x) = (d + 1) / (dx + 1)^2     //magnification
 * </PRE>
 *
 * @see graph.filter.Filter
 * @see graph.distort.PolarDistortion
 *
 * @author Michael Shilman <michaels@eecs.berkeley.edu>
 * @version $Id$
 */
class CartesianDistortion implements Filter {

    public int focus[] = new int[2];
    public int k;
    public static int s_distortIndex = AttributeManager.NO_INDEX;

    ;
	//public EmbedCanvas c;
	
	public CartesianDistortion() {
        if (s_distortIndex == AttributeManager.NO_INDEX) {
            s_distortIndex = AttributeManager.getIndex("Distortion");
        }
    }

    public void apply(Node n) {
    }

    //public void apply(DistortionCanvas c) {
/*
     float focusX = ((float)(focus[0] - c.BORDER)/(c.IMG_X/2.0f));
     float focusY = ((float)(focus[1] - c.BORDER)/(c.IMG_Y/2.0f));

     //        float focusX = ((float)(c.focus[0] - c.BORDER + c.IMG_X/2.0f)/(float)c.IMG_X);
     //        float focusY = ((float)(c.focus[1] - c.BORDER + c.IMG_Y/2.0f)/(float)c.IMG_Y);

     DistortionAttr a = (DistortionAttr)n.attrs[s_distortIndex];
     float x = n.x;
     float y = n.y;
     float nx = Math.abs(focusX - x);
     float ny = Math.abs(focusY - y);
     float mx = (c.k + 1.0f)/((c.k*nx + 1.0f)*(c.k*nx + 1.0f));
     float my = (c.k + 1.0f)/((c.k*ny + 1.0f)*(c.k*ny + 1.0f));
     float tx = (c.k + 1.0f)*nx/(c.k*nx + 1);
     float ty = (c.k + 1.0f)*ny/(c.k*ny + 1);
     if(x < focusX)
     tx = -tx;
     if(y < focusY)
     ty = -ty;

     c.points[i][j][0] = (tx*(c.IMG_X/2) + c.focus[0]);
     c.points[i][j][1] = (ty*(c.IMG_Y/2) + c.focus[1]);
     c.dims[i][j][0] = (mx*c.BOX_X);
     c.dims[i][j][1] = (my*c.BOX_Y);*/
    //}
    /*public Frame buildGUI() { return new Frame(getName()); }
     public Component buildThumbnail() { return null; }
     public String getName() { return "Cartesian Distortion"; }*/
    @Override
    public Frame buildGUI() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component buildThumbnail() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
