package net.roboticapex.broker;

import net.roboticapex.senderReceivers.SenderReceiversPublicIdentity;

/**
 * An auction paired with the person holding it
 */
public class ProductLiaisonTrade implements Comparable<ProductLiaisonTrade> {
    public final String id;
    public final SenderReceiversPublicIdentity seller;
    public final int productAmount;

    public ProductLiaisonTrade(String id, SenderReceiversPublicIdentity seller, int productAmount) {
        this.id = id;
        this.seller = seller;
        this.productAmount = productAmount;
    }

    @Override
    public int compareTo(ProductLiaisonTrade other) {
        if (other.productAmount != productAmount) {
            return Integer.compare(productAmount, other.productAmount);
        }
        if (!other.id.equals(id)) {
            return id.compareTo(other.id);
        }
        if (!other.seller.equals(seller)) {
            return seller.compareTo(other.seller);
        }
        return 0; // they're equal
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductLiaisonTrade that = (ProductLiaisonTrade) o;

        if (productAmount != that.productAmount) return false;
        if (!id.equals(that.id)) return false;
        return seller.equals(that.seller);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + seller.hashCode();
        result = 31 * result + productAmount;
        return result;
    }

    @Override
    public String toString() {
        return "PowerBrokerAuction{" +
                "id='" + id + '\'' +
                ", seller=" + seller +
                ", powerAmount=" + productAmount +
                '}';
    }
}
