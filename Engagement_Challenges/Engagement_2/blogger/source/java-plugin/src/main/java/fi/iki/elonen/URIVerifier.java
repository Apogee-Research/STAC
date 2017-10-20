package fi.iki.elonen;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.LinkedList;
import java.util.TreeMap;

/*
 * The URI Verifier uses a built in NFA which represents an evil regex to match a URI
 * to the format of a java fully qualified class name.
 */
public final class URIVerifier {
    static final int SKIP = -1;
    private final URIElement verifierElements = new URIElement();

    /*
     * This constructor allows you to substitute the built in verifier NFA for
     * one of your own.
     */
    URIVerifier(URIElement n) {
        verifierElements.add(SKIP, n);
    }

    /*
     * This constructor links the URIElements to each other to form the NFA tree.
     * This NFA tree is generated and modified to look like a human wrote it due
     * to its size and complexity.
     */
    URIVerifier() {
        int[] from = {0,17,1,16,2,4,9,5,8,6,8, 1,11,15,12,14,15,16};
        int[] to =  {1, 1,2, 2,3,5,5,6,6,7,9,10,12,12,13,15,16,17};
        URIElement[] elements = new URIElement[to.length];

        for (int i = 0; i < to.length; i++) {
            if (elements[from[i]] == null) elements[from[i]] = new URIElement();
            if (elements[to[i]] == null) elements[to[i]] = new URIElement();
            elements[from[i]].add(SKIP, elements[to[i]]);
        }
        elements[7].isFinal = true;
        elements[13].add('.', elements[14]);
        for (int i = 0; i < 26; i++) {
            elements[10].add('a' + i, elements[11]);
            elements[13].add('a' + i, elements[14]);
            elements[3].add('A' + i, elements[4]);
            elements[5].add('A' + i, elements[8]);
            elements[5].add('a' + i, elements[8]);
        }
        verifierElements.add(SKIP, elements[0]);
    }

    /*
     * Verify is a depth first search of the elements in the NFA constructed above.
     */
    public boolean verify(String string) {
        Tuple<Integer, URIElement> peek;
        LinkedList<Tuple<Integer, URIElement>> tuples = new LinkedList<>();
        tuples.push(new Tuple<>(0, verifierElements));

        while (!tuples.isEmpty() && (peek = tuples.pop()) != null) {
            if (peek.second.isFinal && peek.first == string.length()) {
                return true;
            }

            if (string.length() > peek.first) {
                for (URIElement URIElement : peek.second.get(string.charAt(peek.first))) {
                    tuples.push(new Tuple<>(peek.first + 1, URIElement));
                }
            }

            for (URIElement child : peek.second.get(SKIP)) {
                tuples.push(new Tuple<>(peek.first, child));
            }
        }

        return false;
    }

    /*
     * URIElements are analogous to NFA graph elements. Each node contains a set of
     * cost/reference pairs (edges) which are used in graph search algorithms. As
     * NFA have "final" states you may construct a URIElement as a final state or
     * set it to be final after construction.
     */
    public static final class URIElement {
        boolean isFinal;
        TreeMap<Integer, LinkedList<URIElement>> map = new TreeMap<>();

        public URIElement(){}
        public URIElement(boolean isFinal) {
            this.isFinal = isFinal;
        }

        /*
         * Returns the reference stored in the edge set with the given cost "key"
         */
        LinkedList<URIElement> get(int key) {
            if (map.containsKey(key)) {
                return map.get(key);
            }
            return new LinkedList<>();
        }

        /*
         * Adds a new edge to the edge set. The key is the cost of traversing the edge
         * and the value is the reference of the element at the end of the edge.
         */
        void add(int key, URIElement value) {
            LinkedList<URIElement> l;
            if (!this.map.containsKey(key)) {
                l = new LinkedList<>();
                this.map.put(key, l);
            } else {
                l = this.map.get(key);
            }
            l.add(value);
        }
    }
}


