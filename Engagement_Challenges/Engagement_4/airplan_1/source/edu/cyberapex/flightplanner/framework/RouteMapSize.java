package edu.cyberapex.flightplanner.framework;

/**
 * Contains descriptions of route map sizes
 */
public enum RouteMapSize {
    VERY_LARGE("Large", 300, Integer.MAX_VALUE),
    MODERATELY_LARGE("Medium", 100, 300),
    FAIRLY_SMALL("Small", 0, 100);

    private final String description;
    // the minimum size required for this enum
    private int minSize;
    // the maximum size required for this enum
    private int maxSize;

    RouteMapSize(String description, int minSize, int maxSize) {
        this.description = description;
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    public int fetchMinimumSize() {
        return minSize;
    }

    public int grabMaximumSize() {
        return maxSize;
    }

    public String fetchDescription() {
        return description;
    }
}
