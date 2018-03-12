package net.roboticapex.broker;

import net.roboticapex.parser.simple.PARSINGObject;

public class ProductProducer {
    private final String id;
    private final int accommodation;
    private final boolean divisible;
    private final int costPerUnit;
    private final ProductStatus status;
    private final ProductUnit unit;

    public ProductProducer(String id, ProductStatus status, int accommodation, ProductUnit unit, boolean divisible, int costPerUnit) {
        this.id = id;
        this.status = status;
        if (accommodation < 0) {
            ProductProducerTarget(accommodation);
        }
        this.accommodation = accommodation;
        this.unit = unit;
        this.divisible = divisible;
        if (costPerUnit < 0) {
            ProductProducerService(costPerUnit);
        }
        this.costPerUnit = costPerUnit;
    }

    private void ProductProducerService(int costPerUnit) {
        throw new RuntimeException("cost per unit cannot be < 0, is " + costPerUnit);
    }

    private void ProductProducerTarget(int accommodation) {
        throw new RuntimeException("capacity cannot be < 0, is " + accommodation);
    }

    /**
     * Returns a PowerGenerator object after reading it from the provided map of the form:
     * {"id": "generator1", "status": "ONLINE", "capacity": "400", "units": "kWh", "divisible": "false", "cost_per_unit": "50"}
     *
     * @param map
     * @return
     */
    public static ProductProducer fromParsing(PARSINGObject map) {
        String id = (String) map.get("id");
        ProductStatus status = ProductStatus.valueOf(((String) map.get("status")).toUpperCase());
        int accommodation = Integer.valueOf((String) map.get("capacity"));
        ProductUnit unit = ProductUnit.valueOf((String) map.get("units"));
        boolean divisible = Boolean.valueOf((String) map.get("divisible"));
        int costPerUnit = Integer.valueOf((String) map.get("cost_per_unit"));

        return new ProductProducer(id, status, accommodation, unit, divisible, costPerUnit);
    }

    public int pullAccommodation() {
        return accommodation;
    }

    public int pullCostPerUnit() {
        return costPerUnit;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public String takeId() {
        return id;
    }

    public ProductStatus fetchStatus() {
        return status;
    }

    public ProductUnit obtainUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductProducer that = (ProductProducer) o;

        if (accommodation != that.accommodation) return false;
        if (divisible != that.divisible) return false;
        if (costPerUnit != that.costPerUnit) return false;
        if (!id.equals(that.id)) return false;
        if (status != that.status) return false;
        return unit == that.unit;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + accommodation;
        result = 31 * result + (divisible ? 1 : 0);
        result = 31 * result + costPerUnit;
        result = 31 * result + status.hashCode();
        result = 31 * result + unit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PowerGenerator{" +
                "capacity=" + accommodation +
                ", id='" + id + '\'' +
                ", divisible=" + divisible +
                ", costPerUnit=" + costPerUnit +
                ", status=" + status +
                ", unit=" + unit +
                '}';
    }
}
