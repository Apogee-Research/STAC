package com.networkapex.nnsoft.trudeau.collections.fibonacciheap;

import java.util.Comparator;

public class FibonacciHeapBuilder<E> {
    private Comparator<? super E> comparator = null;

    public FibonacciHeapBuilder setComparator(Comparator<? super E> comparator) {
        this.comparator = comparator;
        return this;
    }

    public FibonacciHeap generateFibonacciHeap() {
        return new FibonacciHeap(comparator);
    }
}