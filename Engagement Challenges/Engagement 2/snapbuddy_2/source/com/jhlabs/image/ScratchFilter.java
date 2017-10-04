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
import java.util.*;

public class ScratchFilter extends AbstractBufferedImageOp {

    private float density = 0.1f;

    private float angle;

    private float angleVariation = 1.0f;

    private float width = 0.5f;

    private float length = 0.5f;

    private int color = 0xffffffff;

    private int seed = 0;

    public ScratchFilter() {
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngleVariation(float angleVariation) {
        this.angleVariation = angleVariation;
    }

    public float getAngleVariation() {
        return angleVariation;
    }

    public void setDensity(float density) {
        setDensityHelper(density);
    }

    public float getDensity() {
        return density;
    }

    public void setLength(float length) {
        setLengthHelper(length);
    }

    public float getLength() {
        return length;
    }

    public void setWidth(float width) {
        setWidthHelper(width);
    }

    public float getWidth() {
        return width;
    }

    public void setColor(int color) {
        setColorHelper(color);
    }

    public int getColor() {
        return color;
    }

    public void setSeed(int seed) {
        setSeedHelper(seed);
    }

    public int getSeed() {
        return seed;
    }

    public BufferedImage filter(BufferedImage src, BufferedImage dst) {
        if (dst == null)
            dst = createCompatibleDestImage(src, null);
        int width = src.getWidth();
        int height = src.getHeight();
        int numScratches = (int) (density * width * height / 100);
        float l = length * width;
        Random random = new  Random(seed);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new  Color(color));
        g.setStroke(new  BasicStroke(this.width));
        for (int i = 0; i < numScratches; i++) {
            filterHelper(g, height, width, random, l);
        }
        g.dispose();
        return dst;
    }

    public String toString() {
        return "Render/Scratches...";
    }

    private void setDensityHelper(float density) {
        this.density = density;
    }

    private void setLengthHelper(float length) {
        this.length = length;
    }

    private void setWidthHelper(float width) {
        this.width = width;
    }

    private void setColorHelper(int color) {
        this.color = color;
    }

    private void setSeedHelper(int seed) {
        this.seed = seed;
    }

    private void filterHelper(Graphics2D g, int height, int width, Random random, float l) {
        float x = width * random.nextFloat();
        float y = height * random.nextFloat();
        float a = angle + ImageMath.TWO_PI * (angleVariation * (random.nextFloat() - 0.5f));
        float s = (float) Math.sin(a) * l;
        float c = (float) Math.cos(a) * l;
        float x1 = x - c;
        float y1 = y - s;
        float x2 = x + c;
        float y2 = y + s;
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }
}
