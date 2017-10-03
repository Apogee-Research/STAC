/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.gef.base;

import java.awt.Graphics;
import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPainter;

/**
 *
 * @author user
 */
public interface GraphicsSpecial {

    public void paintContents(LayerDiagram layer, Graphics g, FigPainter painter);

    public void paintContents(LayerDiagram layer, Graphics g, FigPainter painter, int x, int y, int scale, Fig parent);

}
