package com.virtualpoint.broker;

import com.virtualpoint.part.simple.PLUGINObject;

public class ProductUser {
    private final String id;
    private final int usage;
    private final ProductUnit unit;

    public ProductUser(String id, int usage, ProductUnit unit) {
        this.id = id;
        this.usage = usage;
        this.unit = unit;
    }

    /**
     * Returns a PowerUser from a map like "id": "subscriber1", "usage": "10", "units": "kWh"
     *
     * @param userMap the map to read from
     * @return PowerUser object
     */
    public static ProductUser fromPlugin(PLUGINObject userMap) throws ProductIntermediaryTrouble {
        String id = (String) userMap.get("id");
        int usage = Integer.valueOf((String) userMap.get("usage"));
        if (usage < 0) {
            throw new ProductIntermediaryTrouble("Usage cannot be less than 0, but is: " + usage);
        }
        ProductUnit unit = ProductUnit.valueOf((String) userMap.get("units"));
        return new ProductUser(id, usage, unit);
    }

    public String getId() {
        return id;
    }

    public ProductUnit pullUnit() {
        return unit;
    }

    public int grabUsage() {
        return usage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductUser productUser = (ProductUser) o;

        if (usage != productUser.usage) return false;
        if (!id.equals(productUser.id)) return false;
        return unit == productUser.unit;

    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + usage;
        result = 31 * result + unit.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PowerUser{" +
                "id='" + id + '\'' +
                ", usage=" + usage +
                ", unit=" + unit +
                '}';
    }
}
