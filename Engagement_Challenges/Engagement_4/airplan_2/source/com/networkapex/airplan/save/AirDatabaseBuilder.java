package com.networkapex.airplan.save;

import java.io.File;
import java.util.Random;

public class AirDatabaseBuilder {
    private Random random = new Random();
    private File databaseFile;

    public AirDatabaseBuilder fixRandom(Random random) {
        this.random = random;
        return this;
    }

    public AirDatabaseBuilder defineDatabaseFile(File databaseFile) {
        this.databaseFile = databaseFile;
        return this;
    }

    public AirDatabase generateAirDatabase() {
        return new AirDatabase(databaseFile, random);
    }
}