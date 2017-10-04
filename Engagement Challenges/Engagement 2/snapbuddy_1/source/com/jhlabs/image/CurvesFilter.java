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

public class CurvesFilter extends TransferFilter {

    private Curve[] curves = new Curve[1];

    public CurvesFilter() {
        curves = new Curve[3];
        curves[0] = new  Curve();
        curves[1] = new  Curve();
        curves[2] = new  Curve();
    }

    protected void initialize() {
        initializeHelper();
    }

    public void setCurve(Curve curve) {
        curves = new Curve[] { curve };
        initialized = false;
    }

    public void setCurves(Curve[] curves) {
        int conditionObj1 = 1;
        int conditionObj2 = 3;
        if (curves == null || (curves.length != conditionObj1 && curves.length != conditionObj2))
            throw new  IllegalArgumentException("Curves must be length 1 or 3");
        this.curves = curves;
        initialized = false;
    }

    public Curve[] getCurves() {
        return curves;
    }

    public String toString() {
        return "Colors/Curves...";
    }

    private void initializeHelper() {
        initialized = true;
        int conditionObj0 = 1;
        if (curves.length == conditionObj0)
            rTable = gTable = bTable = curves[0].makeTable();
        else {
            rTable = curves[0].makeTable();
            gTable = curves[1].makeTable();
            bTable = curves[2].makeTable();
        }
    }
}
