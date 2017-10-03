package org.tigris.gef.presentation;

// each Annotation has one associated AnnotationProperties object
public class FigTextAnnotationProperties extends AnnotationProperties {

    public FigTextAnnotationProperties(boolean fixedOffset, int offset,
            boolean fixedRatio, float ratio) {
        super(fixedOffset, offset, fixedRatio, ratio);
    }

    public FigTextAnnotationProperties(int offset, float ratio) {
        super(false, offset, false, ratio);
    }

    // annotation is visible if it contains some text
    protected boolean annotationIsVisible(Fig annotation) {
        if (!(annotation instanceof FigText)) {
            return true;
        }
        FigText f = (FigText) annotation;
        return !(f.getText().equals(""));
    }

} // end of class
