package com.tweeter.service;

import com.tweeter.utility.TrieNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The spelling class contains the time vulnerability.
 * <p/>
 * Review the design doc as it explains the vulnerability, which has not changed since the doc was written.
 * <p/>
 * <ol>
 * <li>
 * <em>generate1Edits(word, matcher)</em>
 * <ol>
 * <li><b>Responsibilities:</b> The generation of the edits of word.</li>
 * <li><b>Oracle (matcher):</b> Used to determine if an edit should be returned immediately or saved.</li>
 * </ol>
 * </li>
 * <li>
 * <em>generateP1Edits(k-1 edits, &lt;optional&gt; matcher)</em>
 * <ol>
 * <li><b>Responsibilities:</b> The generation of all k edits from k-1 edits.</li>
 * <li><b>Optional Oracle (matcher):</b> Used in the generate1Edits, etc to determine if an edit should be returned immediately or saved.</li>
 * </ol>
 * </li>
 * <li>
 * <em>generateKEdits(k, word, matcher)</em>
 * <ol>
 * <li><b>Responsibilities:</b> Use generateP1Edits k times.</li>
 * <li><b>Oracle (matcher):</b> Used in the generation to determine if an edit should be returned immediately or saved.</li>
 * </ol>
 * </li>
 * <li>
 * <em>adjustCases(...)</em>
 * <ol>
 * <li><b>Responsibilities:</b> To attempt to make word replacements fit the punctuation of the original context.</li>
 * <li><b>Example:</b> A phrase Xlphabet zoup would be corrected as Alphabet soup instead of alphabet soup.</li>
 * </ol>
 * </li>
 * <li>
 * <em>generateCorrections(...)</em>
 * <ol>
 * <li><b>Responsibilities:</b> To find all words that correct this word in 3 or fewer edits.</li>
 * <li><b>Accounts for half of the bad code</b></li>
 * </ol>
 * </li>
 * <li>
 * <em>isPossible(...)</em>
 * <ol>
 * <li><b>Responsibilities:</b> To attempt to reduce the number of edits generated before a correction is given up on.</li>
 * </ol>
 * </li>
 * <li>
 * <em>correct(...)</em>
 * <ol>
 * <li><b>This is the entry-point of the by-word spelling corrections.</b></li>
 * <li><b>Responsibilities:</b> Correcting individual words.</li>
 * </ol>
 * </li>
 * </ol>
 */
public class Spelling {
    private final Map<String, Integer> dictionary = new HashMap<>();
    private final TrieNode possiblesTrie = new TrieNode();
    private final PossiblePrefixWordMatcher possiblePrefixWordMatcher = new PossiblePrefixWordMatcher(possiblesTrie);
    private final StoringMatcher storingMatcher = new StoringMatcher(dictionary);
    private final int MAX_WORD_LENGTH;
    private final int MAX_EDIT_LENGTH = 2;

    Spelling(InputStream file) throws IOException {
        MAX_WORD_LENGTH = loadDictionary(file, possiblesTrie, dictionary);
    }

    static int loadDictionary(InputStream file, TrieNode possiblesTrie, Map<String, Integer> dictionary) throws IOException {
        int wordLen = 0;
        BufferedReader in = new BufferedReader(new InputStreamReader(file));
        Pattern p = Pattern.compile("[A-Za-z]+");
        for (String temp = ""; temp != null; temp = in.readLine()) {
            Matcher m = p.matcher(temp.toLowerCase());
            while (m.find()) {
                temp = m.group();
                if (dictionary != null) {
                    dictionary.put(temp, dictionary.containsKey(temp) ? dictionary.get(temp) + 1 : 1);
                }
                if (temp.length() > wordLen) {
                    wordLen = temp.length();
                }
                if (possiblesTrie != null){
                    possiblesTrie.insert(temp);
                }
            }
        }
        in.close();
        return wordLen;
    }

    private Set<String> generate1Edits(String word, IMatcher match) {
        Set<String> result = new HashSet<>();
        String edit;
        // Removals
        for (int i = 0; i < word.length(); ++i) {
            //edit = word.substring(0, i) + word.substring(i + 1);
            edit = removeCharAt(word, i);
            if (match.storeMatch(edit)) result.add(edit);
            if (match.returnMatch(edit)) return result;
        }

        // Transposition
        for (int i = 0; i < word.length() - 1; ++i) {
            //edit = word.substring(0, i) + word.substring(i + 1, i + 2) + word.substring(i, i + 1) + word.substring(i + 2);
            edit = swapCharsAt(word, i);
            if (match.storeMatch(edit)) result.add(edit);
            if (match.returnMatch(edit)) return result;
        }

        // Replacement
        for (int i = 0; i < word.length(); ++i) {
            for (char c = 'a'; c <= 'z'; ++c) {
                //edit = word.substring(0, i) + String.valueOf(c) + word.substring(i + 1);
                edit = replaceCharAt(word, i, c);
                if (match.storeMatch(edit)) result.add(edit);
                if (match.returnMatch(edit)) return result;
            }
        }

        // Addition
        for (int i = 0; i <= word.length(); ++i) {
            for (char c = 'a'; c <= 'z'; ++c) {
                //edit = word.substring(0, i) + String.valueOf(c) + word.substring(i);
                edit = addCharAt(word, i, c);
                if (match.storeMatch(edit)) result.add(edit);
                if (match.returnMatch(edit)) return result;
            }
        }

        return result;
    }

    String removeCharAt(String word, int pos) {
        int c = 0;
        int d = 0;
        char[] e = new char[word.length() - 1];
        while (c < word.length()) {
            if (c != pos) {
                e[d++] = word.charAt(c);
            }
            c++;
        }
        return new String(e);
    }

    String swapCharsAt(String word, int pos) {
        int c = 0;
        int d = 0;
        char[] e = new char[word.length()];
        while (c < word.length()) {
            if (c < pos || c > (pos + 1)) e[d++] = word.charAt(c++);
            else {
                e[d++] = word.charAt(c + 1);
                e[d++] = word.charAt(c);
                c += 2;
            }
        }
        return new String(e);
    }

    String replaceCharAt(String word, int pos, char ch) {
        int c = 0;
        char[] e = new char[word.length()];
        while (c < word.length()) {
            if (c != pos) {
                e[c] = word.charAt(c);
            } else {
                e[c] = ch;
            }
            c++;
        }
        return new String(e);
    }

    String addCharAt(String word, int pos, char ch) {
        int c = 0;
        int d = 0;
        char[] e = new char[word.length() + 1];
        while (c < word.length()) {
            if (c == pos) {
                e[d++] = ch;
            }
            e[d++] = word.charAt(c++);
        }
        if (pos == word.length()) {
            e[d] = ch;
        }
        return new String(e);
    }

    private Set<String> generateP1Edits(Set<String> k_1_edits) {
        final Set<String> kEdits = new HashSet<>();
        for (String k_1_edit : k_1_edits) {
            kEdits.addAll(generate1Edits(k_1_edit, new StoreAllMatcher()));
        }
        return kEdits;
    }

    private Set<String> generateP1Edits(Set<String> k_1_edits, IMatcher matcher) {
        final Set<String> kEdits = new HashSet<>();
        for (String k_1_edit : k_1_edits) {
            Set<String> edits = generate1Edits(k_1_edit, matcher);
            for (String edit : edits) {
                if (matcher.returnMatch(edit)) {
                    return edits;
                }
            }
            kEdits.addAll(edits);
        }
        return kEdits;
    }

    private boolean generateKEdits(int k, String word, IMatcher match) {
        Set<String> lastEdits = new HashSet<>();
        lastEdits.add(word);

        for (int i = 0; i < k; i++) {
            lastEdits = generateP1Edits(lastEdits, match);
            for (String lastEdit : lastEdits) {
                if (match.returnMatch(lastEdit)) return true;
            }
        }

        return false;
    }

    boolean possiblePrefixes(String prefix) {
        if (possiblesTrie.lookup(prefix)) return true;
        for (int i = 1; i <= MAX_EDIT_LENGTH; i++) {
            if (generateKEdits(i, prefix, possiblePrefixWordMatcher)) return true;
        }
        return false;
    }

    boolean isPossible(String word) {
        boolean possible;
        int prefixLength = 1;
        do {
            possible = possiblePrefixes(word.substring(0, prefixLength));
            prefixLength++;
        } while (possible && prefixLength <= word.length());
        return possible;
    }

    private static int min(int a, int b) {
        return a > b ? b : a;
    }

    private boolean isLower(String word, String lower, int i) {
        final char a = word.charAt(i);
        final char b = lower.charAt(i);
        return a == b;
    }

    final Map<Integer, String> correct(String word) { // A word longer than the longest word plus the number of edits to return it to the longest word is a word that is too long for any correction to be possible.
        if (word.length() > MAX_WORD_LENGTH + MAX_EDIT_LENGTH || dictionary.containsKey(word.toLowerCase())) return null;
        Map<Integer, String> corrections;

        if (isPossible(word) && (corrections = getCorrections(word)) != null) return adjustCases(word, corrections);
        return null;
    }

    private Map<Integer, String> adjustCases(String word, Map<Integer, String> corrections) {
        for (Map.Entry<Integer, String> me : corrections.entrySet()) {
            final String lower = word.toLowerCase();
            final String newLower = me.getValue().toLowerCase();
            String output = "";
            final int length = newLower.length() < word.length() ? newLower.length() : word.length();
            for (int i = 0; i < length; i++) {
                final boolean l = isLower(word, lower, i);
                final char cChar = newLower.charAt(i);
                if (!l) {
                    output += Character.toUpperCase(cChar);
                } else {
                    output += cChar;
                }
            }
            if (output.length() < newLower.length()) {
                output += newLower.substring(output.length());
            }

            corrections.put(me.getKey(), output);
        }
        return corrections;
    }

    private Map<Integer, String> getCorrections(String word) {
        Set<String> lastEdits = new HashSet<>();
        lastEdits.add(word);

        for (int i = 1; i <= MAX_EDIT_LENGTH; i++) {
            Map<Integer, String> corrections = new HashMap<>();
            if (i == MAX_EDIT_LENGTH) {
                lastEdits = generateP1Edits(lastEdits, storingMatcher);
            } else {
                lastEdits = generateP1Edits(lastEdits);
            }

            for (String edit : lastEdits) {
                if (dictionary.containsKey(edit)) {
                    corrections.put(dictionary.get(edit), edit);
                }
            }
            if (corrections.size() > 0) {
                return corrections;
            }
        }
        return null;
    }
}
