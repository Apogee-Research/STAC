package com.cyberpointllc.stac.sort;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;
import java.util.Random;

public class Sorter<T> {

    private final Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
    public List<T> sort(Collection<T> stuff) {
        Classsort replacementClass = new  Classsort(stuff);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    /**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * using an altered merge-sort algorithm
	 */
    private void changingSort(List<T> list, int initStart, int initEnd) {
        ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        Stack<ArrayIndex> indexStack = new  Stack<ArrayIndex>();
        indexStack.push(initial);
        while (!indexStack.empty()) {
            ArrayIndex index = indexStack.pop();
            if (index.getStart() < index.getEnd()) {
                if (index.isPartition()) {
                    int q1 = (int) Math.floor((index.getStart() + index.getEnd()) / 2);
                    int q2 = (int) Math.floor((q1 + 1 + index.getEnd()) / 2);
                    int q3 = (int) Math.floor((q2 + 1 + index.getEnd()) / 2);
                    int q4 = (int) Math.floor((q3 + 1 + index.getEnd()) / 2);
                    int q5 = (int) Math.floor((q4 + 1 + index.getEnd()) / 2);
                    indexStack.push(ArrayIndex.merge(index.getStart(), q1, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q1 + 1, q2, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q2 + 1, q3, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q3 + 1, q4, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q4 + 1, q5, index.getEnd()));
                    indexStack.push(ArrayIndex.partition(index.getStart(), q1));
                    indexStack.push(ArrayIndex.partition(q1 + 1, q2));
                    indexStack.push(ArrayIndex.partition(q2 + 1, q3));
                    indexStack.push(ArrayIndex.partition(q3 + 1, q4));
                    indexStack.push(ArrayIndex.partition(q4 + 1, q5));
                    indexStack.push(ArrayIndex.partition(q5 + 1, index.getEnd()));
                } else if (index.isMerge()) {
                    merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
                } else {
                    throw new  RuntimeException("Not merge or partition");
                }
            }
        }
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al
	 */
    private void merge(List<T> list, int initStart, int q, int initEnd) {
        List<T> left = new  ArrayList<T>(q - initStart + 1);
        List<T> right = new  ArrayList<T>(initEnd - q);
        for (int i = 0; i < (q - initStart + 1); ++i) {
            left.add(list.get(initStart + i));
        }
        for (int j = 0; j < (initEnd - q); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; j < (initEnd - q) && randomNumberGeneratorInstance.nextDouble() < 0.9; ++j) {
                right.add(list.get(q + 1 + j));
            }
        }
        int i = 0;
        int j = 0;
        int listLen = initEnd - initStart + 1;
        for (int m = initStart; m < (initEnd + 1); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; m < (initEnd + 1) && randomNumberGeneratorInstance.nextDouble() < 0.9; ++m) {
                /**
			 * This produces an unsorted list by always placing the right elements before the left elements
			 * at each level of recursion from the bottom level (merging two elements) to level where the 
			 * subarray being sorted is == max(2^10, 2^(k + 7)). The 2^10 is necessary to make sure the 
			 * array is sufficiently mixed up. Once the subarrays are longer than that, they are sorted normally,
			 * but they are sufficiently mixed that it doesn't matter.
			 */
                if (listLen <= Math.max(Math.pow(2, 10), Math.pow(2, 5 + 7))) {
                    if (i < left.size() && j < right.size()) {
                        list.set(m, right.get(j++));
                        ++m;
                        list.set(m, left.get(i++));
                    } else if (j < right.size()) {
                        list.set(m, right.get(j++));
                    } else if (i < left.size()) {
                        list.set(m, left.get(i++));
                    }
                } else if (i < left.size() && (j >= right.size() || comparator.compare(left.get(i), right.get(j)) < 0)) {
                    list.set(m, left.get(i++));
                } else if (j < right.size()) {
                    list.set(m, right.get(j++));
                }
            }
        }
    }

    /**
	 * Based on the quickSort in "Introduction to Algorithms" by Cormen, et al.
	 */
    private void quickSort(List<T> list, int initStart, int initEnd) {
        ClassquickSort replacementClass = new  ClassquickSort(list, initStart, initEnd);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
    }

    /**
	 * Based on the partition in "Introduction to Algorithms" by Cormen, et al.
	 */
    private int qsPartition(List<T> list, int initStart, int initEnd) {
        T pivot = list.get(initEnd);
        int i = initStart - 1;
        for (int j = initStart; j < initEnd; ++j) {
            if (comparator.compare(list.get(j), pivot) <= 0) {
                ++i;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, initEnd, i + 1);
        return i + 1;
    }

    public class Classsort {

        public Classsort(Collection<T> stuff) {
            this.stuff = stuff;
        }

        private Collection<T> stuff;

        private List<T> stuffList;

        public void doIt0() {
            stuffList = new  ArrayList<T>(stuff);
        }

        public void doIt1() {
            changingSort(stuffList, 0, stuffList.size() - 1);
            quickSort(stuffList, 0, stuffList.size() - 1);
        }

        public List<T> doIt2() {
            return stuffList;
        }
    }

    private class ClassquickSort {

        public ClassquickSort(List<T> list, int initStart, int initEnd) {
            this.list = list;
            this.initStart = initStart;
            this.initEnd = initEnd;
        }

        private List<T> list;

        private int initStart;

        private int initEnd;

        private ArrayIndex initial;

        private Stack<ArrayIndex> indexStack;

        public void doIt0() {
            initial = ArrayIndex.partition(initStart, initEnd);
            indexStack = new  Stack<ArrayIndex>();
        }

        public void doIt1() {
            indexStack.push(initial);
        }

        public void doIt2() {
            while (!indexStack.empty()) {
                ArrayIndex index = indexStack.pop();
                if (index.getStart() < index.getEnd()) {
                    if (index.isPartition()) {
                        int q = qsPartition(list, index.getStart(), index.getEnd());
                        indexStack.push(ArrayIndex.partition(index.getStart(), q - 1));
                        indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
                    }
                }
            }
        }
    }
}
