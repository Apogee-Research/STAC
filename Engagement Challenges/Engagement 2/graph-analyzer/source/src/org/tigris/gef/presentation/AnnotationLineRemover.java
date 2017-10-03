package org.tigris.gef.presentation;

import java.awt.event.*;
import javax.swing.*;
import java.util.Hashtable;

public class AnnotationLineRemover implements ActionListener {

    private Hashtable timers; // FIG | Timer
    private Hashtable figs; // Timer | Fig (nur der bequemlichkeit wegen ;-)
    private static AnnotationLineRemover theInstance = null;

    private AnnotationLineRemover() {
        timers = new Hashtable();
        figs = new Hashtable();
    }

    public static AnnotationLineRemover instance() {
        if (theInstance == null) {
            theInstance = new AnnotationLineRemover();
        }
        return theInstance;
    }

    public void removeLineIn(int millis, Fig f) {
        // if this fig already has a timer, simply restart it to avoid hiding
        // of the currently moved elements connecting line.
        if (timers.containsKey(f)) {
            ((Timer) timers.get(f)).restart();
        } // create new timer
        else {
            Timer t = new Timer(millis, this);
            timers.put(f, t);
            figs.put(t, f);
            t.start();
        }
    }

    public void actionPerformed(ActionEvent e) {
        // org.graph.commons.logging.LogFactory.getLog(null).info("Event from Timer ! " + e.getSource());
        // time has passed - i.e. line hasn't moved for given number of
        // milliseconds and can therefore be removed.
        // Timer can then be stopped.
        Timer t = (Timer) e.getSource();
        t.stop();
        // ((Fig)figs.get(t)).getAnnotationStrategy().removeAllConnectingLines();
        Fig annotation = ((Fig) figs.get(t));
        // org.graph.commons.logging.LogFactory.getLog(null).info("*************************************"
        // +annotation);
        try {
            annotation.getAnnotationOwner().getAnnotationStrategy()
                    .getAnnotationProperties(annotation).removeLine();
        } catch (Exception ex) {
        }
    }
}
