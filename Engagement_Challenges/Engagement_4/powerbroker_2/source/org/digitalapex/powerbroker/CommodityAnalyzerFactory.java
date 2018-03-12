package org.digitalapex.powerbroker;

public class CommodityAnalyzerFactory {
    public static CommodityAnalyzer generate() {
        return new SimpleCommodityAnalyzer();
    }
}
