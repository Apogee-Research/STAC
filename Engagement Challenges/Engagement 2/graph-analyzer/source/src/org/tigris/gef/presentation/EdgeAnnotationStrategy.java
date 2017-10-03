package org.tigris.gef.presentation;

import java.awt.Point;

import org.tigris.gef.base.Globals;

public class EdgeAnnotationStrategy extends AnnotationStrategy {

    private static final long serialVersionUID = 839904139158340787L;
    AnnotationHelper helper = AnnotationHelper.instance();

    public EdgeAnnotationStrategy() {
    }

    public Point restoreAnnotationPosition(Fig annotation) {
        int d; // offset
        float ratio;

        Fig owner = annotation.getAnnotationOwner();
        // in the case: owner is an edge
        if (owner instanceof FigEdge) {
            AnnotationProperties prop = (AnnotationProperties) annotations
                    .get(annotation);
            d = prop.getOffset();
            ratio = prop.getRatio();
            FigEdge edge = (FigEdge) owner;
            // Point start = edge.getSourcePortFig().center();
            // Point ende = edge.getDestPortFig().center();
            Point start;
            Point ende;
            try {
                start = edge.getFirstPoint();
                ende = edge.getLastPoint();
            } catch (ArrayIndexOutOfBoundsException e) {
                try {
                    start = edge.getSourcePortFig().getCenter();
                    ende = edge.getDestPortFig().getCenter();
                } catch (NullPointerException ne) {
                    start = new Point(10, 10);
                    ende = new Point(100, 10);
                }
            }

            if ((start.x == ende.x) && (start.y == ende.y)) {
                return annotation.getLocation();
            }

            // calculate
            float xdirection = ende.x - start.x;
            float ydirection = ende.y - start.y;
            double newX = start.x + ratio * xdirection;
            double newY = start.y + ratio * ydirection;
            // restore offset d
            newX = newX
                    + d
                    * (ydirection / Math.sqrt(xdirection * xdirection
                            + ydirection * ydirection));
            newY = newY
                    + d
                    * (-1)
                    * (xdirection / Math.sqrt(xdirection * xdirection
                            + ydirection * ydirection));
            // the annotation's new position
            return new Point((int) newX - (annotation.getWidth() / 2),
                    (int) newY - (annotation.getHeight() / 2));
        }
        return new Point(1, 1);
    }

    /**
     * Calculates offset and ratio of annotation (relative to annotationOwner).
     * method is called when the annotation is moved without its owner
     */
    public void storeAnnotationPosition(Fig annotation) {
        Fig owner = annotation.getAnnotationOwner();
        // case: owner ist eine Kante
        if (owner instanceof FigEdge) {
            FigEdge edge = (FigEdge) owner;
            Point anPos = annotation.getCenter();
            // Point start= edge.getSourcePortFig().center();
            // Point ende = edge.getDestPortFig().center();
            Point start;
            Point ende;
            try {
                start = edge.getFirstPoint();
                ende = edge.getLastPoint();
            } catch (ArrayIndexOutOfBoundsException e) {
                start = edge.getSourcePortFig().getCenter();
                ende = edge.getDestPortFig().getCenter();
            }

            if ((start.x == ende.x) && (start.y == ende.y)) {
                return;
            }

            int d = AnnotationHelper.getNormOffset(anPos, start, ende);
            float ratio = AnnotationHelper.getRatio(anPos, start, ende);
            // store values
            AnnotationProperties prop = getAnnotationProperties(annotation);
            prop.setRatio(ratio, prop.hasFixedRatio());
            prop.setOffset(d, prop.hasFixedOffset());
        }
        drawConnectingLine(annotation);
    }

    /**
     * Draws a dotted line between this annotation and its owner.
     */
    public void drawConnectingLine(Fig annotation) throws NullPointerException {
        if (!(getAnnotationProperties(annotation).lineIsVisible(annotation))) {
            return;
        }
        Fig owner = annotation.getAnnotationOwner();
        AnnotationProperties prop = getAnnotationProperties(annotation);
        FigLine line = prop.getConnectingLine();

        if (((FigEdge) owner).getSourcePortFig().getCenter() == null
                || ((FigEdge) owner).getDestPortFig().getCenter() == null) {
            return;
        }
        // line from annotation to closest point on owning edge
        try {
            if (owner instanceof FigEdgePoly) {
                line.setShape(annotation.getCenter(), AnnotationHelper
                        .getClosestPoint(annotation.getCenter(),
                                (FigEdgePoly) owner));
            } else {
                line.setShape(annotation.getCenter(), AnnotationHelper
                        .getClosestPointOnEdge(annotation.getCenter(),
                                ((FigEdge) owner).getFirstPoint(),
                                ((FigEdge) owner).getLastPoint()));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            line.setShape(annotation.getCenter(), AnnotationHelper
                    .getClosestPointOnEdge(annotation.getCenter(),
                            ((FigEdge) owner).getSourcePortFig().getCenter(),
                            ((FigEdge) owner).getDestPortFig().getCenter()));
        }
        line.setLineColor(getAnnotationProperties(annotation).getLineColor());
        line.setFillColor(getAnnotationProperties(annotation).getLineColor());
        line.setDashed(true);
        // draw the line
        if (!(Globals.curEditor().getLayerManager().getContents()
                .contains(line))) {
            Globals.curEditor().add(line);
        }
        Globals.curEditor().getLayerManager().bringToFront(annotation);
        //
        line.damage();
        annotation.damage();

        // remove line automatically
        AnnotationLineRemover.instance()
                .removeLineIn(
                        getAnnotationProperties(annotation)
                        .getLineVisibilityDuration(), annotation);
    }

    /**
     * move annotations. this method is called when an annotationOwner is moved
     * without its annotations
     */
    public void translateAnnotations(Fig owner) {
        java.util.Enumeration iter = annotations.keys();
        // owner has moved; set annotations to their new positions
        while (iter.hasMoreElements()) {
            Fig annotation = (Fig) iter.nextElement();
            annotation.setLocation(restoreAnnotationPosition(annotation));
            // drawConnectingLine(annotation);
            Globals.curEditor().getLayerManager().bringToFront(annotation);
            // call endtrans of annotation
            annotation.endTrans();
            annotation.damage();

        }
        owner.damage();
    }

} // end of class
