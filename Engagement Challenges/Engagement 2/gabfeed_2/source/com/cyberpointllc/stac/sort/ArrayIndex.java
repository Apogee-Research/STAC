package com.cyberpointllc.stac.sort;


public class ArrayIndex {

    private static final int INVALID_MID = -1;

    private int startIndex;

    private int endIndex;

    private int midpoint;

    public static ArrayIndex partition(int startIndex, int endIndex) {
        return new  ArrayIndex(startIndex, INVALID_MID, endIndex);
    }

    public static ArrayIndex merge(int startIndex, int midpoint, int endIndex) {
        return new  ArrayIndex(startIndex, midpoint, endIndex);
    }

    private ArrayIndex(int startIndex, int midpoint, int endIndex) {
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.midpoint = midpoint;
    }

    public int getStart() {
        return startIndex;
    }

    public int getEnd() {
        return endIndex;
    }

    public int getMidpoint() {
        return midpoint;
    }

    public boolean isMerge() {
        return midpoint != INVALID_MID;
    }

    public boolean isPartition() {
        return !isMerge();
    }
}
