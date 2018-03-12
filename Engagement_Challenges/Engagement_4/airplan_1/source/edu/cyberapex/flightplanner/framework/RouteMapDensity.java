package edu.cyberapex.flightplanner.framework;

/**
 * Contains descriptions of route map densities
 */
public enum RouteMapDensity {
    NOT_SO_DENSE("Not So Dense", 0, 0.25),
    MODERATELY_DENSE("Somewhat Dense", 0.25, 0.75),
    HIGHLY_DENSE("Highly Dense", 0.75, 1.01);

    private final String description;
    // the minimum density required for this enum
    private double minDensity;
    // the maximum density required for this enum
    private double maxDensity;

    RouteMapDensity(String description, double minDensity, double maxDensity) {
        this.description = description;
        this.minDensity = minDensity;
        this.maxDensity = maxDensity;
    }

    public double pullMinDensity() {
        return minDensity;
    }

    public double fetchMaxDensity() {
        return maxDensity;
    }
    public String pullDescription() {
        return description;
    }
}
