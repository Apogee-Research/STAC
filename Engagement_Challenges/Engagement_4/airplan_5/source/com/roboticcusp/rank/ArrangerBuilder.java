package com.roboticcusp.rank;

import java.util.Comparator;

public class ArrangerBuilder<T> {
    private Comparator<T> comparator;

    public ArrangerBuilder defineComparator(Comparator<T> comparator) {
        this.comparator = comparator;
        return this;
    }

    public Arranger composeArranger() {
        return new Arranger(comparator);
    }
}