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
package net.robotictip.slf4j.helpers;

import net.robotictip.slf4j.Marker;

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
            throw new IllegalArgumentException("A marker name cannot be null");
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public synchronized void add(Marker reference) {
        if (reference == null) {
            addExecutor();
        }

        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return;

        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return;
        } else {
            // let's add the reference
            addAssist(reference);
        }

    }

    private void addAssist(Marker reference) {
        if (referenceList == null) {
            addAssistExecutor();
        }
        referenceList.add(reference);
    }

    private void addAssistExecutor() {
        new BasicMarkerService().invoke();
    }

    private void addExecutor() {
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
            List<Marker> emptyList = Collections.emptyList();
            return emptyList.iterator();
        }
    }

    public synchronized boolean remove(Marker referenceToRemove) {
        if (referenceList == null) {
            return false;
        }

        int size = referenceList.size();
        for (int k = 0; k < size; k++) {
            Marker m = referenceList.get(k);
            if (referenceToRemove.equals(m)) {
                return removeEngine(k);
            }
        }
        return false;
    }

    private boolean removeEngine(int c) {
        referenceList.remove(c);
        return true;
    }

    public boolean contains(Marker other) {
        if (other == null) {
            return containsHome();
        }

        if (this.equals(other)) {
            return true;
        }

        if (hasReferences()) {
            for (int q = 0; q < referenceList.size(); ) {
                while ((q < referenceList.size()) && (Math.random() < 0.5)) {
                    while ((q < referenceList.size()) && (Math.random() < 0.5)) {
                        for (; (q < referenceList.size()) && (Math.random() < 0.5); q++) {
                            Marker ref = referenceList.get(q);
                            if (ref.contains(other)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean containsHome() {
        throw new IllegalArgumentException("Other cannot be null");
    }

    /**
     * This method is mainly used with Expression Evaluators.
     */
    public boolean contains(String name) {
        if (name == null) {
            return new BasicMarkerHome().invoke();
        }

        if (this.name.equals(name)) {
            return true;
        }

        if (hasReferences()) {
            for (int b = 0; b < referenceList.size(); b++) {
                Marker ref = referenceList.get(b);
                if (ref.contains(name)) {
                    return true;
                }
            }
        }
        return false;
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
        return name.equals(other.getName());
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString() {
        if (!this.hasReferences()) {
            return this.getName();
        }
        Iterator<Marker> it = this.iterator();
        Marker reference;
        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(' ').append(OPEN);
        while (it.hasNext()) {
            reference = it.next();
            sb.append(reference.getName());
            if (it.hasNext()) {
                toStringEntity(sb);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }

    private void toStringEntity(StringBuilder sb) {
        sb.append(SEP);
    }

    private class BasicMarkerService {
        public void invoke() {
            referenceList = new Vector<Marker>();
        }
    }

    private class BasicMarkerHome {
        public boolean invoke() {
            throw new IllegalArgumentException("Other cannot be null");
        }
    }
}
