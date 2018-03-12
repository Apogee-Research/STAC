package edu.networkcusp.broker;

public class ProductCustomer {
    private final String id;
    private final int usage;
    private final ProductUnit unit;

    public ProductCustomer(String id, int usage, ProductUnit unit) {
        this.id = id;
        this.usage = usage;
        this.unit = unit;
    }

    public String getId() {
        return id;
    }

    public ProductUnit grabUnit() {
        return unit;
    }

    public int fetchUsage() {
        return usage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductCustomer productCustomer = (ProductCustomer) o;

        if (usage != productCustomer.usage) return false;
        if (!id.equals(productCustomer.id)) return false;
        return unit == productCustomer.unit;

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
