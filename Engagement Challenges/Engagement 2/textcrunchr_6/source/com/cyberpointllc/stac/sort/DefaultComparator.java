package com.cyberpointllc.stac.sort;

import java.util.Comparator;

/**
 * This orders by the objects' natural ordering
 */
public class DefaultComparator<T extends Comparable<? super T>> implements Comparator<T> {

    public static final DefaultComparator<String> STRING = new  DefaultComparator();

    @Override
    public int compare(T object1, T object2) {
        Classcompare replacementClass = new  Classcompare(object1, object2);
        ;
        return replacementClass.doIt0();
    }

    public class Classcompare {

        public Classcompare(T object1, T object2) {
            this.object1 = object1;
            this.object2 = object2;
        }

        private T object1;

        private T object2;

        public int doIt0() {
            return object1.compareTo(object2);
        }
    }
}
