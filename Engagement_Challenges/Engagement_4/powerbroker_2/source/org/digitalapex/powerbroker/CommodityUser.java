package org.digitalapex.powerbroker;

import org.digitalapex.json.simple.PARSERObject;

public class CommodityUser {
    private final String id;
    private final int usage;
    private final CommodityUnit unit;

    public CommodityUser(String id, int usage, CommodityUnit unit) {
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
    public static CommodityUser fromParser(PARSERObject userMap) throws CommodityGoBetweenRaiser {
        String id = (String) userMap.get("id");
        int usage = Integer.valueOf((String) userMap.get("usage"));
        if (usage < 0) {
            throw new CommodityGoBetweenRaiser("Usage cannot be less than 0, but is: " + usage);
        }
        CommodityUnit unit = CommodityUnit.valueOf((String) userMap.get("units"));
        return new CommodityUser(id, usage, unit);
    }

    public String pullId() {
        return id;
    }

    public CommodityUnit grabUnit() {
        return unit;
    }

    public int getUsage() {
        return usage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommodityUser commodityUser = (CommodityUser) o;

        if (usage != commodityUser.usage) return false;
        if (!id.equals(commodityUser.id)) return false;
        return unit == commodityUser.unit;

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
