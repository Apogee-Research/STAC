package com.graphhopper.tour.util;

import java.io.IOException;

/**
 * Created by ngoffee on 9/29/15.
 */
public interface ProgressReporter
{
    void reportProgress( int complete, int total ) throws IOException;

    ProgressReporter SILENT = new ProgressReporter()
    {
        @Override
        public void reportProgress( int total, int complete ) {}
    };

    ProgressReporter STDERR = new ProgressReporter()
    {
        @Override
        public void reportProgress( int complete, int total )
        {
            System.err.format("%d/%d complete\n", complete, total);
        }
    };
}
