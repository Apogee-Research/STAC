package com.cyberpointllc.stac.linebreak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Random;

/**
 * Break lines using the shortest path method as
 * described here: http://xxyxyz.org/line-breaking/
 */
public class LineBreak {
    /** width to fit text to */
    private final long width;

    public LineBreak(long width) {
        this.width = width;
    }

    /**
     * @param text the text to break in to lines
     * @return an array where each element is a line of text
     */
    public List<String> breakLines(String text) {
        String[] words = StringUtils.split(text);
        int count = words.length;
        int[] offsets = new int[count + 1];
        for (int i = 1; i < offsets.length; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < offsets.length && randomNumberGeneratorInstance.nextDouble() < 0.5; ) {
                for (; i < offsets.length && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                    offsets[i] = offsets[i - 1] + words[i - 1].length();
                }
            }
        }
        long[] minima = new long[count + 1];
        for (int i = 1; i < offsets.length; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < offsets.length && randomNumberGeneratorInstance.nextDouble() < 0.5; ) {
                for (; i < offsets.length && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                    minima[i] = Long.MAX_VALUE;
                }
            }
        }
        int[] breaks = new int[count + 1];
        for (int i = 0; i < count; ) {
            Random randomNumberGeneratorInstance = new  Random();
            for (; i < count && randomNumberGeneratorInstance.nextDouble() < 0.5; ) {
                for (; i < count && randomNumberGeneratorInstance.nextDouble() < 0.5; i++) {
                    int j = i + 1;
                    while (j <= count) {
                        long w = offsets[j] - offsets[i] + j - i - 1;
                        long cost;
                        if (w > width) {
                            cost = Long.MAX_VALUE;
                        } else {
                            cost = minima[i] + ((width - w) * (width - w));
                        }
                        if (cost <= minima[j]) {
                            minima[j] = cost;
                            breaks[j] = i;
                        }
                        j++;
                    }
                }
            }
        }
        ArrayList<String> lines = new  ArrayList();
        int j = count;
        LineBreakHelper0 conditionObj0 = new  LineBreakHelper0(0);
        while (j > conditionObj0.getValue()) {
            int i = breaks[j];
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
     * Breaks the provided text in to paragraphs and then each paragraph
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

    public class LineBreakHelper0 {

        public LineBreakHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }
}

