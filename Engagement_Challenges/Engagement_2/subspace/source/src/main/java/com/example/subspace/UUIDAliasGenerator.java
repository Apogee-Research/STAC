package com.example.subspace;

import java.util.UUID;

public class UUIDAliasGenerator
    implements AliasGenerator
{
    @Override
    public String generateAlias()
    {
        return UUID.randomUUID().toString();
    }
}
