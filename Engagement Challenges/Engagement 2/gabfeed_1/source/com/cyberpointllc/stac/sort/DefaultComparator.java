package com.cyberpointllc.stac.sort;

import java.util.Comparator;

/**
 * This orders by the objects' natural ordering
 */
public class DefaultComparator<T extends Comparable<? super T>> implements Comparator<T> {

    public static final DefaultComparator<String> STRING = new  DefaultComparator();

    @Override
    public int compare(T object1, T object2) {
        return object1.compareTo(object2);
    }
}
