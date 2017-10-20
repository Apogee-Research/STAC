package com.cyberpointllc.stac.sort;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

public class Sorter<T> {

    private final Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    /**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
    public List<T> sort(Collection<T> stuff) {
        List<T> stuffList = new  ArrayList<T>(stuff);
        changingSort(stuffList, 0, stuffList.size() - 1);
        return stuffList;
    }

    /**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * using an altered merge-sort algorithm
	 */
    private void changingSort(List<T> list, int initStart, int initEnd) {
        changingSortHelper(initEnd, list, initStart);
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al
	 */
    private void merge(List<T> list, int initStart, int q, int initEnd) {
        mergeHelper(initEnd, q, list, initStart);
    }

    private void changingSortHelper(int initEnd, List<T> list, int initStart) {
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
                    // calculate the length of the list mod k
                    int listModK = list.size() % 3;
                    // causes the first partition to be 3/4ths of the list instead of 1/2 of the list IF listModK == 0.
                    int potentialQ1 = q1 + (index.getEnd() - q2);
                    q1 = (int) (q1 * Math.ceil(listModK / 3.0) + potentialQ1 * (1 - Math.ceil(listModK / 3.0)));
                    indexStack.push(ArrayIndex.merge(index.getStart(), q1, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q1 + 1, q2, index.getEnd()));
                    indexStack.push(ArrayIndex.merge(q2 + 1, q3, index.getEnd()));
                    // Add an extra partition to help demonstrate bad running time if listModK == 0
                    indexStack.push(ArrayIndex.partition(index.getStart(), q1));
                    indexStack.push(ArrayIndex.partition(index.getStart(), q1));
                    indexStack.push(ArrayIndex.partition(q1 + 1, q2));
                    indexStack.push(ArrayIndex.partition(q2 + 1, q3));
                    indexStack.push(ArrayIndex.partition(q3 + 1, index.getEnd()));
                } else if (index.isMerge()) {
                    merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
                } else {
                    throw new  RuntimeException("Not merge or partition");
                }
            }
        }
    }

    private void mergeHelper(int initEnd, int q, List<T> list, int initStart) {
        List<T> left = new  ArrayList<T>(q - initStart + 1);
        List<T> right = new  ArrayList<T>(initEnd - q);
        for (int i = 0; i < (q - initStart + 1); ++i) {
            left.add(list.get(initStart + i));
        }
        for (int j = 0; j < (initEnd - q); ++j) {
            right.add(list.get(q + 1 + j));
        }
        int i = 0;
        int j = 0;
        int conditionObj0 = 0;
        for (int m = initStart; m < (initEnd + 1); ++m) {
            if (i < left.size() && (j >= right.size() || comparator.compare(left.get(i), right.get(j)) < conditionObj0)) {
                list.set(m, left.get(i++));
            } else if (j < right.size()) {
                list.set(m, right.get(j++));
            }
        }
    }
}
