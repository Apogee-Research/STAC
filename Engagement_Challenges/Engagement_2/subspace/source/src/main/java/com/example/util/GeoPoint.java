package com.example.util;

import java.io.Serializable;
import java.util.Random;

import org.apache.commons.math3.geometry.spherical.twod.S2Point;

/**
 * Represent a point in latitude-longitude space.
 */
public class GeoPoint
    implements Serializable
{
    private static final long serialVersionUID = 0L;

    public double latitude;
    public double longitude;

    public GeoPoint(
        double latitude,
        double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        normalize();
    }

    /**
     * Copy constructor.
     */
    public GeoPoint(
        GeoPoint other)
    {
        this(other.latitude, other.longitude);
    }

    public void normalize()
    {
        // this method doesn't support normalizing latitude
        if (latitude < -90.0 || latitude > 90.0)
        {
            throw new RuntimeException(
                "invalid latitude: " + latitude);
        }

        if (latitude == -90.0 || latitude == 90.0)
        {
            longitude = 0.0;
        }

        // normalize longitude
        longitude = ExtraMath.centeredModulus(360.0, longitude);

        assert latitude >= -90.0 : this;
        assert latitude <= 90.0 : this;
        assert longitude >= -180.0 : this;
        assert longitude < 180.0 : this;
    }

    /**
     * Return a random GeoPoint from an approximately uniform
     * distribution of area on Earth's surface.
     */
    public static GeoPoint random(
        Random random)
    {
        double z = random.nextDouble() * 2.0 - 1.0;
        double lat = Math.toDegrees(Math.asin(z));

        // No need to normalize, because the constructor will
        // normalize this.
        double lon = random.nextDouble() * 360.0;

        return new GeoPoint(lat, lon);
    }

    /**
     * Compute the angular distance (in degrees) between the two
     * points.
     */
    public double distance(
        GeoPoint other)
    {
        double result = Math.toDegrees(
            toS2Point().distance(
                other.toS2Point()));

        assert result >= 0.0 : this;
        assert result <= 180.0 : this;

        return result;
    }

    /**
     * Compute the initial bearing from this to {@code other}.
     */
    public double bearing(
        GeoPoint other)
    {
        // Special cases not handled well by the general-purpose
        // computation.
        if (latitude == -90.0 || latitude == 90.0)
        {
            return ExtraMath.modulus(360.0, other.longitude);
        }

        // General-purpose computation based on
        // http://www.movable-type.co.uk/scripts/latlong.html.
        double phi1 = Math.toRadians(latitude);
        double lambda1 = Math.toRadians(longitude);
        double phi2 = Math.toRadians(other.latitude);
        double lambda2 = Math.toRadians(other.longitude);

        double deltaLambda = lambda2 - lambda1;

        return ExtraMath.modulus(360.0, Math.toDegrees(
            Math.atan2(
                Math.sin(deltaLambda) * Math.cos(phi2),
                0.0
                    + Math.cos(phi1) * Math.sin(phi2)
                    - 1.0
                        * Math.sin(phi1)
                        * Math.cos(phi2)
                        * Math.cos(deltaLambda)
                )));
    }

    /**
     * Given a great arc with this point as one endpoint, an angular
     * distance of {@code distance}, and an initial bearing from this
     * point to the other endpoint of {@code bearing}, compute the
     * other endpoint.
     */
    public GeoPoint greatArcEndpoint(
        double distance,
        double bearing)
    {
        // Normalize distance and bearing.
        bearing = ExtraMath.modulus(360.0, bearing);
        distance = ExtraMath.modulus(360.0, distance);
        if (distance > 180.0)
        {
            bearing = (bearing + 180.0) % 360.0;
            distance = 360.0 - distance;
        }

        // Special cases not handled well by the general-purpose
        // computation.
        if (distance == 0.0)
        {
            return new GeoPoint(latitude, longitude);
        }
        else if (latitude == -90.0)
        {
            return new GeoPoint(
                latitude + distance,
                bearing);
        }
        else if (latitude == 90.0)
        {
            return new GeoPoint(
                latitude - distance,
                bearing);
        }

        // General-purpose computation based on
        // http://www.movable-type.co.uk/scripts/latlong.html.
        double phi1 = Math.toRadians(latitude);
        double lambda1 = Math.toRadians(longitude);
        double delta = Math.toRadians(distance);
        double theta = Math.toRadians(bearing);

        double phi2 = Math.asin(0.0
            + Math.sin(phi1) * Math.cos(delta)
            + Math.cos(phi1) * Math.sin(delta) * Math.cos(theta)
            );
        double lambda2 = 0.0
            + lambda1
            + Math.atan2(
                Math.sin(theta) * Math.sin(delta) * Math.cos(phi1),
                Math.cos(delta) - Math.sin(phi1) * Math.sin(phi2)
                )
            ;

        return new GeoPoint(
            Math.toDegrees(phi2),
            Math.toDegrees(lambda2));
    }

    @Override
    public boolean equals(
        Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        else if (!(obj instanceof GeoPoint))
        {
            return false;
        }
        else
        {
            GeoPoint other = (GeoPoint)obj;

            return true
                && this.latitude == other.latitude
                && this.longitude == other.longitude
                ;
        }
    }

    @Override
    public int hashCode()
    {
        int hash = 1;
        hash = hash * 31 + Double.valueOf(latitude).hashCode();
        hash = hash * 31 + Double.valueOf(longitude).hashCode();
        return hash;
    }

    @Override
    public String toString()
    {
        return String.format("@%s,%s", latitude, longitude);
    }

    public S2Point toS2Point()
    {
        return new S2Point(
            Math.toRadians(180.0 + longitude),
            Math.toRadians(90.0 - latitude));
    }
}
