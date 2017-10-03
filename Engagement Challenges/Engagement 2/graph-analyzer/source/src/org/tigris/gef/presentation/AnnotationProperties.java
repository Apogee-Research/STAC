package org.tigris.gef.presentation;

import java.awt.Color;
import org.tigris.gef.base.*;

// each Annotation has one associated AnnotationProperties object
public class AnnotationProperties {

    private boolean fixedOffset = false;
    private boolean fixedRatio = false;
    private int offset = 10;
    private float ratio = (float) 0.5;
    private int connectingLineVisibilityDuration = 300;
    private FigLine line = new FigLine(0, 0, 0, 0);
    private Color lineColor = Color.red;

    public AnnotationProperties() {
        this(false, 5, false, (float) 0.5);
    }

    public AnnotationProperties(boolean fixedOffset, int offset,
            boolean fixedRatio, float ratio) {
        this.offset = offset;
        this.ratio = ratio;
        this.fixedOffset = fixedOffset;
        this.fixedRatio = fixedRatio;
        // connectingLine visible for 300 ms
    }

    public AnnotationProperties(int offset, float ratio) {
        this(false, offset, false, ratio);
    }

    public void setLineColor(Color c) {
        lineColor = c;
    }

    public Color getLineColor() {
        return lineColor;
    }

    public void setLineVisibilityDuration(int millis) {
        connectingLineVisibilityDuration = millis;
    }

    public int getLineVisibilityDuration() {
        return connectingLineVisibilityDuration;
    }

    public boolean hasFixedRatio() {
        return fixedRatio;
    }

    public boolean hasFixedOffset() {
        return fixedOffset;
    }

    public float getRatio() {
        return ratio;
    }

    public void setRatio(float ratio, boolean fixedRatio) {
        this.ratio = ratio;
        this.fixedRatio = fixedRatio;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset, boolean fixedOffset) {
        this.offset = offset;
        this.fixedOffset = fixedOffset;
    }

    public FigLine getConnectingLine() {
        return line;
    }

    // line is visible only if annotation is visible
    protected boolean lineIsVisible(Fig annotation) {
        return annotationIsVisible(annotation);
    }

    protected boolean annotationIsVisible(Fig annotation) {
        return true;
    }

    // removes the line from the active diagram
    public synchronized void removeLine() {
        if (Globals.curEditor().getLayerManager().getContents().contains(line)) {
            line.removeFromDiagram();
        }
    }

} // end of class
