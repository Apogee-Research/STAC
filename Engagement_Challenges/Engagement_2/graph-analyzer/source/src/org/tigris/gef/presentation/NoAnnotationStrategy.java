package org.tigris.gef.presentation;

public class NoAnnotationStrategy extends AnnotationStrategy {

    private static final long serialVersionUID = 5214334744207570878L;
    private static final NoAnnotationStrategy INSTANCE = new NoAnnotationStrategy();

    public static NoAnnotationStrategy getInstance() {
        return INSTANCE;
    }

    /**
     * This class is just a dummy for those Figs that need no Annotations. It is
     * created by default for all Figs.
     */
    private NoAnnotationStrategy() {
    }

    public void translateAnnotations(Fig owner) {
        // do nothing in this case
    }

    /*
     * protected void restoreAnnotationPosition(Fig annotation){ // do nothing
     * in this case }
     */
    public void storeAnnotationPosition(Fig annotation) {
        // do nothing in this case
    }
}
