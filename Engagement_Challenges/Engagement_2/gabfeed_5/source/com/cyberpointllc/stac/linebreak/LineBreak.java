package com.cyberpointllc.stac.linebreak;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * Break lines using the divide and conquer method found 
 * here: http://xxyxyz.org/line-breaking/
 */
public class LineBreak {
    private final long width;

    public LineBreak(long width) {
        this.width = width;
    }

    /**
     * Breaks the text into lines formatted using
     * the set width.
     * @param text
     * @return List containing lines of text
     */
    public List<String> breakLines(String text) {
        String[] words = StringUtils.split(text);
        int count = words.length;
        int[] offsets = new int[count + 1];
        for (int i = 1; i < offsets.length; i++) {
            offsets[i] = offsets[i - 1] + words[i - 1].length();
        }
        long[] minima = new long[count + 1];
        for (int i = 1; i < minima.length; i++) {
            minima[i] = Long.MAX_VALUE;
        }
        int[] breaks = new int[count + 1];
        int n = count + 1;
        int i = 0;
        int offset = 0;
        boolean splitting = true;
        whileLoop: while (splitting) {
            int r = (int) Math.min(n, Math.pow(2, i + 1));
            int edge = (int) Math.pow(2, i) + offset;
            search(0 + offset, edge, edge, r + offset, minima, breaks, offsets);
            long x = minima[r - 1 + offset];
            // called if "break forLoop" is reached.
            forLoop: {
                for (int j = (int) Math.pow(2, i); j < (r - 1); j++) {
                    long y = cost(j + offset, r - 1 + offset, offsets, minima);
                    if (y <= x) {
                        n -= j;
                        i = 0;
                        offset += j;
                        break forLoop;
                    }
                }
                if (r == n) {
                    break whileLoop;
                }
                i += 1;
            }
        }
        ArrayList<String> lines = new  ArrayList();
        int j = count;
        while (j > 0) {
            i = breaks[j];
            String[] lineWords = ArrayUtils.subarray(words, i, j);
            lines.add(StringUtils.join(lineWords, " "));
            j = i;
        }
        Collections.reverse(lines);
        return lines;
    }

    public List<String> breakParagraphs(String text) {
        return breakParagraphs(text, "\n");
    }

    /**
     * Breaks the provided text into paragraphs and then each paragraph
     * its lines formatted for width
     * @param text
     * @return
     */
    public List<String> breakParagraphs(String text, String paraDelim) {
        String[] paralines = StringUtils.splitPreserveAllTokens(text, "\n", 0);
        StringBuilder curParagraph = new  StringBuilder();
        ArrayList<String> totalLines = new  ArrayList();
        for (String line : paralines) {
            if (!StringUtils.isEmpty(line)) {
                curParagraph.append(line);
                curParagraph.append(paraDelim);
            } else {
                // new paragraph
                // process the old one and then get ready for the new
                totalLines.addAll(breakLines(curParagraph.toString()));
                totalLines.add(paraDelim);
                curParagraph = new  StringBuilder();
            }
        }
        // add the last one
        totalLines.addAll(breakLines(curParagraph.toString()));
        return totalLines;
    }

    /**
     * Helps set the break points for the given text.
     */
    private long cost(int i, int j, int[] offsets, long[] minima) {
        int w = offsets[j] - offsets[i] + j - i - 1;
        if (w > width) {
            return Long.MAX_VALUE;
        }
        return minima[i] + (int) Math.pow(width - w, 2);
    }

    /**
     * Sets the break points for the given text.
     */
    private void search(int i0, int j0, int i1, int j1, long[] minima, int[] breaks, int[] offsets) {
        Stack<List<Integer>> stack = new  Stack();
        stack.push(Arrays.asList(i0, j0, i1, j1));
        while (!stack.isEmpty()) {
            searchHelper(offsets, breaks, stack, minima);
        }
    }

    private void searchHelper(int[] offsets, int[] breaks, Stack<List<Integer>> stack, long[] minima) {
        List<Integer> indices = stack.pop();
        int currentI0 = indices.get(0);
        int currentJ0 = indices.get(1);
        int currentI1 = indices.get(2);
        int currentJ1 = indices.get(3);
        if (currentJ0 < currentJ1) {
            int j = (int) Math.floor((currentJ0 + currentJ1) / 2);
            for (int i = currentI0; i < currentI1; i++) {
                long cost = cost(i, j, offsets, minima);
                if (cost <= minima[j]) {
                    minima[j] = cost;
                    breaks[j] = i;
                }
            }
            stack.push(Arrays.asList(breaks[j], j + 1, currentI1, currentJ1));
            stack.push(Arrays.asList(currentI0, currentJ0, breaks[j] + 1, j));
        }
    }
}

