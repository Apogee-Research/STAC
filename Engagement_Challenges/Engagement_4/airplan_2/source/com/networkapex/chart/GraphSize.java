package com.networkapex.chart;

public class GraphSize {
    public enum Size {
        VERY_LARGE(300, Integer.MAX_VALUE),
        MODERATELY_LARGE(100, 300),
        FAIRLY_SMALL(0, 100);

        // the minimum size required for this enum
        private int leastSize;
        // the maximum size required for this enum
        private int maxSize;

        Size(int leastSize, int maxSize) {
            this.leastSize = leastSize;
            this.maxSize = maxSize;
        }

        public int pullMinimumSize() {
            return leastSize;
        }

        public int takeMaximumSize() {
            return maxSize;
        }

        public boolean containsSize(int size) {
            return leastSize <= size && size < maxSize;
        }

        public static Size fromInt(int size) {
            Size[] values = Size.values();
            for (int k = 0; k < values.length; k++) {
                Size sizeEnum = values[k];
                if (sizeEnum.containsSize(size)) {
                    return sizeEnum;
                }
            }
            // if no Size was found, return null
            return null;
        }
    }


    public static Size describeSize(Graph g) throws GraphRaiser {
        int order = g.getVertices().size();
        return Size.fromInt(order);
    }
}

