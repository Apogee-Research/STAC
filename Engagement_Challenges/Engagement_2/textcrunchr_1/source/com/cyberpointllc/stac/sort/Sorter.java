package com.cyberpointllc.stac.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Sorter<T> {

    private final Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
     * return a List containing the elements of stuff, ordered by class T's natural ordering
     */
    public List<T> sort(Collection<T> stuff) {
        List<T> stuffList = new  ArrayList(stuff);
        Collections.sort(stuffList, comparator);
        return stuffList;
    }
}
