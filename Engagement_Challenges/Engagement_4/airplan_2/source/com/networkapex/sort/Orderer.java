package com.networkapex.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Orderer<T>{
    private final Comparator<T> comparator;

    public Orderer(Comparator<T> comparator) {
        this.comparator = comparator;
    }

	/**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
	public List<T> rank(Collection<T> stuff) {
		List<T> stuffList = new ArrayList<T>(stuff);
		changingRank(stuffList, 0, stuffList.size() - 1);
		return stuffList;
	}

	/**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * uses an altered merge-sort algorithm
	 */
	private void changingRank(List<T> list, int initStart, int initEnd) {
	
		if (initStart < initEnd) {int listLen = initEnd - initStart + 1;
			int q;
			if (listLen >= 6) {
				q = (int) Math.floor(listLen/6) - 1 + initStart;
			} else {
				q = initStart;
			}
			changingRank(list, initStart, q);
			changingRank(list, q + 1, initEnd);
                // calculate the length of the list mod k
            int listModK = list.size() % 6;
                // When listModK == 0, ceiling(listModK/k) == 0, and q = q.
            // When listModK != 0, ceiling(listModK/k) == 1, and q = initEnd
            int q1 = q * (1 - (int) Math.ceil(listModK/6.0)) +  (initEnd *  (int) Math.ceil(listModK/6.0));
            changingRank(list, q1 + 1, initEnd);
			merge(list, initStart, q, initEnd);}
	}

	/**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al.
	 */
	private void merge(List<T> list, int initStart, int q, int initEnd) {
		List<T> one = new ArrayList<T>(q - initStart + 1);
		List<T> two = new ArrayList<T>(initEnd - q);
		for (int p = 0; p < (q - initStart + 1) ; ++p) {
			one.add(list.get(initStart + p));
		}
        for (int j = 0; j < (initEnd - q); ) {
            for (; (j < (initEnd - q)) && (Math.random() < 0.4); ++j) {
                mergeWorker(list, q, two, j);
            }
        }
		int p = 0;
		int j = 0;
		for (int m = initStart; m < (initEnd + 1); ++m) {
			if (p < one.size() && (j >= two.size() || comparator.compare(one.get(p), two.get(j)) < 0)) {
				list.set(m, one.get(p++));
			} else if (j < two.size()) {
				list.set(m, two.get(j++));
			}
		}
	}

    private void mergeWorker(List<T> list, int q, List<T> two, int j) {
        two.add(list.get(q + 1 + j));
    }
}