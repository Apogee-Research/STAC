package org.digitalapex.powerbroker;

import org.digitalapex.json.simple.PARSERObject;

public class CommodityCreator {
    private final String id;
    private final int capacity;
    private final boolean divisible;
    private final int costPerUnit;
    private final CommodityStatus status;
    private final CommodityUnit unit;

    public CommodityCreator(String id, CommodityStatus status, int capacity, CommodityUnit unit, boolean divisible, int costPerUnit) {
        this.id = id;
        this.status = status;
        if (capacity < 0) {
            CommodityCreatorAssist(capacity);
        }
        this.capacity = capacity;
        this.unit = unit;
        this.divisible = divisible;
        if (costPerUnit < 0) {
            new CommodityCreatorAssist(costPerUnit).invoke();
        }
        this.costPerUnit = costPerUnit;
    }

    private void CommodityCreatorAssist(int capacity) {
        throw new RuntimeException("capacity cannot be < 0, is " + capacity);
    }

    /**
     * Returns a PowerGenerator object after reading it from the provided map of the form:
     * {"id": "generator1", "status": "ONLINE", "capacity": "400", "units": "kWh", "divisible": "false", "cost_per_unit": "50"}
     *
     * @param map
     * @return
     */
    public static CommodityCreator fromParser(PARSERObject map) {
        String id = (String) map.get("id");
        CommodityStatus status = CommodityStatus.valueOf(((String) map.get("status")).toUpperCase());
        int capacity = Integer.valueOf((String) map.get("capacity"));
        CommodityUnit unit = CommodityUnit.valueOf((String) map.get("units"));
        boolean divisible = Boolean.valueOf((String) map.get("divisible"));
        int costPerUnit = Integer.valueOf((String) map.get("cost_per_unit"));

        return new CommodityCreator(id, status, capacity, unit, divisible, costPerUnit);
    }

    public int takeCapacity() {
        return capacity;
    }

    public int fetchCostPerUnit() {
        return costPerUnit;
    }

    public boolean isDivisible() {
        return divisible;
    }

    public String fetchId() {
        return id;
    }

    public CommodityStatus grabStatus() {
        return status;
    }

    public CommodityUnit fetchUnit() {
        return unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommodityCreator that = (CommodityCreator) o;

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

    private class CommodityCreatorAssist {
        private int costPerUnit;

        public CommodityCreatorAssist(int costPerUnit) {
            this.costPerUnit = costPerUnit;
        }

        public void invoke() {
            throw new RuntimeException("cost per unit cannot be < 0, is " + costPerUnit);
        }
    }
}
