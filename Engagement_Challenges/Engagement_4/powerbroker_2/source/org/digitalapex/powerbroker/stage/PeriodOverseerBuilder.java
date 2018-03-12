package org.digitalapex.powerbroker.stage;

import org.digitalapex.powerbroker.CommodityGoBetween;
import org.digitalapex.talkers.TalkersIdentity;

public class PeriodOverseerBuilder {
    private CommodityGoBetween commodityGoBetween;
    private TalkersIdentity identity;

    public PeriodOverseerBuilder setCommodityGoBetween(CommodityGoBetween commodityGoBetween) {
        this.commodityGoBetween = commodityGoBetween;
        return this;
    }

    public PeriodOverseerBuilder defineIdentity(TalkersIdentity identity) {
        this.identity = identity;
        return this;
    }

    public PeriodOverseer generatePeriodOverseer() {
        return new PeriodOverseer(identity, commodityGoBetween);
    }
}