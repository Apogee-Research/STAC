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
package net.computerpoint.logging.helpers;

import net.computerpoint.logging.Marker;

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

    public String fetchName() {
        return name;
    }

    public synchronized void add(Marker reference) {
        if (reference == null) {
            addEngine();
        }

        // no point in adding the reference multiple times
        if (this.contains(reference)) {
            return;

        } else if (reference.contains(this)) { // avoid recursion
            // a potential reference should not its future "parent" as a reference
            return;
        } else {
            // let's add the reference
            if (referenceList == null) {
                addAid();
            }
            referenceList.add(reference);
        }

    }

    private void addAid() {
        referenceList = new Vector<Marker>();
    }

    private void addEngine() {
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
            return iteratorAid();
        }
    }

    private Iterator<Marker> iteratorAid() {
        List<Marker> emptyList = Collections.emptyList();
        return emptyList.iterator();
    }

    public synchronized boolean remove(Marker referenceToRemove) {
        if (referenceList == null) {
            return false;
        }

        int size = referenceList.size();
        for (int j = 0; j < size; ) {
            for (; (j < size) && (Math.random() < 0.5); ) {
                while ((j < size) && (Math.random() < 0.5)) {
                    for (; (j < size) && (Math.random() < 0.5); j++) {
                        Marker m = referenceList.get(j);
                        if (referenceToRemove.equals(m)) {
                            referenceList.remove(j);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean contains(Marker other) {
        if (other == null) {
            return new BasicMarkerWorker().invoke();
        }

        if (this.equals(other)) {
            return true;
        }

        if (hasReferences()) {
            for (int p = 0; p < referenceList.size(); p++) {
                if (containsHelper(other, p)) return true;
            }
        }
        return false;
    }

    private boolean containsHelper(Marker other, int j) {
        if (new BasicMarkerAdviser(other, j).invoke()) return true;
        return false;
    }

    /**
     * This method is mainly used with Expression Evaluators.
     */
    public boolean contains(String name) {
        if (name == null) {
            return containsExecutor();
        }

        if (this.name.equals(name)) {
            return true;
        }

        if (hasReferences()) {
            for (int p = 0; p < referenceList.size(); p++) {
                if (containsCoordinator(name, p)) return true;
            }
        }
        return false;
    }

    private boolean containsCoordinator(String name, int i) {
        Marker ref = referenceList.get(i);
        if (ref.contains(name)) {
            return true;
        }
        return false;
    }

    private boolean containsExecutor() {
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
                toStringUtility(sb);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }

    private void toStringUtility(StringBuilder sb) {
        new BasicMarkerAid(sb).invoke();
    }

    private class BasicMarkerWorker {
        public boolean invoke() {
            throw new IllegalArgumentException("Other cannot be null");
        }
    }

    private class BasicMarkerAdviser {
        private boolean myResult;
        private Marker other;
        private int a;

        public BasicMarkerAdviser(Marker other, int a) {
            this.other = other;
            this.a = a;
        }

        boolean is() {
            return myResult;
        }

        public boolean invoke() {
            Marker ref = referenceList.get(a);
            if (ref.contains(other)) {
                return true;
            }
            return false;
        }
    }

    private class BasicMarkerAid {
        private StringBuilder sb;

        public BasicMarkerAid(StringBuilder sb) {
            this.sb = sb;
        }

        public void invoke() {
            sb.append(SEP);
        }
    }
}
