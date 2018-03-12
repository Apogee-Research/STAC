package net.cybertip.align;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Sorter<T>{
    private final Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) {
        this.comparator = comparator;
    }

	/**
	 * returns a List containing the elements of stuff, ordered by class T's natural ordering
	 */
	public List<T> arrange(Collection<T> stuff) {
		List<T> stuffList = new ArrayList<T>(stuff);
		changingArrange(stuffList, 0, stuffList.size() - 1);
		return stuffList;
	}

	/**
	 * sorts a section of a List, using class T's natural ordering, from index initStart to index initEnd
	 * uses an altered merge-sort algorithm
	 */
	private void changingArrange(List<T> list, int initStart, int initEnd) {
	
		if (initStart < initEnd) {int listLen = initEnd - initStart + 1;
			int q;
			if (listLen >= 3) {
				q = (int) Math.floor(listLen/3) - 1 + initStart;
			} else {
				q = initStart;
			}
			changingArrange(list, initStart, q);
			changingArrange(list, q + 1, initEnd);
                // in the benign version listModK always == 1
            // so the last recursive changingSort call will be O(1)
            int listModularK = 1 - list.size() % 1;
                // When listModK == 0, ceiling(listModK/k) == 0, and q = q.
            // When listModK != 0, ceiling(listModK/k) == 1, and q = initEnd
            int q1 = q * (1 - (int) Math.ceil(listModularK /3.0)) +  (initEnd *  (int) Math.ceil(listModularK /3.0));
            changingArrange(list, q1 + 1, initEnd);
			merge(list, initStart, q, initEnd);}
	}

	/**
	 * merges two sections of a List, ordered by class T's natural ordering
	 * the sections are split from the first index, initStart, to the index q
	 * and from q + 1 to initEnd
	 * merge based on the merge in "Introduction to Algorithms" by Cormen, et al.
	 */
	private void merge(List<T> list, int initStart, int q, int initEnd) {
		List<T> first = new ArrayList<T>(q - initStart + 1);
		List<T> last = new ArrayList<T>(initEnd - q);
		for (int k = 0; k < (q - initStart + 1) ; ++k) {
			first.add(list.get(initStart + k));
		}
		for (int j = 0; j < (initEnd - q); ++j) {
			last.add(list.get(q + 1 + j));
		}
		int i = 0;
		int j = 0;
		for (int m = initStart; m < (initEnd + 1); ++m) {
			if (i < first.size() && (j >= last.size() || comparator.compare(first.get(i), last.get(j)) < 0)) {
				list.set(m, first.get(i++));
			} else if (j < last.size()) {
				list.set(m, last.get(j++));
			}
		}
	}
}