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
import java.awt.image.*;

/**
 * A filter which unpremultiplies an image's alpha.
 * Note: this does not change the image type of the BufferedImage
 */
public class UnpremultiplyFilter extends PointFilter {

    public UnpremultiplyFilter() {
    }

    public int filterRGB(int x, int y, int rgb) {
        int a = (rgb >> 24) & 0xff;
        int r = (rgb >> 16) & 0xff;
        int g = (rgb >> 8) & 0xff;
        int b = rgb & 0xff;
        int conditionObj0 = 0;
        int conditionObj1 = 255;
        if (a == conditionObj0 || a == conditionObj1)
            return rgb;
        float f = 255.0f / a;
        r *= f;
        g *= f;
        b *= f;
        int conditionObj2 = 255;
        if (r > conditionObj2)
            r = 255;
        int conditionObj3 = 255;
        if (g > conditionObj3)
            g = 255;
        int conditionObj4 = 255;
        if (b > conditionObj4)
            b = 255;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public String toString() {
        return "Alpha/Unpremultiply";
    }
}
