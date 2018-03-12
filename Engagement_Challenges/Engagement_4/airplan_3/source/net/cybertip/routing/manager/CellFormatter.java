package net.cybertip.routing.manager;

import java.util.Arrays;
import java.util.Random;
/**
 * Class for formatting data for display in html
 */
public class CellFormatter {
    private static Random random = new Random();
    enum Justification {FIRST, LAST, CENTER}
    
    private int width; // we use width as a per-graph max for random padding, to encourage greater size variation between different graphs

    public CellFormatter(int length){
	    width = random.nextInt(length);
        if (width < 5){ // make sure width is at least 5
            width = 5;
        }
    }
    /**
     * Pad with whitespace (or truncate) s to be of exactly length len
     * @param s string to pad/truncate
     * @param len desired string length (if negative, leave length alone)
     * @param adjust boolean indicating to use instead a random length up to len
     * @return
     */
    public String format(String s, int len, Justification j, boolean adjust){
        String result = s; // if len < 0 we just return this

        if (adjust){ // make length some number up to width
            len = width-5 + random.nextInt(width);
            if (len < 4){
                len = 4;
            }
        }
        if (s.length() > len && len > 0){ // s is too long; truncate and put in ellipses
            result = s.substring(0, len-3) + "...";
        } else if (len > 0){ // need to pad

            // make whitespace string to use
            char[] spaceArray = new char[len];
            Arrays.fill(spaceArray, ' ');
            String whitespace = new String(spaceArray);

            // put padding on left, right, or both sides, as directed by Justification parameter j
            if (j.equals(Justification.FIRST)){
                result = s + whitespace.substring(s.length());
            } else if (j.equals(Justification.LAST)){
                result = whitespace.substring(s.length()) + s;
            } else { // CENTER
                int buffer = (len - s.length())/2;
                result = whitespace.substring(0, buffer) + s + whitespace.substring(0, buffer);
                if (len%2!=0){ // if len was odd, add one more space
                    result += " ";
                }
            }
        }
        return result;
    }
}
