package edu.networkcusp.broker;

public class MyAuction {
    /** amount of power being sold */
    private final int amountOfProduct;

    /** minimum price to sell for */
    private final int reserve;

    /** a way to identify the auction */
    private final String id;


    public MyAuction(String id, int amountOfProduct, int reserve) {
        this.id = id;
        this.amountOfProduct = amountOfProduct;
        this.reserve = reserve;
    }

    public int pullAmountOfProduct() {
        return amountOfProduct;
    }

    public int obtainReserve() {
        return reserve;
    }

    public String getId() { return id; }

    public String getDescription() {
        return "id='" + id + '\'' +
                ", amountOfPower=" + amountOfProduct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyAuction myAuction = (MyAuction) o;

        if (amountOfProduct != myAuction.amountOfProduct) return false;
        if (reserve != myAuction.reserve) return false;
        return id.equals(myAuction.id);

    }

    @Override
    public int hashCode() {
        int result = amountOfProduct;
        result = 31 * result + reserve;
        result = 31 * result + id.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "id='" + id + '\'' +
                ", amountOfPower=" + amountOfProduct +
                ", reserve=" + reserve;
    }
}
