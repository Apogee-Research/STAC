package com.graphhopper.tour;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.Path;
import com.graphhopper.util.shapes.GHPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Calculates a distance matrix for a given set of points.
 *
 * @see Matrix
 */
public class MatrixCalculator
{
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final GraphHopper hopper;

    public MatrixCalculator( GraphHopper hopper )
    {
        this.hopper = hopper;
    }

    public <P extends GHPoint> Matrix<P> calcMatrix( List<P> points )
    {
        PathCalculator pc = new PathCalculator(hopper);
        Matrix matrix = new Matrix<P>(points);

        int size = points.size();
        int numPaths = size * (size - 1);
        logger.info("Calculating " + numPaths + " pairwise paths");

        for (int i = 0, c = 0; i < size; i++)
        {
            GHPoint from = points.get(i);
            for (int j = 0; j < size; j++)
            {
                if (j == i)
                    continue;
                
                GHPoint to = points.get(j);

                Path path = pc.calcPath(from, to);
                matrix.setWeight(i, j, path.getWeight());

                if (++c % 100 == 0)
                    logger.info(c + "/" + numPaths);
            }
        }

        return matrix;
    }
}
