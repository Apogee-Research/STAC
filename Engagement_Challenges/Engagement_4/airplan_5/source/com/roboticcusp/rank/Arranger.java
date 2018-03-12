package com.roboticcusp.rank;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Arranger<T>{
    private final Comparator<T> comparator;

    public Arranger(Comparator<T> comparator) {
        this.comparator = comparator;
    }

	/**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
	public List<T> arrange(Collection<T> stuff) {
		List<T> stuffList = new ArrayList<T>(stuff);
		changingArrange(stuffList, 0, stuffList.size() - 1, 0);
		return stuffList;
	}

	/**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * uses an altered merge-sort algorithm
	 */
	private void changingArrange(List<T> list, int initStart, int initEnd, int level) {
	
		if (initStart < initEnd) {if (level % 2 == 0) {
				int q1 = (int) Math.floor((initStart + initEnd)/2);
				int q2 = (int) Math.floor((q1 + 1 + initEnd)/2);
				int q3 = (int) Math.floor((q2 + 1 + initEnd)/2);
				int q4 = (int) Math.floor((q3 + 1 + initEnd)/2);
				int q5 = (int) Math.floor((q4 + 1 + initEnd)/2);
				changingArrange(list, initStart, q1, level + 1);
				changingArrange(list, q1 + 1, q2, level + 1);
				changingArrange(list, q2 + 1, q3, level + 1);
				changingArrange(list, q3 + 1, q4, level + 1);
				changingArrange(list, q4 + 1, q5, level + 1);
				changingArrange(list, q5 + 1, initEnd, level + 1);
				if ((q4 + 1) <= q5 && (q4 + 1) != initEnd) {
                    changingArrangeTarget(list, initEnd, q4, q5);
                }
				if ((q3 + 1) <= q4 && (q3 + 1) != initEnd) {
                    changingArrangeAid(list, initEnd, q3, q4);
                }
				if ((q2 + 1) <= q3 && (q2 + 1) != initEnd) {
                    changingArrangeExecutor(list, initEnd, q2, q3);
                }
				if ((q1 + 1) <= q2 && (q1 + 1) != initEnd) {
					merge(list, q1 + 1, q2, initEnd);
				}
				merge(list, initStart, q1, initEnd);
			} else {
				int listLen = initEnd - initStart + 1;
				int q;
				if (listLen >= 5) {
					q = (int) Math.floor(listLen/5) - 1 + initStart;
				} else {
					q = initStart;
				}
				changingArrange(list, initStart, q, level + 1);
				changingArrange(list, q + 1, initEnd, level + 1);
				merge(list, initStart, q, initEnd);
			}}
	}

    private void changingArrangeExecutor(List<T> list, int initEnd, int q2, int q3) {
        merge(list, q2 + 1, q3, initEnd);
    }

    private void changingArrangeAid(List<T> list, int initEnd, int q3, int q4) {
        merge(list, q3 + 1, q4, initEnd);
    }

    private void changingArrangeTarget(List<T> list, int initEnd, int q4, int q5) {
        merge(list, q4 + 1, q5, initEnd);
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al.
	 */
	private void merge(List<T> list, int initStart, int q, int initEnd) {
		List<T> one = new ArrayList<T>(q - initStart + 1);
		List<T> right = new ArrayList<T>(initEnd - q);
        for (int a = 0; a < (q - initStart + 1); ) {
            while ((a < (q - initStart + 1)) && (Math.random() < 0.5)) {
                while ((a < (q - initStart + 1)) && (Math.random() < 0.4)) {
                    for (; (a < (q - initStart + 1)) && (Math.random() < 0.4); ++a) {
                        mergeHerder(list, initStart, one, a);
                    }
                }
            }
        }
		for (int j = 0; j < (initEnd - q); ++j) {
			right.add(list.get(q + 1 + j));
		}
		int c = 0;
		int j = 0;
		for (int m = initStart; m < (initEnd + 1); ++m) {
			if (c < one.size() && (j >=right.size() || comparator.compare(one.get(c), right.get(j)) < 0)) {
				list.set(m, one.get(c++));
			} else if (j < right.size()) {
				list.set(m, right.get(j++));
			}
		}
	}

    private void mergeHerder(List<T> list, int initStart, List<T> one, int j) {
        one.add(list.get(initStart + j));
    }
}