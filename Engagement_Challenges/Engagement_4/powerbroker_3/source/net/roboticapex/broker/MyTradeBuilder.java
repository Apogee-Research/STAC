package net.roboticapex.broker;

public class MyTradeBuilder {
    private int amountOfProduct;
    private String id;
    private int reserve;

    public MyTradeBuilder setAmountOfProduct(int amountOfProduct) {
        this.amountOfProduct = amountOfProduct;
        return this;
    }

    public MyTradeBuilder defineId(String id) {
        this.id = id;
        return this;
    }

    public MyTradeBuilder setReserve(int reserve) {
        this.reserve = reserve;
        return this;
    }

    public MyTrade makeMyTrade() {
        return new MyTrade(id, amountOfProduct, reserve);
    }
}