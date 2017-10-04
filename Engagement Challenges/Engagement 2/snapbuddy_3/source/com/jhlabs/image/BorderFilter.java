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
 * A filter to add a border around an image using the supplied Paint, which may be null for no painting.
 */
public class BorderFilter extends AbstractBufferedImageOp {

    private int leftBorder, rightBorder;

    private int topBorder, bottomBorder;

    private Paint borderPaint;

    /**
     * Construct a BorderFilter which does nothing.
     */
    public BorderFilter() {
    }

    /**
     * Construct a BorderFilter.
	 * @param leftBorder the left border value
	 * @param topBorder the top border value
	 * @param rightBorder the right border value
	 * @param bottomBorder the bottom border value
	 * @param borderPaint the paint with which to fill the border
     */
    public BorderFilter(int leftBorder, int topBorder, int rightBorder, int bottomBorder, Paint borderPaint) {
        this.leftBorder = leftBorder;
        this.topBorder = topBorder;
        this.rightBorder = rightBorder;
        this.bottomBorder = bottomBorder;
        this.borderPaint = borderPaint;
    }

    /**
	 * Set the border size on the left edge.
	 * @param leftBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getLeftBorder
	 */
    public void setLeftBorder(int leftBorder) {
        this.leftBorder = leftBorder;
    }

    /**
     * Returns the left border value.
     * @return the left border value.
     * @see #setLeftBorder
     */
    public int getLeftBorder() {
        ClassgetLeftBorder replacementClass = new  ClassgetLeftBorder();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set the border size on the right edge.
	 * @param rightBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getRightBorder
	 */
    public void setRightBorder(int rightBorder) {
        this.rightBorder = rightBorder;
    }

    /**
     * Returns the right border value.
     * @return the right border value.
     * @see #setRightBorder
     */
    public int getRightBorder() {
        ClassgetRightBorder replacementClass = new  ClassgetRightBorder();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set the border size on the top edge.
	 * @param topBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getTopBorder
	 */
    public void setTopBorder(int topBorder) {
        this.topBorder = topBorder;
    }

    /**
     * Returns the top border value.
     * @return the top border value.
     * @see #setTopBorder
     */
    public int getTopBorder() {
        return topBorder;
    }

    /**
	 * Set the border size on the bottom edge.
	 * @param bottomBorder the number of pixels of border to add to the edge
     * @min-value 0
     * @see #getBottomBorder
	 */
    public void setBottomBorder(int bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    /**
     * Returns the border border value.
     * @return the border border value.
     * @see #setBottomBorder
     */
    public int getBottomBorder() {
        return bottomBorder;
    }

    /**
	 * Set the border paint.
	 * @param borderPaint the paint with which to fill the border
     * @see #getBorderPaint
	 */
    public void setBorderPaint(Paint borderPaint) {
        this.borderPaint = borderPaint;
    }

    /**
	 * Get the border paint.
	 * @return the paint with which to fill the border
     * @see #setBorderPaint
	 */
    public Paint getBorderPaint() {
        return borderPaint;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        Classfilter replacementClass = new  Classfilter(src, dst);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    public String toString() {
        return "Distort/Border...";
    }

    public class BorderFilterHelper0 {

        public BorderFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class BorderFilterHelper1 {

        public BorderFilterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class BorderFilterHelper2 {

        public BorderFilterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetLeftBorder {

        public ClassgetLeftBorder() {
        }

        public int doIt0() {
            return leftBorder;
        }
    }

    public class ClassgetRightBorder {

        public ClassgetRightBorder() {
        }

        public int doIt0() {
            return rightBorder;
        }
    }

    public class Classfilter {

        public Classfilter(BufferedImage src, BufferedImage dst) {
            this.src = src;
            this.dst = dst;
        }

        private BufferedImage src;

        private BufferedImage dst;

        private int width;

        private int height;

        private Graphics2D g;

        private BorderFilterHelper0 conditionObj0;

        private BorderFilterHelper1 conditionObj1;

        private BorderFilterHelper2 conditionObj2;

        public void doIt0() {
            width = src.getWidth();
            height = src.getHeight();
            if (dst == null)
                dst = new  BufferedImage(width + leftBorder + rightBorder, height + topBorder + bottomBorder, src.getType());
            g = dst.createGraphics();
            conditionObj0 = new  BorderFilterHelper0(0);
            conditionObj1 = new  BorderFilterHelper1(0);
            conditionObj2 = new  BorderFilterHelper2(0);
            if (borderPaint != null) {
                g.setPaint(borderPaint);
                if (leftBorder > 0)
                    g.fillRect(0, 0, leftBorder, height);
                if (rightBorder > conditionObj2.getValue())
                    g.fillRect(width - rightBorder, 0, rightBorder, height);
                if (topBorder > conditionObj1.getValue())
                    g.fillRect(leftBorder, 0, width - leftBorder - rightBorder, topBorder);
                if (bottomBorder > conditionObj0.getValue())
                    g.fillRect(leftBorder, height - bottomBorder, width - leftBorder - rightBorder, bottomBorder);
            }
        }

        public void doIt1() {
            g.drawRenderedImage(src, AffineTransform.getTranslateInstance(leftBorder, rightBorder));
        }

        public void doIt2() {
            g.dispose();
        }

        public BufferedImage doIt3() {
            return dst;
        }
    }
}
