package com.example.subspace;

/**
 * Interface for classes that generate alias names.
 */
public interface AliasGenerator
{
    /**
     * Return an alias name that's unlikely to already be in use.
     */
    public String generateAlias();
}
