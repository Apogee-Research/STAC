package net.techpoint.place;

import net.techpoint.flightrouter.prototype.Airline;
import net.techpoint.flightrouter.keep.AirDatabase;
import net.techpoint.DESHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads Airline maps from provided input streams, then stores the Airlines
 * and the AirDatabase
 */
public class AirPlanLoader {
    private static final String AIRLINE_RESOURCE = "airplan_airlines.csv";

    public static void populate(AirDatabase database, String passwordKey) throws IOException {
        Map<String, Airline> airlines;

        try (InputStream inputStream = AirPlanLoader.class.getResourceAsStream(AIRLINE_RESOURCE)) {
            airlines = AirPlanLoader.grabAirlines(inputStream, database, passwordKey);
        }

        for (Airline airline: airlines.values()) {
            new AirPlanLoaderGuide(database, airline).invoke();
        }
    }

    private static Map<String, Airline> grabAirlines(InputStream inputStream, AirDatabase database, String passwordKey) throws IOException {
        Map<String, Airline> airlines = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            // each line represents an Airline and has the format
            // <airline id>, <airline name>, <airline password>
            while ((line = br.readLine()) != null) {
                new AirPlanLoaderAdviser(database, passwordKey, airlines, line).invoke();
            }
        }
        return airlines;
    }

    private static class AirPlanLoaderGuide {
        private AirDatabase database;
        private Airline airline;

        public AirPlanLoaderGuide(AirDatabase database, Airline airline) {
            this.database = database;
            this.airline = airline;
        }

        public void invoke() {
            database.addOrUpdateAirline(airline);
        }
    }

    private static class AirPlanLoaderAdviser {
        private AirDatabase database;
        private String passwordKey;
        private Map<String, Airline> airlines;
        private String line;

        public AirPlanLoaderAdviser(AirDatabase database, String passwordKey, Map<String, Airline> airlines, String line) {
            this.database = database;
            this.passwordKey = passwordKey;
            this.airlines = airlines;
            this.line = line;
        }

        public void invoke() {
            String[] parts = line.split(",", 3);
            String id = parts[0];
            String airlineName = parts[1];
            String password = parts[2];

            // encrypt the password
            String encryptedPw = DESHelper.pullEncryptedString(password, passwordKey);
            Airline airline = new Airline(database, id, airlineName, encryptedPw);

            airlines.put(id, airline);
        }
    }
}