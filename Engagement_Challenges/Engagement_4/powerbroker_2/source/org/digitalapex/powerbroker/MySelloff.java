package org.digitalapex.powerbroker;

public class MySelloff {
    /** amount of power being sold */
    private final int amountOfCommodity;

    /** minimum price to sell for */
    private final int reserve;

    /** a way to identify the auction */
    private final String id;


    public MySelloff(String id, int amountOfCommodity, int reserve) {
        this.id = id;
        this.amountOfCommodity = amountOfCommodity;
        this.reserve = reserve;
    }

    public int getAmountOfCommodity() {
        return amountOfCommodity;
    }

    public int fetchReserve() {
        return reserve;
    }

    public String grabId() { return id; }

    public String getDescription() {
        return "id='" + id + '\'' +
                ", amountOfPower=" + amountOfCommodity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MySelloff mySelloff = (MySelloff) o;

        if (amountOfCommodity != mySelloff.amountOfCommodity) return false;
        if (reserve != mySelloff.reserve) return false;
        return id.equals(mySelloff.id);

    }

    @Override
    public int hashCode() {
        int result = amountOfCommodity;
        result = 31 * result + reserve;
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "id='" + id + '\'' +
                ", amountOfPower=" + amountOfCommodity +
                ", reserve=" + reserve;
    }
}
