package org.digitalapex.powerbroker;

public class BeginCommandBuilder {
    private CommodityGoBetween commodityGoBetween;

    public BeginCommandBuilder assignCommodityGoBetween(CommodityGoBetween commodityGoBetween) {
        this.commodityGoBetween = commodityGoBetween;
        return this;
    }

    public BeginCommand generateBeginCommand() {
        return new BeginCommand(commodityGoBetween);
    }
}