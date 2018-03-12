package net.roboticapex.broker;

public class ProductEvaluatorFactory {
    public static ProductEvaluator make() {
        return new SimpleProductEvaluator();
    }
}
