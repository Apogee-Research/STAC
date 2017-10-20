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
 * A Filter which distorts an image by twisting it from the centre out.
 * The twisting is centred at the centre of the image and extends out to the smallest of
 * the width and height. Pixels outside this radius are unaffected.
 */
public class TwirlFilter extends TransformFilter {

    private float angle = 0;

    private float centreX = 0.5f;

    private float centreY = 0.5f;

    private float radius = 100;

    private float radius2 = 0;

    private float icentreX;

    private float icentreY;

    /**
	 * Construct a TwirlFilter with no distortion.
	 */
    public TwirlFilter() {
        setEdgeAction(CLAMP);
    }

    /**
	 * Set the angle of twirl in radians. 0 means no distortion.
	 * @param angle the angle of twirl. This is the angle by which pixels at the nearest edge of the image will move.
     * @see #getAngle
	 */
    public void setAngle(float angle) {
        ClasssetAngle replacementClass = new  ClasssetAngle(angle);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the angle of twist.
	 * @return the angle in radians.
     * @see #setAngle
	 */
    public float getAngle() {
        return angle;
    }

    /**
	 * Set the centre of the effect in the X direction as a proportion of the image size.
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
        ClassgetCentreX replacementClass = new  ClassgetCentreX();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set the centre of the effect in the Y direction as a proportion of the image size.
	 * @param centreY the center
     * @see #getCentreY
	 */
    public void setCentreY(float centreY) {
        this.centreY = centreY;
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

    /**
	 * Set the radius of the effect.
	 * @param radius the radius
     * @min-value 0
     * @see #getRadius
	 */
    public void setRadius(float radius) {
        this.radius = radius;
    }

    /**
	 * Get the radius of the effect.
	 * @return the radius
     * @see #setRadius
	 */
    public float getRadius() {
        return radius;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        icentreX = src.getWidth() * centreX;
        icentreY = src.getHeight() * centreY;
        TwirlFilterHelper0 conditionObj0 = new  TwirlFilterHelper0(0);
        if (radius == conditionObj0.getValue())
            radius = Math.min(icentreX, icentreY);
        radius2 = radius * radius;
        return super.filter(src, dst);
    }

    protected void transformInverse(int x, int y, float[] out) {
        float dx = x - icentreX;
        float dy = y - icentreY;
        float distance = dx * dx + dy * dy;
        if (distance > radius2) {
            out[0] = x;
            out[1] = y;
        } else {
            distance = (float) Math.sqrt(distance);
            float a = (float) Math.atan2(dy, dx) + angle * (radius - distance) / radius;
            out[0] = icentreX + distance * (float) Math.cos(a);
            out[1] = icentreY + distance * (float) Math.sin(a);
        }
    }

    public String toString() {
        return "Distort/Twirl...";
    }

    public class TwirlFilterHelper0 {

        public TwirlFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue replacementClass = new  ClassgetValue();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue {

            public ClassgetValue() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    public class ClasssetAngle {

        public ClasssetAngle(float angle) {
            this.angle = angle;
        }

        private float angle;

        public void doIt0() {
            TwirlFilter.this.angle = angle;
        }
    }

    public class ClassgetCentreX {

        public ClassgetCentreX() {
        }

        public float doIt0() {
            return centreX;
        }
    }

    public class ClassgetCentre {

        public ClassgetCentre() {
        }

        public Point2D doIt0() {
            return new  Point2D.Float(centreX, centreY);
        }
    }
}
