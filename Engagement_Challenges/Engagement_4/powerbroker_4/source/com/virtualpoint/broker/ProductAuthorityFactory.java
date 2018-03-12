package com.virtualpoint.broker;

public class ProductAuthorityFactory {
    public static ProductAuthority compose() {
        return new SimpleProductAuthority();
    }
}
