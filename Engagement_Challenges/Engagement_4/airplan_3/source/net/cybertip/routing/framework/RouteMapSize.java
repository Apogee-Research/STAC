package net.cybertip.routing.framework;

/**
 * Contains descriptions of route map sizes
 */
public enum RouteMapSize {
    VERY_LARGE("Large", 300, Integer.MAX_VALUE),
    MODERATELY_LARGE("Medium", 100, 300),
    FAIRLY_SMALL("Small", 0, 100);

    private final String description;
    // the minimum size required for this enum
    private int smallestSize;
    // the maximum size required for this enum
    private int maxSize;

    RouteMapSize(String description, int smallestSize, int maxSize) {
        this.description = description;
        this.smallestSize = smallestSize;
        this.maxSize = maxSize;
    }

    public int obtainMinimumSize() {
        return smallestSize;
    }

    public int fetchMaximumSize() {
        return maxSize;
    }

    public String obtainDescription() {
        return description;
    }
}
