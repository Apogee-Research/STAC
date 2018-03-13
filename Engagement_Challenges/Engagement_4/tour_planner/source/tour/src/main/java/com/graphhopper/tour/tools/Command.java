package com.graphhopper.tour.tools;

import com.graphhopper.util.CmdArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ngoffee on 9/22/15.
 */
public abstract class Command
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected CmdArgs cmdArgs;
    protected List<String> ownArgs;

    public Command parseArgs(String[] args)
    {
        List<String> ghArgs = new ArrayList<>();
        ownArgs = new ArrayList<>();
        for (String arg : args)
        {
            if (arg.contains("="))
                ghArgs.add(arg);
            else
                ownArgs.add(arg);
        }

        cmdArgs = CmdArgs.read(ghArgs.toArray(new String[ghArgs.size()]));

        checkArgs();

        return this;
    }

    public abstract void run() throws Exception;

    /**
     * Subclass hook to do additional checks on arguments.
     */
    protected void checkArgs()
    {
    }
}
