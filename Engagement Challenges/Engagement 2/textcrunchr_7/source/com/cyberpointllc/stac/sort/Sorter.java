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
        ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        Stack<ArrayIndex> indexStack = new  Stack<ArrayIndex>();
        indexStack.push(initial);
        while (!indexStack.empty()) {
            ArrayIndex index = indexStack.pop();
            if (index.getStart() < index.getEnd()) {
                changingSortHelper(indexStack, index, list);
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
        for (Sorter.MyValueWrapper iTheOriginalOne = new  Sorter.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < (q - initStart + 1); i.increment()) {
            mergeHelper(list, left, initStart, i);
        }
        for (Sorter.MyValueWrapper jTheOriginalOne = new  Sorter.MyValueWrapper(0), j = jTheOriginalOne; jTheOriginalOne.getValue() < (initEnd - q); j.increment()) {
            {
                right.add(list.get(q + 1 + (int) j.getValue()));
            }
            j.foo();
        }
        int i = 0;
        int j = 0;
        SorterHelper0 conditionObj0 = new  SorterHelper0(0);
        for (int m = initStart; m < (initEnd + 1); ++m) {
            if (i < left.size() && (j >= right.size() || comparator.compare(left.get(i), right.get(j)) < conditionObj0.getValue())) {
                list.set(m, left.get(i++));
            } else if (j < right.size()) {
                list.set(m, right.get(j++));
            }
        }
    }

    public static class MyValueWrapper {

        private float val;

        public MyValueWrapper(float init_val) {
            val = init_val;
        }

        public void increment() {
            val++;
        }

        public void decrement() {
            decrementHelper();
        }

        public float getValue() {
            return val;
        }

        public void setValue(float value) {
            setValueHelper(value);
        }

        public void setValue(int value) {
            setValueHelper1(value);
        }

        public void foo() {
            fooHelper();
        }

        private void decrementHelper() {
            val--;
        }

        private void setValueHelper(float value) {
            val = value;
        }

        private void setValueHelper1(int value) {
            val = value;
        }

        private void fooHelper() {
            val /= 1.0;
        }
    }

    private void changingSortHelper(Stack<ArrayIndex> indexStack, ArrayIndex index, List<T> list) {
        if (index.isPartition()) {
            int q1 = (int) Math.floor((index.getStart() + index.getEnd()) / 2);
            int q2 = (int) Math.floor((q1 + 1 + index.getEnd()) / 2);
            int q3 = (int) Math.floor((q2 + 1 + index.getEnd()) / 2);
            int q4 = (int) Math.floor((q3 + 1 + index.getEnd()) / 2);
            int q5 = (int) Math.floor((q4 + 1 + index.getEnd()) / 2);
            int q6 = (int) Math.floor((q5 + 1 + index.getEnd()) / 2);
            int q7 = (int) Math.floor((q6 + 1 + index.getEnd()) / 2);
            indexStack.push(ArrayIndex.merge(index.getStart(), q1, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q1 + 1, q2, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q2 + 1, q3, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q3 + 1, q4, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q4 + 1, q5, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q5 + 1, q6, index.getEnd()));
            indexStack.push(ArrayIndex.merge(q6 + 1, q7, index.getEnd()));
            indexStack.push(ArrayIndex.partition(index.getStart(), q1));
            indexStack.push(ArrayIndex.partition(q1 + 1, q2));
            indexStack.push(ArrayIndex.partition(q2 + 1, q3));
            indexStack.push(ArrayIndex.partition(q3 + 1, q4));
            indexStack.push(ArrayIndex.partition(q4 + 1, q5));
            indexStack.push(ArrayIndex.partition(q5 + 1, q6));
            indexStack.push(ArrayIndex.partition(q6 + 1, q7));
            indexStack.push(ArrayIndex.partition(q7 + 1, index.getEnd()));
        } else if (index.isMerge()) {
            merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
        } else {
            throw new  RuntimeException("Not merge or partition");
        }
    }

    private void mergeHelper(List<T> list, List<T> left, int initStart, Sorter.MyValueWrapper i) {
        {
            left.add(list.get(initStart + (int) i.getValue()));
        }
        i.foo();
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
}
