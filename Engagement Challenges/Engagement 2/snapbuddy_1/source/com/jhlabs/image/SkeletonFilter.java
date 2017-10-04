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
 * A filter which reduces a binary image to a skeleton.
 *
 * Based on an algorithm by Zhang and Suen (CACM, March 1984, 236-239).
 */
public class SkeletonFilter extends BinaryFilter {

    private static final byte[] skeletonTable = { 0, 0, 0, 1, 0, 0, 1, 3, 0, 0, 3, 1, 1, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 2, 0, 3, 0, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 3, 0, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 3, 0, 2, 0, 0, 1, 3, 1, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 3, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 1, 3, 0, 0, 1, 3, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 0, 1, 0, 0, 0, 0, 2, 2, 0, 0, 2, 0, 0, 0 };

    public SkeletonFilter() {
        newColor = 0xffffffff;
    }

    protected int[] filterPixels(int width, int height, int[] inPixels, Rectangle transformedSpace) {
        int[] outPixels = new int[width * height];
        int count = 0;
        int black = 0xff000000;
        int white = 0xffffffff;
        int conditionObj0 = 0;
        int conditionObj1 = 2;
        int conditionObj2 = 1;
        int conditionObj3 = 1;
        int conditionObj4 = 3;
        int conditionObj5 = 2;
        int conditionObj6 = 3;
        for (int i = 0; i < iterations; i++) {
            count = 0;
            for (int pass = 0; pass < conditionObj1; pass++) {
                for (int y = 1; y < height - 1; y++) {
                    int offset = y * width + 1;
                    for (int x = 1; x < width - 1; x++) {
                        int pixel = inPixels[offset];
                        if (pixel == black) {
                            int tableIndex = 0;
                            if (inPixels[offset - width - 1] == black)
                                tableIndex |= 1;
                            if (inPixels[offset - width] == black)
                                tableIndex |= 2;
                            if (inPixels[offset - width + 1] == black)
                                tableIndex |= 4;
                            if (inPixels[offset + 1] == black)
                                tableIndex |= 8;
                            if (inPixels[offset + width + 1] == black)
                                tableIndex |= 16;
                            if (inPixels[offset + width] == black)
                                tableIndex |= 32;
                            if (inPixels[offset + width - 1] == black)
                                tableIndex |= 64;
                            if (inPixels[offset - 1] == black)
                                tableIndex |= 128;
                            int code = skeletonTable[tableIndex];
                            if (pass == conditionObj2) {
                                if (code == conditionObj5 || code == conditionObj6) {
                                    if (colormap != null)
                                        pixel = colormap.getColor((float) i / iterations);
                                    else
                                        pixel = newColor;
                                    count++;
                                }
                            } else {
                                if (code == conditionObj3 || code == conditionObj4) {
                                    if (colormap != null)
                                        pixel = colormap.getColor((float) i / iterations);
                                    else
                                        pixel = newColor;
                                    count++;
                                }
                            }
                        }
                        outPixels[offset++] = pixel;
                    }
                }
                if (pass == 0) {
                    inPixels = outPixels;
                    outPixels = new int[width * height];
                }
            }
            if (count == conditionObj0)
                break;
        }
        return outPixels;
    }

    public String toString() {
        return "Binary/Skeletonize...";
    }
}
