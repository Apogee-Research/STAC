package com.roboticcusp.mapping;

public class ChartSize {
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

        public int takeMinimumSize() {
            return leastSize;
        }

        public int fetchMaximumSize() {
            return maxSize;
        }

        public boolean containsSize(int size) {
            return leastSize <= size && size < maxSize;
        }

        public static Size fromInt(int size) {
            Size[] values = Size.values();
            for (int j = 0; j < values.length; j++) {
                Size sizeEnum = values[j];
                if (sizeEnum.containsSize(size)) {
                    return sizeEnum;
                }
            }
            // if no Size was found, return null
            return null;
        }
    }
}

