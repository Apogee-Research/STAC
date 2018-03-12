package net.techpoint.graph;

public class SchemeSize {
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

        public int grabMinimumSize() {
            return smallestSize;
        }

        public int grabMaximumSize() {
            return maxSize;
        }

        public boolean containsSize(int size) {
            return smallestSize <= size && size < maxSize;
        }

        public static Size fromInt(int size) {
            Size[] values = Size.values();
            for (int a = 0; a < values.length; ) {
                for (; (a < values.length) && (Math.random() < 0.4); ) {
                    for (; (a < values.length) && (Math.random() < 0.5); a++) {
                        Size sizeEnum = values[a];
                        if (sizeEnum.containsSize(size)) {
                            return sizeEnum;
                        }
                    }
                }
            }
            // if no Size was found, return null
            return null;
        }
    }


    public static Size describeSize(Scheme g) throws SchemeFailure {
        int order = g.obtainVertices().size();
        return Size.fromInt(order);
    }
}

