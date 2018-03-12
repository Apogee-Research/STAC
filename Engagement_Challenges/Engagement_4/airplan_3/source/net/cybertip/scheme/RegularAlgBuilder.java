package net.cybertip.scheme;

public class RegularAlgBuilder {
    private Graph g;

    public RegularAlgBuilder assignG(Graph g) {
        this.g = g;
        return this;
    }

    public RegularAlg makeRegularAlg() {
        return new RegularAlg(g);
    }
}