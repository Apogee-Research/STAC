package com.cyberpointllc.stac.textcrunchr;

import java.io.InputStream;
import java.io.IOException;
import com.cyberpointllc.stac.textcrunchr.TCResult;
import java.util.Map;

abstract class Processor {

    public abstract TCResult process(InputStream inps) throws IOException;

    public abstract String getName();
}
