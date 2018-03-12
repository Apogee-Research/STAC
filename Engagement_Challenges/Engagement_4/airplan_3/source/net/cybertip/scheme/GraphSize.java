package net.cybertip.scheme;

public class GraphSize {
    public enum Size {
        VERY_LARGE(300, Integer.MAX_VALUE),
        MODERATELY_LARGE(100, 300),
        FAIRLY_SMALL(0, 100);

        // the minimum size required for this enum
        private int smallestSize;
        // the maximum size required for this enum
        private int maxSize;

        Size(int smallestSize, int maxSize) {
            this.smallestSize = smallestSize;
            this.maxSize = maxSize;
        }

        public int obtainMinimumSize() {
            return smallestSize;
        }

        public int obtainMaximumSize() {
            return maxSize;
        }

        public boolean containsSize(int size) {
            return smallestSize <= size && size < maxSize;
        }

        public static Size fromInt(int size) {
            Size[] values = Size.values();
            for (int q = 0; q < values.length; q++) {
                Size sizeEnum = values[q];
                if (sizeEnum.containsSize(size)) {
                    return sizeEnum;
                }
            }
            // if no Size was found, return null
            return null;
        }
    }


    public static Size describeSize(Graph g) throws GraphTrouble {
        int order = g.grabVertices().size();
        return Size.fromInt(order);
    }
}

