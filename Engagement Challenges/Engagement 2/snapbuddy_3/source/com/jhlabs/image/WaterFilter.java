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
import com.jhlabs.math.*;

/**
 * A filter which produces a water ripple distortion.
 */
public class WaterFilter extends TransformFilter {

    private float wavelength = 16;

    private float amplitude = 10;

    private float phase = 0;

    private float centreX = 0.5f;

    private float centreY = 0.5f;

    private float radius = 50;

    private float radius2 = 0;

    private float icentreX;

    private float icentreY;

    public WaterFilter() {
        setEdgeAction(CLAMP);
    }

    /**
	 * Set the wavelength of the ripples.
	 * @param wavelength the wavelength
     * @see #getWavelength
	 */
    public void setWavelength(float wavelength) {
        this.wavelength = wavelength;
    }

    /**
	 * Get the wavelength of the ripples.
	 * @return the wavelength
     * @see #setWavelength
	 */
    public float getWavelength() {
        ClassgetWavelength replacementClass = new  ClassgetWavelength();
        ;
        return replacementClass.doIt0();
    }

    /**
	 * Set the amplitude of the ripples.
	 * @param amplitude the amplitude
     * @see #getAmplitude
	 */
    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    /**
	 * Get the amplitude of the ripples.
	 * @return the amplitude
     * @see #setAmplitude
	 */
    public float getAmplitude() {
        return amplitude;
    }

    /**
	 * Set the phase of the ripples.
	 * @param phase the phase
     * @see #getPhase
	 */
    public void setPhase(float phase) {
        ClasssetPhase replacementClass = new  ClasssetPhase(phase);
        ;
        replacementClass.doIt0();
    }

    /**
	 * Get the phase of the ripples.
	 * @return the phase
     * @see #setPhase
	 */
    public float getPhase() {
        return phase;
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
        return centreX;
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
        ClasssetCentre replacementClass = new  ClasssetCentre(centre);
        ;
        replacementClass.doIt0();
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

    private boolean inside(int v, int a, int b) {
        return a <= v && v <= b;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        icentreX = src.getWidth() * centreX;
        icentreY = src.getHeight() * centreY;
        if (radius == 0)
            radius = Math.min(icentreX, icentreY);
        radius2 = radius * radius;
        return super.filter(src, dst);
    }

    protected void transformInverse(int x, int y, float[] out) {
        float dx = x - icentreX;
        float dy = y - icentreY;
        float distance2 = dx * dx + dy * dy;
        WaterFilterHelper0 conditionObj0 = new  WaterFilterHelper0(0);
        if (distance2 > radius2) {
            out[0] = x;
            out[1] = y;
        } else {
            float distance = (float) Math.sqrt(distance2);
            float amount = amplitude * (float) Math.sin(distance / wavelength * ImageMath.TWO_PI - phase);
            amount *= (radius - distance) / radius;
            if (distance != conditionObj0.getValue())
                amount *= wavelength / distance;
            out[0] = x + dx * amount;
            out[1] = y + dy * amount;
        }
    }

    public String toString() {
        return "Distort/Water Ripples...";
    }

    protected class WaterFilterHelper0 {

        public WaterFilterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class ClassgetWavelength {

        public ClassgetWavelength() {
        }

        public float doIt0() {
            return wavelength;
        }
    }

    public class ClasssetPhase {

        public ClasssetPhase(float phase) {
            this.phase = phase;
        }

        private float phase;

        public void doIt0() {
            WaterFilter.this.phase = phase;
        }
    }

    public class ClasssetCentre {

        public ClasssetCentre(Point2D centre) {
            this.centre = centre;
        }

        private Point2D centre;

        public void doIt0() {
            WaterFilter.this.centreX = (float) centre.getX();
            WaterFilter.this.centreY = (float) centre.getY();
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
