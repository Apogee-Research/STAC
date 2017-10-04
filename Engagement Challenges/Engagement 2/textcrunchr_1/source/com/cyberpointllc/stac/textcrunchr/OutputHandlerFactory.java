package com.cyberpointllc.stac.textcrunchr;

import com.cyberpointllc.stac.textcrunchr.XmlOutputHandler;
import com.cyberpointllc.stac.textcrunchr.WindowOutputHandler;
import com.cyberpointllc.stac.textcrunchr.ConsoleOutputHandler;

public class OutputHandlerFactory {

    public static OutputHandler getOutputHandler(String type) {
        if (type.equalsIgnoreCase("xml")) {
            return new  XmlOutputHandler();
        } else if (type.equalsIgnoreCase("window")) {
            return new  WindowOutputHandler();
        }
        return new  ConsoleOutputHandler();
    }
}
