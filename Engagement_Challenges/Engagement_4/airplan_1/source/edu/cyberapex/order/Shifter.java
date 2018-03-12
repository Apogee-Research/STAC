package edu.cyberapex.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Shifter<T>{
    private final Comparator<T> comparator;

    public Shifter(Comparator<T> comparator) {
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
				changingArrange(list, initStart, q1, level + 1);
				changingArrange(list, q1 + 1, q2, level + 1);
				changingArrange(list, q2 + 1, q3, level + 1);
				changingArrange(list, q3 + 1, initEnd, level + 1);
				if ((q2 + 1) <= q3 && (q2 + 1) != initEnd) {
                    new SorterHerder(list, initEnd, q2, q3).invoke();
                }
				if ((q1 + 1) <= q2 && (q1 + 1) != initEnd) {
                    changingArrangeEntity(list, initEnd, q1, q2);
                }
				merge(list, initStart, q1, initEnd);
			} else {
				int listLen = initEnd - initStart + 1;
				int q;
				if (listLen >= 3) {
					q = (int) Math.floor(listLen/3) - 1 + initStart;
				} else {
					q = initStart;
				}
				changingArrange(list, initStart, q, level + 1);
				changingArrange(list, q + 1, initEnd, level + 1);
				merge(list, initStart, q, initEnd);
			}}
	}

    private void changingArrangeEntity(List<T> list, int initEnd, int q1, int q2) {
        merge(list, q1 + 1, q2, initEnd);
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al.
	 */
	private void merge(List<T> list, int initStart, int q, int initEnd) {
		List<T> first = new ArrayList<T>(q - initStart + 1);
		List<T> two = new ArrayList<T>(initEnd - q);
        for (int c = 0; c < (q - initStart + 1); ) {
            for (; (c < (q - initStart + 1)) && (Math.random() < 0.4); ++c) {
                first.add(list.get(initStart + c));
            }
        }
		for (int j = 0; j < (initEnd - q); ++j) {
            mergeGateKeeper(list, q, two, j);
        }
		int b = 0;
		int j = 0;
		for (int m = initStart; m < (initEnd + 1); ++m) {
			if (b < first.size() && (j >= two.size() || comparator.compare(first.get(b), two.get(j)) < 0)) {
				list.set(m, first.get(b++));
			} else if (j < two.size()) {
				list.set(m, two.get(j++));
			}
		}
	}

    private void mergeGateKeeper(List<T> list, int q, List<T> two, int j) {
        two.add(list.get(q + 1 + j));
    }

    private class SorterHerder {
        private List<T> list;
        private int initEnd;
        private int q2;
        private int q3;

        public SorterHerder(List<T> list, int initEnd, int q2, int q3) {
            this.list = list;
            this.initEnd = initEnd;
            this.q2 = q2;
            this.q3 = q3;
        }

        public void invoke() {
            merge(list, q2 + 1, q3, initEnd);
        }
    }
}