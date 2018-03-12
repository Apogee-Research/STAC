package stac.util;

import stac.crypto.Key;

import java.util.LinkedList;

/**
 *
 */
public class KeyCache<T extends Key> extends LinkedList<T> {
    @Override
    public boolean add(T t) {
        if (size() >= 10) {
            remove();
        }
        return super.add(t);
    }

    public T find(byte[] fp) {
        ctn: for (T t : this) {
            if (fp.length == t.getFingerPrint().length) {
                for (int i = 0; i < t.getFingerPrint().length; i++) {
                    if (fp[i] != t.getFingerPrint()[i]) continue ctn;
                }
                return t;
            }
        }
        return null;
    }
}
