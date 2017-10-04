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
        ClasschangingSort replacementClass = new  ClasschangingSort(list, initStart, initEnd);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al
	 */
    private void merge(List<T> list, int initStart, int q, int initEnd) {
        Classmerge replacementClass = new  Classmerge(list, initStart, q, initEnd);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
    }

    private class ClasschangingSort {

        public ClasschangingSort(List<T> list, int initStart, int initEnd) {
            this.list = list;
            this.initStart = initStart;
            this.initEnd = initEnd;
        }

        private List<T> list;

        private int initStart;

        private int initEnd;

        private ArrayIndex initial;

        public void doIt0() {
            initial = ArrayIndex.partition(initStart, initEnd);
        }

        private Stack<ArrayIndex> indexStack;

        public void doIt1() {
            indexStack = new  Stack<ArrayIndex>();
            indexStack.push(initial);
            while (!indexStack.empty()) {
                ArrayIndex index = indexStack.pop();
                if (index.getStart() < index.getEnd()) {
                    if (index.isPartition()) {
                        int listLen = index.getEnd() - index.getStart() + 1;
                        int q;
                        if (listLen >= 3) {
                            q = (int) Math.floor(listLen / 3) - 1 + index.getStart();
                        } else {
                            q = index.getStart();
                        }
                        indexStack.push(ArrayIndex.merge(index.getStart(), q, index.getEnd()));
                        // If the list has this characteristic, we let changingSort have a bad running time.
                        if (list.size() % 3 == 0) {
                            // depending on the index 3 this running time could be much worse than O(n^2)
                            indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
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
    }

    private class Classmerge {

        public Classmerge(List<T> list, int initStart, int q, int initEnd) {
            this.list = list;
            this.initStart = initStart;
            this.q = q;
            this.initEnd = initEnd;
        }

        private List<T> list;

        private int initStart;

        private int q;

        private int initEnd;

        private List<T> left;

        public void doIt0() {
            left = new  ArrayList<T>(q - initStart + 1);
        }

        private List<T> right;

        private int i;

        private int j;

        public void doIt1() {
            right = new  ArrayList<T>(initEnd - q);
            for (int i = 0; i < (q - initStart + 1); ++i) {
                left.add(list.get(initStart + i));
            }
            for (int j = 0; j < (initEnd - q); ++j) {
                right.add(list.get(q + 1 + j));
            }
            i = 0;
            j = 0;
            for (int m = initStart; m < (initEnd + 1); ++m) {
                if (i < left.size() && (j >= right.size() || comparator.compare(left.get(i), right.get(j)) < 0)) {
                    list.set(m, left.get(i++));
                } else if (j < right.size()) {
                    list.set(m, right.get(j++));
                }
            }
        }
    }
}
