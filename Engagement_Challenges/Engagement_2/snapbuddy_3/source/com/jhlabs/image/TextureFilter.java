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

import java.awt.image.*;
import com.jhlabs.math.*;

public class TextureFilter extends PointFilter {

    private float scale = 32;

    private float stretch = 1.0f;

    private float angle = 0.0f;

    public float amount = 1.0f;

    public float turbulence = 1.0f;

    public float gain = 0.5f;

    public float bias = 0.5f;

    public int operation;

    private float m00 = 1.0f;

    private float m01 = 0.0f;

    private float m10 = 0.0f;

    private float m11 = 1.0f;

    private Colormap colormap = new  Gradient();

    private Function2D function = new  Noise();

    public TextureFilter() {
    }

    /**
	 * Set the amount of texture.
	 * @param amount the amount
     * @min-value 0
     * @max-value 1
     * @see #getAmount
	 */
    public void setAmount(float amount) {
        this.amount = amount;
    }

    /**
	 * Get the amount of texture.
	 * @return the amount
     * @see #setAmount
	 */
    public float getAmount() {
        return amount;
    }

    public void setFunction(Function2D function) {
        this.function = function;
    }

    public Function2D getFunction() {
        return function;
    }

    public void setOperation(int operation) {
        ClasssetOperation replacementClass = new  ClasssetOperation(operation);
        ;
        replacementClass.doIt0();
    }

    public int getOperation() {
        return operation;
    }

    /**
     * Specifies the scale of the texture.
     * @param scale the scale of the texture.
     * @min-value 1
     * @max-value 300+
     * @see #getScale
     */
    public void setScale(float scale) {
        this.scale = scale;
    }

    /**
     * Returns the scale of the texture.
     * @return the scale of the texture.
     * @see #setScale
     */
    public float getScale() {
        ClassgetScale replacementClass = new  ClassgetScale();
        ;
        return replacementClass.doIt0();
    }

    /**
     * Specifies the stretch factor of the texture.
     * @param stretch the stretch factor of the texture.
     * @min-value 1
     * @max-value 50+
     * @see #getStretch
     */
    public void setStretch(float stretch) {
        this.stretch = stretch;
    }

    /**
     * Returns the stretch factor of the texture.
     * @return the stretch factor of the texture.
     * @see #setStretch
     */
    public float getStretch() {
        return stretch;
    }

    /**
     * Specifies the angle of the texture.
     * @param angle the angle of the texture.
     * @angle
     * @see #getAngle
     */
    public void setAngle(float angle) {
        ClasssetAngle replacementClass = new  ClasssetAngle(angle);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
    }

    /**
     * Returns the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    /**
     * Specifies the turbulence of the texture.
     * @param turbulence the turbulence of the texture.
     * @min-value 0
     * @max-value 1
     * @see #getTurbulence
     */
    public void setTurbulence(float turbulence) {
        this.turbulence = turbulence;
    }

    /**
     * Returns the turbulence of the texture.
     * @return the turbulence of the texture.
     * @see #setTurbulence
     */
    public float getTurbulence() {
        return turbulence;
    }

    /**
     * Set the colormap to be used for the filter.
     * @param colormap the colormap
     * @see #getColormap
     */
    public void setColormap(Colormap colormap) {
        this.colormap = colormap;
    }

    /**
     * Get the colormap to be used for the filter.
     * @return the colormap
     * @see #setColormap
     */
    public Colormap getColormap() {
        ClassgetColormap replacementClass = new  ClassgetColormap();
        ;
        return replacementClass.doIt0();
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        return replacementClass.doIt4();
    }

    public String toString() {
        return "Texture/Noise...";
    }

    public class ClasssetOperation {

        public ClasssetOperation(int operation) {
            this.operation = operation;
        }

        private int operation;

        public void doIt0() {
            TextureFilter.this.operation = operation;
        }
    }

    public class ClassgetScale {

        public ClassgetScale() {
        }

        public float doIt0() {
            return scale;
        }
    }

    public class ClasssetAngle {

        public ClasssetAngle(float angle) {
            this.angle = angle;
        }

        private float angle;

        public void doIt0() {
            TextureFilter.this.angle = angle;
        }

        private float cos;

        private float sin;

        public void doIt1() {
            cos = (float) Math.cos(angle);
            sin = (float) Math.sin(angle);
            m00 = cos;
            m01 = sin;
            m10 = -sin;
        }

        public void doIt2() {
            m11 = cos;
        }
    }

    public class ClassgetColormap {

        public ClassgetColormap() {
        }

        public Colormap doIt0() {
            return colormap;
        }
    }

    public class ClassfilterRGB {

        public ClassfilterRGB(int x, int y, int rgb) {
            this.x = x;
            this.y = y;
            this.rgb = rgb;
        }

        private int x;

        private int y;

        private int rgb;

        private float nx;

        private float ny;

        private float f;

        public void doIt0() {
            nx = m00 * x + m01 * y;
            ny = m10 * x + m11 * y;
            nx /= scale;
            ny /= scale * stretch;
            f = turbulence == 1.0 ? Noise.noise2(nx, ny) : Noise.turbulence2(nx, ny, turbulence);
        }

        public void doIt1() {
            f = (f * 0.5f) + 0.5f;
            f = ImageMath.gain(f, gain);
        }

        public void doIt2() {
            f = ImageMath.bias(f, bias);
        }

        private int a;

        private int v;

        public void doIt3() {
            f *= amount;
            a = rgb & 0xff000000;
            if (colormap != null)
                v = colormap.getColor(f);
            else {
                v = PixelUtils.clamp((int) (f * 255));
                int r = v << 16;
                int g = v << 8;
                int b = v;
                v = a | r | g | b;
            }
        }

        public int doIt4() {
            if (operation != PixelUtils.REPLACE)
                v = PixelUtils.combinePixels(rgb, v, operation);
            return v;
        }
    }
}
