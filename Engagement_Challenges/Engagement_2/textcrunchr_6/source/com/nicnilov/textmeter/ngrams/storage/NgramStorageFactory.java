package com.nicnilov.textmeter.ngrams.storage;

import com.nicnilov.textmeter.NotImplementedException;
import com.nicnilov.textmeter.ngrams.NgramType;

/**
 * Created as part of textmeter project
 * by Nic Nilov on 26.10.13 at 0:22
 */
public class NgramStorageFactory {

    public static NgramStorage get(NgramType ngramType, NgramStorageStrategy ngramStorageStrategy, int sizeHint) {
        NgramStorage ngramStorage;
        switch(ngramStorageStrategy) {
            case HASHMAP:
                {
                    ngramStorage = new  HashMapStorage(ngramType, sizeHint);
                    break;
                }
            case TREEMAP:
                {
                    ngramStorage = new  TreeMapStorage(ngramType);
                    break;
                }
            //            }
            default:
                {
                    throw new  NotImplementedException();
                }
        }
        return ngramStorage;
    }
}
