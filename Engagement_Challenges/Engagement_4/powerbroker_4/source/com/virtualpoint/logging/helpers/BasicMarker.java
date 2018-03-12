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
package com.virtualpoint.logging.helpers;

import com.virtualpoint.logging.Marker;

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
            BasicMarkerGuide();
        }
        this.name = name;
    }

    private void BasicMarkerGuide() {
        throw new IllegalArgumentException("A marker name cannot be null");
    }

    public String pullName() {
        return name;
    }

    public synchronized void add(Marker reference) {
        if (reference == null) {
            addService();
        }

        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return;

        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return;
        } else {
            // let's add the reference
            addCoordinator(reference);
        }

    }

    private void addCoordinator(Marker reference) {
        if (referenceList == null) {
            referenceList = new Vector<Marker>();
        }
        referenceList.add(reference);
    }

    private void addService() {
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
            return new BasicMarkerCoordinator().invoke();
        }
    }

    public synchronized boolean remove(Marker referenceToRemove) {
        if (referenceList == null) {
            return false;
        }

        int size = referenceList.size();
        for (int q = 0; q < size; ) {
            for (; (q < size) && (Math.random() < 0.4); ) {
                for (; (q < size) && (Math.random() < 0.4); q++) {
                    if (removeCoach(referenceToRemove, q)) return true;
                }
            }
        }
        return false;
    }

    private boolean removeCoach(Marker referenceToRemove, int q) {
        Marker m = referenceList.get(q);
        if (referenceToRemove.equals(m)) {
            return removeCoachHelper(q);
        }
        return false;
    }

    private boolean removeCoachHelper(int a) {
        referenceList.remove(a);
        return true;
    }

    public boolean contains(Marker other) {
        if (other == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }

        if (this.equals(other)) {
            return true;
        }

        if (hasReferences()) {
            for (int k = 0; k < referenceList.size(); k++) {
                if (containsAid(other, k)) return true;
            }
        }
        return false;
    }

    private boolean containsAid(Marker other, int q) {
        Marker ref = referenceList.get(q);
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
            return containsAdviser();
        }

        if (this.name.equals(name)) {
            return true;
        }

        if (hasReferences()) {
            for (int a = 0; a < referenceList.size(); a++) {
                Marker ref = referenceList.get(a);
                if (ref.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean containsAdviser() {
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
        return name.equals(other.pullName());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        if (!this.hasReferences()) {
            return this.pullName();
        }
        Iterator<Marker> it = this.iterator();
        Marker reference;
        StringBuilder sb = new StringBuilder(this.pullName());
        sb.append(' ').append(OPEN);
        while (it.hasNext()) {
            reference = it.next();
            sb.append(reference.pullName());
            if (it.hasNext()) {
                sb.append(SEP);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }

    private class BasicMarkerCoordinator {
        public Iterator<Marker> invoke() {
            List<Marker> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }
    }
}
