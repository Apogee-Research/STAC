package com.cyberpointllc.stac.snapservice.model;

import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Location {

    public static final Location UNKNOWN = new  Location("C0000", "Unknown");

    private final String identity;

    private final String city;

    private final Set<AccessPoint> accessPoints;

    public Location(String identity, String city, Set<AccessPoint> accessPoints) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Location identity may not be null or empty");
        }
        if (StringUtils.isBlank(city)) {
            throw new  IllegalArgumentException("City name may not be null or empty");
        }
        if (accessPoints == null) {
            throw new  IllegalArgumentException("Set of access points may not be null");
        }
        if (accessPoints.isEmpty()) {
            throw new  IllegalArgumentException("Set of access points may not be empty");
        }
        this.identity = identity;
        this.city = city;
        // LinkedHashSet is vulnerable
        this.accessPoints = new  LinkedHashSet(accessPoints);
    }

    private Location(String identity, String city) {
        // Special constructor to by-pass empty access points constraint
        this.identity = identity;
        this.city = city;
        accessPoints = Collections.emptySet();
    }

    /**
     * Returns the identity for this Location.
     * The identity may not be modified.
     *
     * @return String representing the identity;
     * guaranteed to not be <code>null</code>
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the city associated with this location.
     *
     * @return String representing the city name;
     * guaranteed to not be <code>null</code>
     */
    public String getCity() {
        return city;
    }

    /**
     * Returns the set of access points defining
     * this location.
     * The set returned may not be modified.
     *
     * @return Set of AccessPoint instances defining
     * this location; guaranteed to not be <code>null</code>
     */
    public Set<AccessPoint> getAccessPoints() {
        return Collections.unmodifiableSet(accessPoints);
    }

    /**
     * Returns the set of BSSIDs of each of the
     * access points defining this location.
     *
     * @return Set of access point BSSID values defining this location;
     * may be empty but guaranteed to not be <code>null</code>
     */
    public Set<String> getAccessPointBssids() {
        // LinkedHashSet is vulnerable
        Set<String> set = new  LinkedHashSet();
        for (AccessPoint accessPoint : accessPoints) {
            getAccessPointBssidsHelper(set, accessPoint);
        }
        return set;
    }

    /**
     * Returns the set of names of each of the
     * access points defining this location.
     *
     * @return Set of the access point names defining this location;
     * may be empty but guaranteed to not be <code>null</code>
     */
    public Set<String> getAccessPointNames() {
        // LinkedHashSet is vulnerable
        Set<String> set = new  LinkedHashSet();
        for (AccessPoint accessPoint : accessPoints) {
            getAccessPointNamesHelper(set, accessPoint);
        }
        return set;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Location location = (Location) obj;
        return identity.equals(location.getIdentity());
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    private void getAccessPointBssidsHelper(Set<String> set, AccessPoint accessPoint) {
        set.add(accessPoint.getBssid());
    }

    private void getAccessPointNamesHelper(Set<String> set, AccessPoint accessPoint) {
        set.add(accessPoint.getName());
    }
}
