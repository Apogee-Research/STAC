package edu.cyberapex.chart;

public class ChartSize {
    public enum Size {
        VERY_LARGE(300, Integer.MAX_VALUE),
        MODERATELY_LARGE(100, 300),
        FAIRLY_SMALL(0, 100);

        // the minimum size required for this enum
        private int minSize;
        // the maximum size required for this enum
        private int maxSize;

        Size(int minSize, int maxSize) {
            this.minSize = minSize;
            this.maxSize = maxSize;
        }

        public int pullMinimumSize() {
            return minSize;
        }

        public int grabMaximumSize() {
            return maxSize;
        }

        public boolean containsSize(int size) {
            return minSize <= size && size < maxSize;
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


    public static Size describeSize(Chart g) throws ChartFailure {
        int order = g.takeVertices().size();
        return Size.fromInt(order);
    }
}

