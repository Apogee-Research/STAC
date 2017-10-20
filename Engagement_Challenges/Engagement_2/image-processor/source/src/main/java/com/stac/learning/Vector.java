package com.stac.learning;

import java.io.InvalidObjectException;
import java.util.Objects;

/**
 *
 */
abstract public class Vector {
    final float[] attributes;
    /**
     * Constructs a vector of size n.
     * @param n the size of the vector.
     */
    Vector(int n) {
        attributes = new float[n];
    }

    /**
     * Return the vector's size.
     * @return The size.
     */
    final public int size() {
        return attributes.length;
    }

    final float[] getAttributes() {
        return attributes;
    }

    public abstract float compareTo(Vector other);

    public static class VectorBuilder {
        private int trackedIndex = 0;
        private final VectorFactory ctor;
        private Vector vector;

        /**
         * Begins the construction of a vector.
         * @param size The size of the vector.
         * @param vectorFactory This vector factory decides what type of vector is used.
         */
        public VectorBuilder(VectorFactory vectorFactory, int size) {
            this.ctor = Objects.requireNonNull(vectorFactory, "Vector Supplier must not be null");
            vector = ctor.get(size);
        }

        /**
         * Adds an attribute to the vector.
         * @param attr The attribute to add. This should be in the range [0,1]
         * @return This vector builder.
         */
        public VectorBuilder add(float attr) {
            if (attr >= 0f && attr <= 1f) {
                vector.attributes[trackedIndex++] = attr;
            } else {
                throw new IllegalArgumentException("Attribute 'attr': must be in the range (0,1)");
            }
            return this;
        }

        /**
         * Builds the Vector.
         * @return a Vector object.
         * @throws InvalidObjectException Occurs when the Vector has already been built.
         */
        public Vector build() throws InvalidObjectException {
            if (vector == null) {
                throw new InvalidObjectException("This vector has been built.");
            }
            Vector v = vector;
            vector = null;
            return v;
        }

    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Vector [");
        int i = 0;
        for (; i < this.attributes.length - 1; i++) {
            sb.append(this.attributes[i]).append(", ");
        }
        sb.append(this.attributes[i]);
        sb.append("]");
        return sb.toString();
    }
}
