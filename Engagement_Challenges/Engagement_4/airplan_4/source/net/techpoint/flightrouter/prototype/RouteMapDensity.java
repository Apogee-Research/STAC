package net.techpoint.flightrouter.prototype;

/**
 * Contains descriptions of route map densities
 */
public enum RouteMapDensity {
    NOT_SO_DENSE("Not So Dense", 0, 0.25),
    MODERATELY_DENSE("Somewhat Dense", 0.25, 0.75),
    HIGHLY_DENSE("Highly Dense", 0.75, 1.01);

    private final String description;
    // the minimum density required for this enum
    private double smallestDensity;
    // the maximum density required for this enum
    private double maxDensity;

    RouteMapDensity(String description, double smallestDensity, double maxDensity) {
        this.description = description;
        this.smallestDensity = smallestDensity;
        this.maxDensity = maxDensity;
    }

    public double getSmallestDensity() {
        return smallestDensity;
    }

    public double takeMaxDensity() {
        return maxDensity;
    }
    public String takeDescription() {
        return description;
    }
}
