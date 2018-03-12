package net.roboticapex.broker;

public class ProductUser {
    private final String id;
    private final int usage;
    private final ProductUnit unit;

    public ProductUser(String id, int usage, ProductUnit unit) {
        this.id = id;
        this.usage = usage;
        this.unit = unit;
    }

    public String takeId() {
        return id;
    }

    public ProductUnit obtainUnit() {
        return unit;
    }

    public int getUsage() {
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
