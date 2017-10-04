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
package com.jhlabs.math;

import java.util.Random;

public class TurbulenceFunction extends CompoundFunction2D {

    private float octaves;

    public TurbulenceFunction(Function2D basis, float octaves) {
        super(basis);
        this.octaves = octaves;
    }

    public void setOctaves(float octaves) {
        this.octaves = octaves;
    }

    public float getOctaves() {
        return octaves;
    }

    public float evaluate(float x, float y) {
        Classevaluate replacementClass = new  Classevaluate(x, y);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public class Classevaluate {

        public Classevaluate(float x, float y) {
            this.x = x;
            this.y = y;
        }

        private float x;

        private float y;

        private float t;

        public void doIt0() {
            t = 0.0f;
        }

        public float doIt1() {
            for (float f = 1.0f; f <= octaves; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; f <= octaves && randomNumberGeneratorInstance.nextDouble() < 0.9; f *= 2) t += Math.abs(basis.evaluate(f * x, f * y)) / f;
            }
            return t;
        }
    }
}
