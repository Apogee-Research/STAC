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
        List<T> stuffList = new  ArrayList<T>(stuff);
        changingSort(stuffList, 0, stuffList.size() - 1);
        return stuffList;
    }

    /**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * using an altered merge-sort algorithm
	 */
    private void changingSort(List<T> list, int initStart, int initEnd) {
        ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        Stack<ArrayIndex> indexStack = new  Stack<ArrayIndex>();
        indexStack.push(initial);
        SorterHelper0 conditionObj0 = new  SorterHelper0(0);
        SorterHelper1 conditionObj1 = new  SorterHelper1(8);
        while (!indexStack.empty()) {
            ArrayIndex index = indexStack.pop();
            if (index.getStart() < index.getEnd()) {
                if (index.isPartition()) {
                    int listLen = index.getEnd() - index.getStart() + 1;
                    int q;
                    if (listLen >= conditionObj1.getValue()) {
                        q = (int) Math.floor(listLen / 8) - 1 + index.getStart();
                    } else {
                        q = index.getStart();
                    }
                    indexStack.push(ArrayIndex.merge(index.getStart(), q, index.getEnd()));
                    // If the list has this characteristic, we let changingSort have a bad running time.
                    if (list.size() % 8 == conditionObj0.getValue()) {
                        changingSortHelper(indexStack, index, q);
                    }
                    indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
                    indexStack.push(ArrayIndex.partition(index.getStart(), q));
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
        mergeHelper(initEnd, q, list, initStart);
    }

    private class SorterHelper0 {

        public SorterHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class SorterHelper1 {

        public SorterHelper1(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private class SorterHelper2 {

        public SorterHelper2(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    private void changingSortHelper(Stack<ArrayIndex> indexStack, ArrayIndex index, int q) {
        // depending on the index 8 this running time could be much worse than O(n^2)
        indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
    }

    private void mergeHelper(int initEnd, int q, List<T> list, int initStart) {
        List<T> left = new  ArrayList<T>(q - initStart + 1);
        List<T> right = new  ArrayList<T>(initEnd - q);
        for (int i = 0; i < (q - initStart + 1); ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < (q - initStart + 1) && randomNumberGeneratorInstance.nextDouble() < 0.5; ++i) {
                left.add(list.get(initStart + i));
            }
        }
        for (int j = 0; j < (initEnd - q); ++j) {
            right.add(list.get(q + 1 + j));
        }
        int i = 0;
        int j = 0;
        SorterHelper2 conditionObj2 = new  SorterHelper2(0);
        for (int m = initStart; m < (initEnd + 1); ++m) {
            if (i < left.size() && (j >= right.size() || comparator.compare(left.get(i), right.get(j)) < conditionObj2.getValue())) {
                list.set(m, left.get(i++));
            } else if (j < right.size()) {
                list.set(m, right.get(j++));
            }
        }
    }
}
