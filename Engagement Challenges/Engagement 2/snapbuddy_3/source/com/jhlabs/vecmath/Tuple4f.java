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
package com.jhlabs.vecmath;

/**
 * Vector math package, converted to look similar to javax.vecmath.
 */
public class Tuple4f {

    public float x, y, z, w;

    public Tuple4f() {
        this(0, 0, 0, 0);
    }

    public Tuple4f(float[] x) {
        this.x = x[0];
        this.y = x[1];
        this.z = x[2];
        this.w = x[2];
    }

    public Tuple4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Tuple4f(Tuple4f t) {
        this.x = t.x;
        this.y = t.y;
        this.z = t.z;
        this.w = t.w;
    }

    public void absolute() {
        x = Math.abs(x);
        y = Math.abs(y);
        z = Math.abs(z);
        w = Math.abs(w);
    }

    public void absolute(Tuple4f t) {
        x = Math.abs(t.x);
        y = Math.abs(t.y);
        z = Math.abs(t.z);
        w = Math.abs(t.w);
    }

    public void clamp(float min, float max) {
        Classclamp replacementClass = new  Classclamp(min, max);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public void set(float[] x) {
        this.x = x[0];
        this.y = x[1];
        this.z = x[2];
        this.w = x[2];
    }

    public void set(Tuple4f t) {
        x = t.x;
        y = t.y;
        z = t.z;
        w = t.w;
    }

    public void get(Tuple4f t) {
        t.x = x;
        t.y = y;
        t.z = z;
        t.w = w;
    }

    public void get(float[] t) {
        Classget replacementClass = new  Classget(t);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public void negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
    }

    public void negate(Tuple4f t) {
        x = -t.x;
        y = -t.y;
        z = -t.z;
        w = -t.w;
    }

    public void interpolate(Tuple4f t, float alpha) {
        float a = 1 - alpha;
        x = a * x + alpha * t.x;
        y = a * y + alpha * t.y;
        z = a * z + alpha * t.z;
        w = a * w + alpha * t.w;
    }

    public void scale(float s) {
        x *= s;
        y *= s;
        z *= s;
        w *= s;
    }

    public void add(Tuple4f t) {
        x += t.x;
        y += t.y;
        z += t.z;
        w += t.w;
    }

    public void add(Tuple4f t1, Tuple4f t2) {
        x = t1.x + t2.x;
        y = t1.y + t2.y;
        z = t1.z + t2.z;
        w = t1.w + t2.w;
    }

    public void sub(Tuple4f t) {
        x -= t.x;
        y -= t.y;
        z -= t.z;
        w -= t.w;
    }

    public void sub(Tuple4f t1, Tuple4f t2) {
        Classsub replacementClass = new  Classsub(t1, t2);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    public class Classclamp {

        public Classclamp(float min, float max) {
            this.min = min;
            this.max = max;
        }

        private float min;

        private float max;

        public void doIt0() {
            if (x < min)
                x = min;
            else if (x > max)
                x = max;
        }

        public void doIt1() {
            if (y < min)
                y = min;
            else if (y > max)
                y = max;
            if (z < min)
                z = min;
            else if (z > max)
                z = max;
            if (w < min)
                w = min;
            else if (w > max)
                w = max;
        }
    }

    public class Classget {

        public Classget(float[] t) {
            this.t = t;
        }

        private float[] t;

        public void doIt0() {
            t[0] = x;
        }

        public void doIt1() {
            t[1] = y;
            t[2] = z;
            t[3] = w;
        }
    }

    public class Classsub {

        public Classsub(Tuple4f t1, Tuple4f t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        private Tuple4f t1;

        private Tuple4f t2;

        public void doIt0() {
            x = t1.x - t2.x;
            y = t1.y - t2.y;
            z = t1.z - t2.z;
        }

        public void doIt1() {
            w = t1.w - t2.w;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "[" + x + ", " + y + ", " + z + ", " + w + "]";
        }
    }
}
