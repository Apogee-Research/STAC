package fi.iki.elonen.HTTP;

/*
 * #%L
 * NanoHttpd-Webserver-Java-Plugin
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TokenResolver based on a Map.
 * Acts as an Adapter from {@link ITokenResolver} to {@link Map}
 */
public class MapTokenResolver implements ITokenResolver, Map<String, String> {

    protected Map<String,String> tokenMap = null;

    /**
     * Construct a TokenResolver from a Map.
     * @param tokenMap the map to use as the mapping from token to value.
     */
    public MapTokenResolver(Map<String,String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    /**
     * Constructs a TokenResolver with a new internal map.
     */
    public MapTokenResolver() {
        this.tokenMap = new HashMap<String,String>();
    }

    /**
     * Returns the value associated with a token.
     * @param tokenName The token label.
     * @return The token value.
     */
    public String resolveToken(String tokenName) {
        return this.tokenMap.get(tokenName);
    }

    @Override
    public int size() {
        return tokenMap.size();
    }

    @Override
    public boolean isEmpty() {
        return tokenMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return tokenMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return tokenMap.containsValue(value);
    }

    @Override
    public String get(Object key) {
        return tokenMap.get(key);
    }

    @Override
    public String put(String key, String value) {
        return tokenMap.put(key, value);
    }

    public String put(String key, HTTPUnit unit) {
        return tokenMap.put(key, unit.toString());
    }

    @Override
    public String remove(Object key) {
        return tokenMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        tokenMap.putAll(m);
    }

    @Override
    public void clear() {
        tokenMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return tokenMap.keySet();
    }

    @Override
    public Collection<String> values() {
        return tokenMap.values();
    }

    @Override
    public Set<Map.Entry<String, String>> entrySet() {
        return tokenMap.entrySet();
    }
}