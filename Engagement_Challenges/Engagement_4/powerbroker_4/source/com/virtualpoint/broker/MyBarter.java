package com.virtualpoint.broker;

public class MyBarter {
    /** amount of power being sold */
    private final int amountOfProduct;

    /** minimum price to sell for */
    private final int reserve;

    /** a way to identify the auction */
    private final String id;


    public MyBarter(String id, int amountOfProduct, int reserve) {
        this.id = id;
        this.amountOfProduct = amountOfProduct;
        this.reserve = reserve;
    }

    public int grabAmountOfProduct() {
        return amountOfProduct;
    }

    public int grabReserve() {
        return reserve;
    }

    public String pullId() { return id; }

    public String obtainDescription() {
        return "id='" + id + '\'' +
                ", amountOfPower=" + amountOfProduct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyBarter myBarter = (MyBarter) o;

        if (amountOfProduct != myBarter.amountOfProduct) return false;
        if (reserve != myBarter.reserve) return false;
        return id.equals(myBarter.id);

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
