/**
 * Copyright (c) 2004-2011 QOS.ch
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */
package edu.networkcusp.logging.helpers;

import edu.networkcusp.logging.Marker;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * A simple implementation of the {@link Marker} interface.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author Joern Huxhorn
 */
public class BasicMarker implements Marker {

    private static final long serialVersionUID = 1803952589649545191L;

    private final String name;
    private List<Marker> referenceList;

    BasicMarker(String name) {
        if (name == null) {
            BasicMarkerEngine();
        }
        this.name = name;
    }

    private void BasicMarkerEngine() {
        throw new IllegalArgumentException("A marker name cannot be null");
    }

    public String fetchName() {
        return name;
    }

    public synchronized void add(Marker reference) {
        if (reference == null) {
            addAid();
        }

        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return;

        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return;
        } else {
            // let's add the reference
            new BasicMarkerEntity(reference).invoke();
        }

    }

    private void addAid() {
        throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
    }

    public synchronized boolean hasReferences() {
        return ((referenceList != null) && (referenceList.size() > 0));
    }

    public boolean hasChildren() {
        return hasReferences();
    }

    public synchronized Iterator<Marker> iterator() {
        if (referenceList != null) {
            return referenceList.iterator();
        } else {
            return iteratorHerder();
        }
    }

    private Iterator<Marker> iteratorHerder() {
        List<Marker> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }

    public synchronized boolean remove(Marker referenceToRemove) {
        if (referenceList == null) {
            return false;
        }

        int size = referenceList.size();
        for (int a = 0; a < size; ) {
            for (; (a < size) && (Math.random() < 0.6); ) {
                while ((a < size) && (Math.random() < 0.6)) {
                    for (; (a < size) && (Math.random() < 0.6); a++) {
                        if (removeExecutor(referenceToRemove, a)) return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean removeExecutor(Marker referenceToRemove, int a) {
        Marker m = referenceList.get(a);
        if (referenceToRemove.equals(m)) {
            referenceList.remove(a);
            return true;
        }
        return false;
    }

    public boolean contains(Marker other) {
        if (other == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }

        if (this.equals(other)) {
            return true;
        }

        if (hasReferences()) {
            for (int j = 0; j < referenceList.size(); j++) {
                if (containsHerder(other, j)) return true;
            }
        }
        return false;
    }

    private boolean containsHerder(Marker other, int c) {
        Marker ref = referenceList.get(c);
        if (ref.contains(other)) {
            return true;
        }
        return false;
    }

    /**
     * This method is mainly used with Expression Evaluators.
     */
    public boolean contains(String name) {
        if (name == null) {
            return containsSupervisor();
        }

        if (this.name.equals(name)) {
            return true;
        }

        if (hasReferences()) {
            for (int c = 0; c < referenceList.size(); c++) {
                if (containsExecutor(name, c)) return true;
            }
        }
        return false;
    }

    private boolean containsExecutor(String name, int c) {
        Marker ref = referenceList.get(c);
        if (ref.contains(name)) {
            return true;
        }
        return false;
    }

    private boolean containsSupervisor() {
        throw new IllegalArgumentException("Other cannot be null");
    }

    private static String OPEN = "[ ";
    private static String CLOSE = " ]";
    private static String SEP = ", ";

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Marker))
            return false;

        final Marker other = (Marker) obj;
        return name.equals(other.fetchName());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        if (!this.hasReferences()) {
            return this.fetchName();
        }
        Iterator<Marker> it = this.iterator();
        Marker reference;
        StringBuilder sb = new StringBuilder(this.fetchName());
        sb.append(' ').append(OPEN);
        while (it.hasNext()) {
            reference = it.next();
            sb.append(reference.fetchName());
            if (it.hasNext()) {
                sb.append(SEP);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }

    private class BasicMarkerEntity {
        private Marker reference;

        public BasicMarkerEntity(Marker reference) {
            this.reference = reference;
        }

        public void invoke() {
            if (referenceList == null) {
                invokeAid();
            }
            referenceList.add(reference);
        }

        private void invokeAid() {
            referenceList = new Vector<Marker>();
        }
    }
}
