package com.cyberpointllc.stac.snapservice.model;

public class AccessPoint {

    // 12 characters for the bssid then 4 more for the 'check' digits
    private static final int BSSID_LENGTH = 16;

    private final String bssid;

    private final String name;

    private final double latitude;

    private final double longitude;

    public AccessPoint(double latitude, double longitude, String name, String bssid) {
        if ((bssid == null) || (bssid.length() != BSSID_LENGTH)) {
            throw new  IllegalArgumentException("BSSID must exist and be of length " + Integer.toString(BSSID_LENGTH));
        }
        this.bssid = bssid;
        this.name = (name != null) ? name : bssid;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getBssid() {
        return bssid;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        // relying exclusively on the BSSID, which is unique
        AccessPoint that = (AccessPoint) obj;
        return bssid.equals(that.bssid);
    }

    @Override
    public int hashCode() {
        return bssid.hashCode();
    }

    @Override
    public String toString() {
        ClasstoString replacementClass = new  ClasstoString();
        ;
        return replacementClass.doIt0();
    }

    public class ClasstoString {

        public ClasstoString() {
        }

        public String doIt0() {
            return String.format("%s: %s (%.6f,%.6f)", name, bssid, latitude, longitude);
        }
    }
}
