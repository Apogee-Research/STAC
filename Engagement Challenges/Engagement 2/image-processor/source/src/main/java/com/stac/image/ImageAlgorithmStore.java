package com.stac.image;

import com.stac.image.algorithms.detectors.*;
import com.stac.image.algorithms.filters.*;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ImageAlgorithmStore {
    private static Map<String, ImageAlgorithm> store = new HashMap<>();

    static {
        addAlgorithm(BlackDetector.class);
        addAlgorithm(BlueDetector.class);
        addAlgorithm(EdgingDetector.class);
        addAlgorithm(GreenDetector.class);
        addAlgorithm(RedDetector.class);
        addAlgorithm(WhiteDetector.class);
        addAlgorithm(Intensify.class);
        addAlgorithm(Invert.class);
    }

    private static void addAlgorithm(Class<? extends ImageAlgorithm> algo) {
        try {
            store.put(algo.getSimpleName(), algo.newInstance());
        } catch (InstantiationException|IllegalAccessException e) {
            throw new RuntimeException("Fatal initialization error has occurred");
        }
    }

    public static ImageAlgorithm getAlgorithm(String name) {
        return store.get(name);
    }
}
