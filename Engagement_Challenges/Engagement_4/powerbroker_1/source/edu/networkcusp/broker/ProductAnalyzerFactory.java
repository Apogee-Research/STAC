package edu.networkcusp.broker;

public class ProductAnalyzerFactory {
    public static ProductAnalyzer form() {
        return new SimpleProductAnalyzer();
    }
}
