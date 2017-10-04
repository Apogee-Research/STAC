package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Location;
import java.util.Set;

public interface LocationService {

    /**
     * Returns the set of all known locations.
     *
     * @return Set of Location instances known;
     * may be empty but guaranteed to not be <code>null</code>
     */
    Set<Location> getLocations();

    /**
     * Returns the Location associated with the specified identity.
     * If no match exists, <code>null</code> is returned.
     *
     * @param identity used to determine the location
     * @return Location matching the identity;
     * will be <code>null</code> if no matching location exists
     */
    Location getLocation(String identity);

    /**
     * Returns the Location associated with the specified
     * set of <code>BSSIDs</code>s.
     * If no match exists, <code>null</code> is returned.
     *
     * @param bssids used to determine location
     * @return Location matching the set of access points;
     * will be <code>null</code> if no matching location exists
     */
    Location getLocation(Set<String> bssids);
}
