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

/**
 * A class for calulating the colors of the spectrum.
 */
public class Spectrum {

    private static int adjust(float color, float factor, float gamma) {
        if (color == 0.0)
            return 0;
        return (int) Math.round(255 * Math.pow(color * factor, gamma));
    }

    /**
     * Convert a wavelength to an RGB value.
	 * @param wavelength wavelength in nanometres
     * @return the RGB value
	 */
    public static int wavelengthToRGB(float wavelength) {
        ClasswavelengthToRGB replacementClass = new  ClasswavelengthToRGB(wavelength);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
        return replacementClass.doIt6();
    }

    public static class SpectrumHelper0 {

        public SpectrumHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class SpectrumHelper1 {

        public SpectrumHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class SpectrumHelper2 {

        public SpectrumHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public static class ClasswavelengthToRGB {

        public ClasswavelengthToRGB(float wavelength) {
            this.wavelength = wavelength;
        }

        private float wavelength;

        private float gamma;

        public void doIt0() {
            gamma = 0.80f;
        }

        private float r, g, b, factor;

        public void doIt1() {
        }

        private int w;

        public void doIt2() {
            w = (int) wavelength;
        }

        private SpectrumHelper0 conditionObj0;

        public void doIt3() {
            conditionObj0 = new  SpectrumHelper0(645);
        }

        private SpectrumHelper1 conditionObj1;

        public void doIt4() {
            conditionObj1 = new  SpectrumHelper1(780);
            if (w < 380) {
                r = 0.0f;
                g = 0.0f;
                b = 0.0f;
            } else if (w < 440) {
                r = -(wavelength - 440) / (440 - 380);
                g = 0.0f;
                b = 1.0f;
            } else if (w < 490) {
                r = 0.0f;
                g = (wavelength - 440) / (490 - 440);
                b = 1.0f;
            } else if (w < 510) {
                r = 0.0f;
                g = 1.0f;
                b = -(wavelength - 510) / (510 - 490);
            } else if (w < 580) {
                r = (wavelength - 510) / (580 - 510);
                g = 1.0f;
                b = 0.0f;
            } else if (w < conditionObj0.getValue()) {
                r = 1.0f;
                g = -(wavelength - 645) / (645 - 580);
                b = 0.0f;
            } else if (w <= conditionObj1.getValue()) {
                r = 1.0f;
                g = 0.0f;
                b = 0.0f;
            } else {
                r = 0.0f;
                g = 0.0f;
                b = 0.0f;
            }
        }

        private SpectrumHelper2 conditionObj2;

        public void doIt5() {
            conditionObj2 = new  SpectrumHelper2(419);
        }

        private int ir;

        private int ig;

        private int ib;

        public int doIt6() {
            // Let the intensity fall off near the vision limits
            if (380 <= w && w <= conditionObj2.getValue())
                factor = 0.3f + 0.7f * (wavelength - 380) / (420 - 380);
            else if (420 <= w && w <= 700)
                factor = 1.0f;
            else if (701 <= w && w <= 780)
                factor = 0.3f + 0.7f * (780 - wavelength) / (780 - 700);
            else
                factor = 0.0f;
            ir = adjust(r, factor, gamma);
            ig = adjust(g, factor, gamma);
            ib = adjust(b, factor, gamma);
            return 0xff000000 | (ir << 16) | (ig << 8) | ib;
        }
    }
}
