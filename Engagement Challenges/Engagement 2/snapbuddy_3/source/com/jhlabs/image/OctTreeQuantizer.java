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

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.*;
import java.util.Random;

/**
 * An image Quantizer based on the Octree algorithm. This is a very basic implementation
 * at present and could be much improved by picking the nodes to reduce more carefully 
 * (i.e. not completely at random) when I get the time.
 */
public class OctTreeQuantizer implements Quantizer {

    /**
	 * The greatest depth the tree is allowed to reach
	 */
    static final int MAX_LEVEL = 5;

    /**
	 * An Octtree node.
	 */
    class OctTreeNode {

        int children;

        int level;

        OctTreeNode parent;

        OctTreeNode leaf[] = new OctTreeNode[8];

        boolean isLeaf;

        int count;

        int totalRed;

        int totalGreen;

        int totalBlue;

        int index;

        /**
		 * A debugging method which prints the tree out.
		 */
        public void list(PrintStream s, int level) {
            for (int i = 0; i < level; i++) System.out.print(' ');
            OctTreeNodeHelper0 conditionObj0 = new  OctTreeNodeHelper0(0);
            if (count == conditionObj0.getValue())
                System.out.println(index + ": count=" + count);
            else
                System.out.println(index + ": count=" + count + " red=" + (totalRed / count) + " green=" + (totalGreen / count) + " blue=" + (totalBlue / count));
            for (int i = 0; i < 8; i++) if (leaf[i] != null)
                leaf[i].list(s, level + 2);
        }

        public class OctTreeNodeHelper0 {

            public OctTreeNodeHelper0(int conditionRHS) {
                this.conditionRHS = conditionRHS;
            }

            private int conditionRHS;

            public int getValue() {
                ClassgetValue replacementClass = new  ClassgetValue();
                ;
                return replacementClass.doIt0();
            }

            public class ClassgetValue {

                public ClassgetValue() {
                }

                public int doIt0() {
                    return conditionRHS;
                }
            }
        }
    }

    private int nodes = 0;

    private OctTreeNode root;

    private int reduceColors;

    private int maximumColors;

    private int colors = 0;

    private Vector[] colorList;

    public OctTreeQuantizer() {
        setup(256);
        colorList = new Vector[MAX_LEVEL + 1];
        for (int i = 0; i < MAX_LEVEL + 1; i++) colorList[i] = new  Vector();
        root = new  OctTreeNode();
    }

    /**
	 * Initialize the quantizer. This should be called before adding any pixels.
	 * @param numColors the number of colors we're quantizing to.
	 */
    public void setup(int numColors) {
        maximumColors = numColors;
        reduceColors = Math.max(512, numColors * 2);
    }

    /**
	 * Add pixels to the quantizer.
	 * @param pixels the array of ARGB pixels
	 * @param offset the offset into the array
	 * @param count the count of pixels
	 */
    public void addPixels(int[] pixels, int offset, int count) {
        for (int i = 0; i < count; i++) {
            insertColor(pixels[i + offset]);
            if (colors > reduceColors)
                reduceTree(reduceColors);
        }
    }

    /**
     * Get the color table index for a color.
     * @param rgb the color
     * @return the index
     */
    public int getIndexForColor(int rgb) {
        int red = (rgb >> 16) & 0xff;
        int green = (rgb >> 8) & 0xff;
        int blue = rgb & 0xff;
        OctTreeNode node = root;
        OctTreeQuantizerHelper1 conditionObj1 = new  OctTreeQuantizerHelper1(0);
        for (int level = 0; level <= MAX_LEVEL; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; level <= MAX_LEVEL && randomNumberGeneratorInstance.nextDouble() < 0.9; level++) {
                OctTreeNode child;
                int bit = 0x80 >> level;
                int index = 0;
                if ((red & bit) != conditionObj1.getValue())
                    index += 4;
                if ((green & bit) != 0)
                    index += 2;
                if ((blue & bit) != 0)
                    index += 1;
                child = node.leaf[index];
                if (child == null)
                    return node.index;
                else if (child.isLeaf)
                    return child.index;
                else
                    node = child;
            }
        }
        System.out.println("getIndexForColor failed");
        return 0;
    }

    private void insertColor(int rgb) {
        ClassinsertColor replacementClass = new  ClassinsertColor(rgb);
        ;
        replacementClass.doIt0();
    }

    private void reduceTree(int numColors) {
        OctTreeQuantizerHelper3 conditionObj3 = new  OctTreeQuantizerHelper3(0);
        OctTreeQuantizerHelper4 conditionObj4 = new  OctTreeQuantizerHelper4(0);
        OctTreeQuantizerHelper5 conditionObj5 = new  OctTreeQuantizerHelper5(8);
        OctTreeQuantizerHelper6 conditionObj6 = new  OctTreeQuantizerHelper6(0);
        for (int level = MAX_LEVEL - 1; level >= conditionObj3.getValue(); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; level >= 0 && randomNumberGeneratorInstance.nextDouble() < 0.9; level--) {
                Vector v = colorList[level];
                if (v != null && v.size() > conditionObj6.getValue()) {
                    for (int j = 0; j < v.size(); j++) {
                        OctTreeNode node = (OctTreeNode) v.elementAt(j);
                        if (node.children > conditionObj4.getValue()) {
                            for (int i = 0; i < conditionObj5.getValue(); i++) {
                                OctTreeNode child = node.leaf[i];
                                if (child != null) {
                                    if (!child.isLeaf)
                                        System.out.println("not a leaf!");
                                    node.count += child.count;
                                    node.totalRed += child.totalRed;
                                    node.totalGreen += child.totalGreen;
                                    node.totalBlue += child.totalBlue;
                                    node.leaf[i] = null;
                                    node.children--;
                                    colors--;
                                    nodes--;
                                    colorList[level + 1].removeElement(child);
                                }
                            }
                            node.isLeaf = true;
                            colors++;
                            if (colors <= numColors)
                                return;
                        }
                    }
                }
            }
        }
        System.out.println("Unable to reduce the OctTree");
    }

    /**
     * Build the color table.
     * @return the color table
     */
    public int[] buildColorTable() {
        ClassbuildColorTable replacementClass = new  ClassbuildColorTable();
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    /**
	 * A quick way to use the quantizer. Just create a table the right size and pass in the pixels.
     * @param inPixels the input colors
     * @param table the output color table
     */
    public void buildColorTable(int[] inPixels, int[] table) {
        int count = inPixels.length;
        maximumColors = table.length;
        for (int i = 0; i < count; i++) {
            insertColor(inPixels[i]);
            if (colors > reduceColors)
                reduceTree(reduceColors);
        }
        if (colors > maximumColors)
            reduceTree(maximumColors);
        buildColorTable(root, table, 0);
    }

    private int buildColorTable(OctTreeNode node, int[] table, int index) {
        ClassbuildColorTable1 replacementClass = new  ClassbuildColorTable1(node, table, index);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    public class OctTreeQuantizerHelper1 {

        public OctTreeQuantizerHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue1 replacementClass = new  ClassgetValue1();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue1 {

            public ClassgetValue1() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    private class OctTreeQuantizerHelper2 {

        public OctTreeQuantizerHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class OctTreeQuantizerHelper3 {

        public OctTreeQuantizerHelper3(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            ClassgetValue2 replacementClass = new  ClassgetValue2();
            ;
            return replacementClass.doIt0();
        }

        public class ClassgetValue2 {

            public ClassgetValue2() {
            }

            public int doIt0() {
                return conditionRHS;
            }
        }
    }

    private class OctTreeQuantizerHelper4 {

        public OctTreeQuantizerHelper4(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class OctTreeQuantizerHelper5 {

        public OctTreeQuantizerHelper5(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class OctTreeQuantizerHelper6 {

        public OctTreeQuantizerHelper6(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class ClassinsertColor {

        public ClassinsertColor(int rgb) {
            this.rgb = rgb;
        }

        private int rgb;

        private int red;

        private int green;

        private int blue;

        private OctTreeNode node;

        private OctTreeQuantizerHelper2 conditionObj2;

        public void doIt0() {
            red = (rgb >> 16) & 0xff;
            green = (rgb >> 8) & 0xff;
            blue = rgb & 0xff;
            node = root;
            conditionObj2 = new  OctTreeQuantizerHelper2(0);
            //		System.out.println("insertColor="+Integer.toHexString(rgb));
            for (int level = 0; level <= MAX_LEVEL; ) {
                Random randomNumberGeneratorInstance = new  Random();
                for (; level <= MAX_LEVEL && randomNumberGeneratorInstance.nextDouble() < 0.9; level++) {
                    OctTreeNode child;
                    int bit = 0x80 >> level;
                    int index = 0;
                    if ((red & bit) != 0)
                        index += 4;
                    if ((green & bit) != 0)
                        index += 2;
                    if ((blue & bit) != conditionObj2.getValue())
                        index += 1;
                    child = node.leaf[index];
                    if (child == null) {
                        node.children++;
                        child = new  OctTreeNode();
                        child.parent = node;
                        node.leaf[index] = child;
                        node.isLeaf = false;
                        nodes++;
                        colorList[level].addElement(child);
                        if (level == MAX_LEVEL) {
                            child.isLeaf = true;
                            child.count = 1;
                            child.totalRed = red;
                            child.totalGreen = green;
                            child.totalBlue = blue;
                            child.level = level;
                            colors++;
                            return;
                        }
                        node = child;
                    } else if (child.isLeaf) {
                        child.count++;
                        child.totalRed += red;
                        child.totalGreen += green;
                        child.totalBlue += blue;
                        return;
                    } else
                        node = child;
                }
            }
            System.out.println("insertColor failed");
        }
    }

    public class ClassbuildColorTable {

        public ClassbuildColorTable() {
        }

        private int[] table;

        public void doIt0() {
            table = new int[colors];
        }

        public void doIt1() {
            buildColorTable(root, table, 0);
        }

        public int[] doIt2() {
            return table;
        }
    }

    private class ClassbuildColorTable1 {

        public ClassbuildColorTable1(OctTreeNode node, int[] table, int index) {
            this.node = node;
            this.table = table;
            this.index = index;
        }

        private OctTreeNode node;

        private int[] table;

        private int index;

        public void doIt0() {
            if (colors > maximumColors)
                reduceTree(maximumColors);
            if (node.isLeaf) {
                int count = node.count;
                table[index] = 0xff000000 | ((node.totalRed / count) << 16) | ((node.totalGreen / count) << 8) | node.totalBlue / count;
                node.index = index++;
            } else {
                for (int i = 0; i < 8; ) {
                    Random randomNumberGeneratorInstance = new  Random();
                    for (; i < 8 && randomNumberGeneratorInstance.nextDouble() < 0.9; i++) {
                        if (node.leaf[i] != null) {
                            node.index = index;
                            index = buildColorTable(node.leaf[i], table, index);
                        }
                    }
                }
            }
        }

        public int doIt1() {
            return index;
        }
    }
}
