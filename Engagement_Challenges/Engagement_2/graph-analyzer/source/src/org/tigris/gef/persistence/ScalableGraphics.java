/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tigris.gef.persistence;

import java.awt.Graphics2D;

/**
 *
 * @author burkep
 */
public abstract class ScalableGraphics extends Graphics2D {

    public abstract void setScale(double s);

}
