package com.cyberpointllc.stac.textcrunchr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class WordStatsProcessor extends Processor {

    private static final String NAME = "wordStats";

    private final String MODEL = "en-sent.bin";

    public TCResult process(InputStream inps) throws IOException {
        InputStreamReader isr = new  InputStreamReader(inps);
        // count number of sentences
        String input = readInput(inps);
        String words[] = tokenize(input);
        TCResult result = new  TCResult("Word stats");
        result.addResult("Word count", words.length);
        result.addResult("Average word length", meanLen(words));
        result.addResult("Variance in word length", varLen(words));
        result.addResult("Longest word: ", longest(words));
        return result;
    }

    public String getName() {
        return NAME;
    }

    /**
	 * 
	 * @param input
	 * @return array of words in input
	 */
    private String[] tokenize(String input) {
        //"\\s+;";
        String regex = "[^\\p{Alnum}]+";
        String[] words = input.split(regex);
        return words;
    }

    /**
	 * 
	 * @param words
	 * @return the longest word
	 */
    private String longest(String[] words) {
        int maxLen = 0;
        String longestWord = "";
        for (String word : words) {
            int n = word.length();
            if (n > maxLen) {
                maxLen = n;
                longestWord = word;
            }
        }
        return longestWord;
    }

    /**
	 * 
	 * @param words
	 * @return the mean word length
	 */
    private double meanLen(String[] words) {
        double sum = 0;
        for (String s : words) {
            sum += s.length();
        }
        return sum / words.length;
    }

    /**
	 * @param words
	 * @return the variance in word length
	 */
    private double varLen(String[] words) {
        double sum = 0;
        double sumSq = 0;
        for (String s : words) {
            int senLen = s.length();
            sum += senLen;
            sumSq += senLen * senLen;
        }
        int len = words.length;
        return sumSq / len - sum * sum / (len * len);
    }

    private String readInput(InputStream inps) throws IOException {
        // read to string
        BufferedReader br = new  BufferedReader(new  InputStreamReader(inps));
        StringBuilder sb = new  StringBuilder();
        String read = br.readLine();
        while (read != null) {
            sb.append(read);
            read = br.readLine();
        }
        String theString = sb.toString();
        return theString;
    }
}
