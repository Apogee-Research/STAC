package com.stac.learning;

import java.util.Objects;

/**
 *
 */
public class EuclideanVector extends Vector {
    /**
     * Constructs a vector of size n.
     *
     * @param n the size of the vector.
     */
    EuclideanVector(int n) {
        super(n);
    }

    @Override
    public float compareTo(Vector other) {
        Objects.requireNonNull(attributes);
        Objects.requireNonNull(other.attributes);

        if (attributes.length != other.attributes.length) {
            throw new IllegalArgumentException("Vector lengths do not match.");
        }

        if (!attributes.getClass().getComponentType().equals(other.attributes.getClass().getComponentType())) {
            throw new IllegalArgumentException("Arrays must be of the same type.");
        }

        double sum = 0;
        for (int i = 0; i < attributes.length; i++) {
            sum += (attributes[i] - other.attributes[i]) * (attributes[i] - other.attributes[i]);
        }
        return (float) (1.0 - Math.sqrt(sum));
    }
}
