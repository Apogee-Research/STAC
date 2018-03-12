package net.techpoint.flightrouter.manager;

public class CellFormatterBuilder {
    private int length;

    public CellFormatterBuilder assignLength(int length) {
        this.length = length;
        return this;
    }

    public CellFormatter formCellFormatter() {
        return new CellFormatter(length);
    }
}