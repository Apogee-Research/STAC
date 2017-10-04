/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.jhlabs.image;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;

/**
 * A filter which wraps an image around a circular arc.
 */
public class CircleFilter extends TransformFilter {

    private float radius = 10;

    private float height = 20;

    private float angle = 0;

    private float spreadAngle = (float) Math.PI;

    private float centreX = 0.5f;

    private float centreY = 0.5f;

    private float icentreX;

    private float icentreY;

    private float iWidth;

    private float iHeight;

    /**
     * Construct a CircleFilter.
     */
    public CircleFilter() {
        setEdgeAction(ZERO);
    }

    /**
     * Set the height of the arc.
     * @param height the height
     * @see #getHeight
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Get the height of the arc.
     * @return the height
     * @see #setHeight
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set the angle of the arc.
     * @param angle the angle of the arc.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        this.angle = angle;
    }

    /**
     * Returns the angle of the arc.
     * @return the angle of the arc.
     * @see #setAngle
     */
    public float getAngle() {
        ClassgetAngle replacementClass = new  ClassgetAngle();
        ;
        return replacementClass.doIt0();
    }

    /**
     * Set the spread angle of the arc.
     * @param spreadAngle the angle
     * @angle
     * @see #getSpreadAngle
     */
    public void setSpreadAngle(float spreadAngle) {
        this.spreadAngle = spreadAngle;
    }

    /**
     * Get the spread angle of the arc.
     * @return the angle
     * @angle
     * @see #setSpreadAngle
     */
    public float getSpreadAngle() {
        return spreadAngle;
    }

    /**
	 * Set the radius of the effect.
	 * @param radius the radius
     * @min-value 0
     * @see #getRadius
	 */
    public void setRadius(float radius) {
        ClasssetRadius replacementClass = new  ClasssetRadius(radius);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the radius of the effect.
	 * @return the radius
     * @see #setRadius
	 */
    public float getRadius() {
        return radius;
    }

    /**
	 * Set the centre of the effect in the Y direction as a proportion of the image size.
	 * @param centreX the center
     * @see #getCentreX
	 */
    public void setCentreX(float centreX) {
        this.centreX = centreX;
    }

    /**
	 * Get the centre of the effect in the X direction as a proportion of the image size.
	 * @return the center
     * @see #setCentreX
	 */
    public float getCentreX() {
        return centreX;
    }

    /**
	 * Set the centre of the effect in the Y direction as a proportion of the image size.
	 * @param centreY the center
     * @see #getCentreY
	 */
    public void setCentreY(float centreY) {
        ClasssetCentreY replacementClass = new  ClasssetCentreY(centreY);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the centre of the effect in the Y direction as a proportion of the image size.
	 * @return the center
     * @see #setCentreY
	 */
    public float getCentreY() {
        return centreY;
    }

    /**
	 * Set the centre of the effect as a proportion of the image size.
	 * @param centre the center
     * @see #getCentre
	 */
    public void setCentre(Point2D centre) {
        this.centreX = (float) centre.getX();
        this.centreY = (float) centre.getY();
    }

    /**
	 * Get the centre of the effect as a proportion of the image size.
	 * @return the center
     * @see #setCentre
	 */
    public Point2D getCentre() {
        ClassgetCentre replacementClass = new  ClassgetCentre();
        ;
        return replacementClass.doIt0();
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Classfilter replacementClass = new  Classfilter(src, dst);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    protected void transformInverse(int x, int y, float[] out) {
        ClasstransformInverse replacementClass = new  ClasstransformInverse(x, y, out);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
    }

    public String toString() {
        return "Distort/Circle...";
    }

    public class ClassgetAngle {

        public ClassgetAngle() {
        }

        public float doIt0() {
            return angle;
        }
    }

    public class ClasssetRadius {

        public ClasssetRadius(float radius) {
            this.radius = radius;
        }

        private float radius;

        public void doIt0() {
            CircleFilter.this.radius = radius;
        }
    }

    public class ClasssetCentreY {

        public ClasssetCentreY(float centreY) {
            this.centreY = centreY;
        }

        private float centreY;

        public void doIt0() {
            CircleFilter.this.centreY = centreY;
        }
    }

    public class ClassgetCentre {

        public ClassgetCentre() {
        }

        public Point2D doIt0() {
            return new  Point2D.Float(centreX, centreY);
        }
    }

    public class Classfilter {

        public Classfilter(BufferedImage src, BufferedImage dst) {
            this.src = src;
            this.dst = dst;
        }

        private BufferedImage src;

        private BufferedImage dst;

        public void doIt0() {
            iWidth = src.getWidth();
            iHeight = src.getHeight();
            icentreX = iWidth * centreX;
            icentreY = iHeight * centreY;
            iWidth--;
        }

        public BufferedImage doIt1() {
            return CircleFilter.super.filter(src, dst);
        }
    }

    protected class ClasstransformInverse {

        public ClasstransformInverse(int x, int y, float[] out) {
            this.x = x;
            this.y = y;
            this.out = out;
        }

        private int x;

        private int y;

        private float[] out;

        private float dx;

        public void doIt0() {
            dx = x - icentreX;
        }

        private float dy;

        private float theta;

        private float r;

        public void doIt1() {
            dy = y - icentreY;
            theta = (float) Math.atan2(-dy, -dx) + angle;
            r = (float) Math.sqrt(dx * dx + dy * dy);
        }

        public void doIt2() {
            theta = ImageMath.mod(theta, 2 * (float) Math.PI);
            out[0] = iWidth * theta / (spreadAngle + 0.00001f);
        }

        public void doIt3() {
            out[1] = iHeight * (1 - (r - radius) / (height + 0.00001f));
        }
    }
}
