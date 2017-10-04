package com.nicnilov.textmeter.ngrams.storage;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.nicnilov.textmeter.ngrams.NgramType;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 26.10.13 at 0:03
 */
final class HashMapStorage extends NgramStorage {

    public HashMapStorage(NgramType ngramType, int sizeHint) {
        super(ngramType);
        this.storage = new  HashMap(sizeHint < DEFAULT_SIZE_HINT ? DEFAULT_SIZE_HINT : sizeHint);
    }

    @Override
    public NgramStorageStrategy getStorageStrategy() {
        return NgramStorageStrategy.HASHMAP;
    }
}
