package com.tweeter.service;

/**
 * The store-all matcher is an oracle who tells the asking algorithm that they may always store and never terminate early.
 */
public class StoreAllMatcher implements IMatcher {
    @Override
    public boolean returnMatch(String s) {
        return false;
    }

    @Override
    public boolean storeMatch(String s) {
        return true;
    }
}
