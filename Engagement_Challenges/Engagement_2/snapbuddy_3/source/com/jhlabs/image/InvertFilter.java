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

/**
 * A filter which inverts the RGB channels of an image.
 */
public class InvertFilter extends PointFilter {

    public InvertFilter() {
        canFilterIndexColorModel = true;
    }

    public int filterRGB(int x, int y, int rgb) {
        ClassfilterRGB replacementClass = new  ClassfilterRGB(x, y, rgb);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
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

        private int a;

        public void doIt0() {
            a = rgb & 0xff000000;
        }

        public int doIt1() {
            return a | (~rgb & 0x00ffffff);
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "Colors/Invert";
        }
    }
}
