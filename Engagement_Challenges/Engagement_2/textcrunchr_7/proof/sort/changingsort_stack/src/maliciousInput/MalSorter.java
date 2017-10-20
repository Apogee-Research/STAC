package maliciousInput;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

public class MalSorter<T extends Comparable<? super T>> {

    /**
     * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
     * uses an altered merge-sort algorithm
     */
    public void changingSort(List<T> list, int initStart, int initEnd) {
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
        for (MalSorter.MyValueWrapper iTheOriginalOne = new  MalSorter.MyValueWrapper(0), i = iTheOriginalOne; iTheOriginalOne.getValue() < (q - initStart + 1); i.increment()) {
            {
                left.add(list.get(initStart + (int) i.getValue()));
            }
            i.foo();
        }
        for (MalSorter.MyValueWrapper jTheOriginalOne = new  MalSorter.MyValueWrapper(0), j = jTheOriginalOne; jTheOriginalOne.getValue() < (initEnd - q); j.increment()) {
            {
                right.add(list.get(q + 1 + (int) j.getValue()));
            }
            j.foo();
        }
        int i = 0;
        int j = 0;
        int listLen = initEnd - initStart + 1;
        for (int m = initStart; m < (initEnd + 1); ++m) {
            /**
             * This produces an unsorted list by always placing the right elements before the left elements
             * at each level of recursion from the bottom level (merging two elements) to level where the 
             * subarray being sorted is == max(2^10, 2^(k + 7)). The 2^10 is necessary to make sure the 
             * array is sufficiently mixed up. Once the subarrays are longer than that, they are sorted normally,
             * but they are sufficiently mixed that it doesn't matter.
             */
            if (listLen <= Math.max(Math.pow(2, 10), Math.pow(2, 7 + 7))) {
                if (i < left.size() && j < right.size()) {
                    list.set(m, right.get(j++));
                    ++m;
                    list.set(m, left.get(i++));
                } else if (j < right.size()) {
                    list.set(m, right.get(j++));
                } else if (i < left.size()) {
                    list.set(m, left.get(i++));
                }
            } else if (i < left.size() && (j >= right.size() || left.get(i).compareTo(right.get(j)) < 0)) {
                list.set(m, left.get(i++));
            } else if (j < right.size()) {
                list.set(m, right.get(j++));
            }
        }
    }

    public static class MyValueWrapper {

        private float val;

        public  MyValueWrapper(float init_val) {
            val = init_val;
        }

        public void increment() {
            val++;
        }

        public void decrement() {
            val--;
        }

        public float getValue() {
            return val;
        }

        public void setValue(float value) {
            val = value;
        }

        public void setValue(int value) {
            val = value;
        }

        public void foo() {
            val /= 1.0;
        }
    }
}
