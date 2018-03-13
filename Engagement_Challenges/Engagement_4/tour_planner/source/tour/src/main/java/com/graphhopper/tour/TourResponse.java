package com.graphhopper.tour;

import com.graphhopper.util.shapes.GHPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for {@link TourCalculator} results.
 *
 * Based on {@link com.graphhopper.GHResponse}.
 */
public class TourResponse<P extends GHPoint>
{
    private final List<Throwable> errors = new ArrayList<>(4);
    private List<P> points = new ArrayList<>(0);

    private void check( String method )
    {
        if (hasErrors())
        {
            throw new RuntimeException("You cannot call " + method + " if response contains errors. Check this with ghResponse.hasErrors(). "
                    + "Errors are: " + getErrors());
        }
    }

    /**
     * @return true if one or more error found
     */
    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }

    public List<Throwable> getErrors()
    {
        return errors;
    }

    @SuppressWarnings("unchecked")
    public TourResponse<P> addError( Throwable error )
    {
        errors.add(error);
        return this;
    }

    public TourResponse<P> setPoints( List<P> points )
    {
        this.points = points;
        return this;
    }

    public List<? extends P> getPoints()
    {
        check("getPoints");
        return points;
    }
}
