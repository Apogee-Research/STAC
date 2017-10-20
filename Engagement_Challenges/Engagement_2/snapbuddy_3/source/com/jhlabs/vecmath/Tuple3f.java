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
public class Tuple3f {

    public float x, y, z;

    public Tuple3f() {
        this(0, 0, 0);
    }

    public Tuple3f(float[] x) {
        this.x = x[0];
        this.y = x[1];
        this.z = x[2];
    }

    public Tuple3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Tuple3f(Tuple3f t) {
        this.x = t.x;
        this.y = t.y;
        this.z = t.z;
    }

    public void absolute() {
        Classabsolute replacementClass = new  Classabsolute();
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public void absolute(Tuple3f t) {
        x = Math.abs(t.x);
        y = Math.abs(t.y);
        z = Math.abs(t.z);
    }

    public void clamp(float min, float max) {
        Classclamp replacementClass = new  Classclamp(min, max);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float[] x) {
        this.x = x[0];
        this.y = x[1];
        this.z = x[2];
    }

    public void set(Tuple3f t) {
        Classset replacementClass = new  Classset(t);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public void get(Tuple3f t) {
        t.x = x;
        t.y = y;
        t.z = z;
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
    }

    public void negate(Tuple3f t) {
        x = -t.x;
        y = -t.y;
        z = -t.z;
    }

    public void interpolate(Tuple3f t, float alpha) {
        float a = 1 - alpha;
        x = a * x + alpha * t.x;
        y = a * y + alpha * t.y;
        z = a * z + alpha * t.z;
    }

    public void scale(float s) {
        Classscale replacementClass = new  Classscale(s);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public void add(Tuple3f t) {
        x += t.x;
        y += t.y;
        z += t.z;
    }

    public void add(Tuple3f t1, Tuple3f t2) {
        x = t1.x + t2.x;
        y = t1.y + t2.y;
        z = t1.z + t2.z;
    }

    public void sub(Tuple3f t) {
        x -= t.x;
        y -= t.y;
        z -= t.z;
    }

    public void sub(Tuple3f t1, Tuple3f t2) {
        x = t1.x - t2.x;
        y = t1.y - t2.y;
        z = t1.z - t2.z;
    }

    public void scaleAdd(float s, Tuple3f t) {
        x += s * t.x;
        y += s * t.y;
        z += s * t.z;
    }

    public void scaleAdd(float s, Tuple3f t1, Tuple3f t2) {
        ClassscaleAdd replacementClass = new  ClassscaleAdd(s, t1, t2);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    public class Classabsolute {

        public Classabsolute() {
        }

        public void doIt0() {
            x = Math.abs(x);
        }

        public void doIt1() {
            y = Math.abs(y);
            z = Math.abs(z);
        }
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
        }

        public void doIt2() {
            if (z < min)
                z = min;
            else if (z > max)
                z = max;
        }
    }

    public class Classset {

        public Classset(Tuple3f t) {
            this.t = t;
        }

        private Tuple3f t;

        public void doIt0() {
            x = t.x;
        }

        public void doIt1() {
            y = t.y;
            z = t.z;
        }
    }

    public class Classget {

        public Classget(float[] t) {
            this.t = t;
        }

        private float[] t;

        public void doIt0() {
            t[0] = x;
            t[1] = y;
        }

        public void doIt1() {
            t[2] = z;
        }
    }

    public class Classscale {

        public Classscale(float s) {
            this.s = s;
        }

        private float s;

        public void doIt0() {
            x *= s;
            y *= s;
        }

        public void doIt1() {
            z *= s;
        }
    }

    public class ClassscaleAdd {

        public ClassscaleAdd(float s, Tuple3f t1, Tuple3f t2) {
            this.s = s;
            this.t1 = t1;
            this.t2 = t2;
        }

        private float s;

        private Tuple3f t1;

        private Tuple3f t2;

        public void doIt0() {
            x = s * t1.x + t2.x;
        }

        public void doIt1() {
            y = s * t1.y + t2.y;
            z = s * t1.z + t2.z;
        }
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return "[" + x + ", " + y + ", " + z + "]";
        }
    }
}
