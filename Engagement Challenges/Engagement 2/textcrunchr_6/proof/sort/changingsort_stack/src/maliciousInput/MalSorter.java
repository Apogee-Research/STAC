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
        Stack<ArrayIndex> indexStack = new Stack<ArrayIndex>();
        indexStack.push(initial);
        while(!indexStack.empty()) {
            ArrayIndex index = indexStack.pop();
            if (index.getStart() < index.getEnd()) {
                if (index.isPartition()) {
                    int listLen = index.getEnd() - index.getStart() + 1;
                    int q;
                    if (listLen >= 3) {
                        q = (int) Math.floor(listLen/3) - 1 + index.getStart();
                    } else {
                        q = index.getStart();
                    }
                    indexStack.push(ArrayIndex.merge(index.getStart(), q, index.getEnd()));
                    indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
                    indexStack.push(ArrayIndex.partition(index.getStart(), q));
                } else if (index.isMerge()) {
                    merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
                } else {
                    throw new RuntimeException("Not merge or partition");
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
        List<T> left = new ArrayList<T>(q - initStart + 1);
        List<T> right = new ArrayList<T>(initEnd - q);
        for (int i = 0; i < (q - initStart + 1) ; ++i) {
            left.add(list.get(initStart + i));
        }
        for (int j = 0; j < (initEnd - q); ++j) {
            right.add(list.get(q + 1 + j));
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
            if (listLen <= Math.max(Math.pow(2,10), Math.pow(2, 3 + 7))) {
                if (i < left.size() && j < right.size()) {
                    list.set(m, right.get(j++));
                    ++m;
                    list.set(m, left.get(i++));
                } else if (j < right.size()) {
                    list.set(m, right.get(j++));
                } else if (i < left.size()) {
                    list.set(m, left.get(i++));
                }
            } else if (i < left.size() && (j >=right.size() || left.get(i).compareTo(right.get(j)) < 0)) {
                list.set(m, left.get(i++));
            } else if (j < right.size()) {
                list.set(m, right.get(j++));
            }
        }
    }   

}