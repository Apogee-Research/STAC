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
import java.util.*;
import com.jhlabs.math.*;

/**
 * A filter which produces textures from fractal Brownian motion.
 */
public class FBMFilter extends PointFilter implements Cloneable {

    public static final int NOISE = 0;

    public static final int RIDGED = 1;

    public static final int VLNOISE = 2;

    public static final int SCNOISE = 3;

    public static final int CELLULAR = 4;

    private float scale = 32;

    private float stretch = 1.0f;

    private float angle = 0.0f;

    private float amount = 1.0f;

    private float H = 1.0f;

    private float octaves = 4.0f;

    private float lacunarity = 2.0f;

    private float gain = 0.5f;

    private float bias = 0.5f;

    private int operation;

    private float m00 = 1.0f;

    private float m01 = 0.0f;

    private float m10 = 0.0f;

    private float m11 = 1.0f;

    private float min;

    private float max;

    private Colormap colormap = new  Gradient();

    private boolean ridged;

    private FBM fBm;

    protected Random random = new  Random();

    private int basisType = NOISE;

    private Function2D basis;

    public FBMFilter() {
        setBasisType(NOISE);
    }

    /**
	 * Set the amount of effect.
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

    public void setOperation(int operation) {
        this.operation = operation;
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
        ClasssetStretch replacementClass = new  ClasssetStretch(stretch);
        ;
        replacementClass.doIt0();
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
        this.angle = angle;
        float cos = (float) Math.cos(this.angle);
        float sin = (float) Math.sin(this.angle);
        m00 = cos;
        m01 = sin;
        m10 = -sin;
        m11 = cos;
    }

    /**
     * Returns the angle of the texture.
     * @return the angle of the texture.
     * @see #setAngle
     */
    public float getAngle() {
        return angle;
    }

    public void setOctaves(float octaves) {
        this.octaves = octaves;
    }

    public float getOctaves() {
        return octaves;
    }

    public void setH(float H) {
        ClasssetH replacementClass = new  ClasssetH(H);
        ;
        replacementClass.doIt0();
    }

    public float getH() {
        return H;
    }

    public void setLacunarity(float lacunarity) {
        this.lacunarity = lacunarity;
    }

    public float getLacunarity() {
        return lacunarity;
    }

    public void setGain(float gain) {
        this.gain = gain;
    }

    public float getGain() {
        return gain;
    }

    public void setBias(float bias) {
        this.bias = bias;
    }

    public float getBias() {
        return bias;
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

    public void setBasisType(int basisType) {
        ClasssetBasisType replacementClass = new  ClasssetBasisType(basisType);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public int getBasisType() {
        return basisType;
    }

    public void setBasis(Function2D basis) {
        this.basis = basis;
    }

    public Function2D getBasis() {
        ClassgetBasis replacementClass = new  ClassgetBasis();
        ;
        return replacementClass.doIt0();
    }

    protected FBM makeFBM(float H, float lacunarity, float octaves) {
        ClassmakeFBM replacementClass = new  ClassmakeFBM(H, lacunarity, octaves);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        fBm = makeFBM(H, lacunarity, octaves);
        return super.filter(src, dst);
    }

    public int filterRGB(int x, int y, int rgb) {
        float nx = m00 * x + m01 * y;
        float ny = m10 * x + m11 * y;
        nx /= scale;
        ny /= scale * stretch;
        float f = fBm.evaluate(nx, ny);
        // Normalize to 0..1
        f = (f - min) / (max - min);
        f = ImageMath.gain(f, gain);
        f = ImageMath.bias(f, bias);
        f *= amount;
        int a = rgb & 0xff000000;
        int v;
        if (colormap != null)
            v = colormap.getColor(f);
        else {
            v = PixelUtils.clamp((int) (f * 255));
            int r = v << 16;
            int g = v << 8;
            int b = v;
            v = a | r | g | b;
        }
        if (operation != PixelUtils.REPLACE)
            v = PixelUtils.combinePixels(rgb, v, operation);
        return v;
    }

    public String toString() {
        return "Texture/Fractal Brownian Motion...";
    }

    public class ClassgetScale {

        public ClassgetScale() {
        }

        public float doIt0() {
            return scale;
        }
    }

    public class ClasssetStretch {

        public ClasssetStretch(float stretch) {
            this.stretch = stretch;
        }

        private float stretch;

        public void doIt0() {
            FBMFilter.this.stretch = stretch;
        }
    }

    public class ClasssetH {

        public ClasssetH(float H) {
            this.H = H;
        }

        private float H;

        public void doIt0() {
            FBMFilter.this.H = H;
        }
    }

    public class ClassgetColormap {

        public ClassgetColormap() {
        }

        public Colormap doIt0() {
            return colormap;
        }
    }

    public class ClasssetBasisType {

        public ClasssetBasisType(int basisType) {
            this.basisType = basisType;
        }

        private int basisType;

        public void doIt0() {
            FBMFilter.this.basisType = basisType;
        }

        public void doIt1() {
            switch(basisType) {
                default:
                case NOISE:
                    basis = new  Noise();
                    break;
                case RIDGED:
                    basis = new  RidgedFBM();
                    break;
                case VLNOISE:
                    basis = new  VLNoise();
                    break;
                case SCNOISE:
                    basis = new  SCNoise();
                    break;
                case CELLULAR:
                    basis = new  CellularFunction2D();
                    break;
            }
        }
    }

    public class ClassgetBasis {

        public ClassgetBasis() {
        }

        public Function2D doIt0() {
            return basis;
        }
    }

    protected class ClassmakeFBM {

        public ClassmakeFBM(float H, float lacunarity, float octaves) {
            this.H = H;
            this.lacunarity = lacunarity;
            this.octaves = octaves;
        }

        private float H;

        private float lacunarity;

        private float octaves;

        private FBM fbm;

        private float[] minmax;

        public void doIt0() {
            fbm = new  FBM(H, lacunarity, octaves, basis);
            minmax = Noise.findRange(fbm, null);
        }

        public FBM doIt1() {
            min = minmax[0];
            max = minmax[1];
            return fbm;
        }
    }
}
