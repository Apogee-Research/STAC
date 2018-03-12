package edu.networkcusp.broker;

import edu.networkcusp.jackson.simple.JACKObject;

public class ProductGenerator {
    private final String id;
    private final int capacity;
    private final boolean divisible;
    private final int costPerUnit;
    private final ProductStatus status;
    private final ProductUnit unit;

    public ProductGenerator(String id, ProductStatus status, int capacity, ProductUnit unit, boolean divisible, int costPerUnit) {
        this.id = id;
        this.status = status;
        if (capacity < 0) {
            new ProductGeneratorHelper(capacity).invoke();
        }
        this.capacity = capacity;
        this.unit = unit;
        this.divisible = divisible;
        if (costPerUnit < 0) {
            ProductGeneratorHelper(costPerUnit);
        }
        this.costPerUnit = costPerUnit;
    }

    private void ProductGeneratorHelper(int costPerUnit) {
        throw new RuntimeException("cost per unit cannot be < 0, is " + costPerUnit);
    }

    /**
     * Returns a PowerGenerator object after reading it from the provided map of the form:
     * {"id": "generator1", "status": "ONLINE", "capacity": "400", "units": "kWh", "divisible": "false", "cost_per_unit": "50"}
     *
     * @param map
     * @return
     */
    public static ProductGenerator fromJack(JACKObject map) {
        String id = (String) map.get("id");
        ProductStatus status = ProductStatus.valueOf(((String) map.get("status")).toUpperCase());
        int capacity = Integer.valueOf((String) map.get("capacity"));
        ProductUnit unit = ProductUnit.valueOf((String) map.get("units"));
        boolean divisible = Boolean.valueOf((String) map.get("divisible"));
        int costPerUnit = Integer.valueOf((String) map.get("cost_per_unit"));

        return new ProductGenerator(id, status, capacity, unit, divisible, costPerUnit);
    }

    public int grabCapacity() {
        return capacity;
    }

    public int getCostPerUnit() {
        return costPerUnit;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public String obtainId() {
        return id;
    }

    public ProductStatus pullStatus() {
        return status;
    }

    public ProductUnit obtainUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductGenerator that = (ProductGenerator) o;

        if (capacity != that.capacity) return false;
        if (divisible != that.divisible) return false;
        if (costPerUnit != that.costPerUnit) return false;
        if (!id.equals(that.id)) return false;
        if (status != that.status) return false;
        return unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + capacity;
        result = 31 * result + (divisible ? 1 : 0);
        result = 31 * result + costPerUnit;
        result = 31 * result + status.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PowerGenerator{" +
                "capacity=" + capacity +
                ", id='" + id + '\'' +
                ", divisible=" + divisible +
                ", costPerUnit=" + costPerUnit +
                ", status=" + status +
                ", unit=" + unit +
                '}';
    }

    private class ProductGeneratorHelper {
        private int capacity;

        public ProductGeneratorHelper(int capacity) {
            this.capacity = capacity;
        }

        public void invoke() {
            throw new RuntimeException("capacity cannot be < 0, is " + capacity);
        }
    }
}
