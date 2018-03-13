package com.tweeter.service;

/**
 * The Matcher interface allows an algorithm to alter its functionality based on an external stimulus.
 * In the general case the stimulus is an oracle who knows the answer to the following questions:
 *
 * <ol>
 *     <li><b>1.</b> Should I terminate and return s?</li>
 *     <li><b>2.</b> Should I store s?</li>
 * </ol>
 *
 */
public interface IMatcher {
    /**
     * Indicates that an algorithm using this matcher should terminate and return s.
     * @param s Some string that the algorithm is attempting to return.
     * @return True if the algorithm should terminate and return s.
     */
    boolean returnMatch(String s);

    /**
     * Indicates that an algorithm should store the string s.
     * @param s Some string that the algorithm is attempting to store.
     * @return True if the algorithm should store s.
     */
    boolean storeMatch(String s);

}
