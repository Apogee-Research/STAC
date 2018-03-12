package net.cybertip.home;

import net.cybertip.routing.framework.Airline;
import net.cybertip.routing.keep.AirDatabase;
import net.cybertip.DESHelper;

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
            airlines = AirPlanLoader.takeAirlines(inputStream, database, passwordKey);
        }

        for (Airline airline: airlines.values()) {
            database.addOrUpdateAirline(airline);
        }
    }

    private static Map<String, Airline> takeAirlines(InputStream inputStream, AirDatabase database, String passwordKey) throws IOException {
        Map<String, Airline> airlines = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            // each line represents an Airline and has the format
            // <airline id>, <airline name>, <airline password>
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",", 3);
                String id = parts[0];
                String airlineName = parts[1];
                String password = parts[2];

                // encrypt the password
                String encryptedPw = DESHelper.takeEncryptedString(password, passwordKey);
                Airline airline = new Airline(database, id, airlineName, encryptedPw);

                airlines.put(id, airline);
            }
        }
        return airlines;
    }
}