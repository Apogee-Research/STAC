package net.techpoint.order;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Ranker<T>{
    private final Comparator<T> comparator;

    public Ranker(Comparator<T> comparator) {
        this.comparator = comparator;
    }

	/**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
	public List<T> align(Collection<T> stuff) {
		List<T> stuffList = new ArrayList<T>(stuff);
		changingAlign(stuffList, 0, stuffList.size() - 1);
		return stuffList;
	}

	/**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * uses an altered merge-sort algorithm
	 */
	private void changingAlign(List<T> list, int initStart, int initEnd) {
	
		if (initStart < initEnd) {
			int q1 = (int) Math.floor((initStart + initEnd)/2);
			int q2 = (int) Math.floor((q1 + 1 + initEnd)/2);
			int q3 = (int) Math.floor((q2 + 1 + initEnd)/2);
			int q4 = (int) Math.floor((q3 + 1 + initEnd)/2);
			int q5 = (int) Math.floor((q4 + 1 + initEnd)/2);
			changingAlign(list, initStart, q1);
			changingAlign(list, q1 + 1, q2);
			changingAlign(list, q2 + 1, q3);
			changingAlign(list, q3 + 1, q4);
			changingAlign(list, q4 + 1, q5);
			changingAlign(list, q5 + 1, initEnd);
			if ((q4 + 1) <= q5 && (q4 + 1) != initEnd) {
                changingAlignGuide(list, initEnd, q4, q5);
            }
			if ((q3 + 1) <= q4 && (q3 + 1) != initEnd) {
                changingAlignExecutor(list, initEnd, q3, q4);
            }
			if ((q2 + 1) <= q3 && (q2 + 1) != initEnd) {
				merge(list, q2 + 1, q3, initEnd);
			}
			if ((q1 + 1) <= q2 && (q1 + 1) != initEnd) {
				merge(list, q1 + 1, q2, initEnd);
			}
			merge(list, initStart, q1, initEnd);}
	}

    private void changingAlignExecutor(List<T> list, int initEnd, int q3, int q4) {
        merge(list, q3 + 1, q4, initEnd);
    }

    private void changingAlignGuide(List<T> list, int initEnd, int q4, int q5) {
        new SorterExecutor(list, initEnd, q4, q5).invoke();
    }

    /**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al.
	 */
	private void merge(List<T> list, int initStart, int q, int initEnd) {
		List<T> one = new ArrayList<T>(q - initStart + 1);
		List<T> last = new ArrayList<T>(initEnd - q);
		for (int i = 0; i < (q - initStart + 1) ; ++i) {
            mergeAdviser(list, initStart, one, i);
        }
        for (int j = 0; j < (initEnd - q); ) {
            while ((j < (initEnd - q)) && (Math.random() < 0.4)) {
                for (; (j < (initEnd - q)) && (Math.random() < 0.5); ) {
                    for (; (j < (initEnd - q)) && (Math.random() < 0.6); ++j) {
                        last.add(list.get(q + 1 + j));
                    }
                }
            }
        }
		int i = 0;
		int j = 0;
		for (int m = initStart; m < (initEnd + 1); ++m) {
			if (i < one.size() && (j >= last.size() || comparator.compare(one.get(i), last.get(j)) < 0)) {
				list.set(m, one.get(i++));
			} else if (j < last.size()) {
				list.set(m, last.get(j++));
			}
		}
	}

    private void mergeAdviser(List<T> list, int initStart, List<T> one, int b) {
        one.add(list.get(initStart + b));
    }

    private class SorterExecutor {
        private List<T> list;
        private int initEnd;
        private int q4;
        private int q5;

        public SorterExecutor(List<T> list, int initEnd, int q4, int q5) {
            this.list = list;
            this.initEnd = initEnd;
            this.q4 = q4;
            this.q5 = q5;
        }

        public void invoke() {
            merge(list, q4 + 1, q5, initEnd);
        }
    }
}