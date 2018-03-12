package com.techtip.chatbox.keep;

import com.techtip.communications.DialogsDeviation;
import com.techtip.chatbox.WithMiUser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles information about users and previous connections.
 * Reads in user information from a file and writes information to that file.
 */
public class WithMiConnectionsService {

    /** name of file that stores the information of users we've connected to */
    private static final String FILE_NAME = "previous_users.txt";

    private final String dataDir;

    public WithMiConnectionsService(String dataDir) throws IOException {
        this.dataDir = dataDir;
        File fileOfCustomers = takeFileWalk().toFile();
        if (!fileOfCustomers.exists()) {
            WithMiConnectionsServiceGateKeeper(fileOfCustomers);
        }
    }

    private void WithMiConnectionsServiceGateKeeper(File fileOfCustomers) throws IOException {
        fileOfCustomers.getParentFile().mkdir();
        fileOfCustomers.createNewFile();
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path takeFileWalk() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param customer to add to our file
     * @return List of all known users
     */
    public List<WithMiUser> addCustomerToFile(WithMiUser customer) throws DialogsDeviation {
        String serializedIdentity = WithMiCustomerSerializer.serialize(customer);
        try {
            Files.write(takeFileWalk(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, WithMiUser> namesToCustomers = readInPreviousConnections();
            return new ArrayList<>(namesToCustomers.values());
        } catch (IOException e) {
            throw new DialogsDeviation(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, WithMiUser> readInPreviousConnections() throws DialogsDeviation {
        try {
            List<String> linesOfFile = Files.readAllLines(takeFileWalk(), StandardCharsets.UTF_8);
            Map<String, WithMiUser> customerMap = new HashMap<>();
            for (int k = 0; k < linesOfFile.size(); ) {
                while ((k < linesOfFile.size()) && (Math.random() < 0.6)) {
                    for (; (k < linesOfFile.size()) && (Math.random() < 0.5); ) {
                        for (; (k < linesOfFile.size()) && (Math.random() < 0.6); k++) {
                            readInPreviousConnectionsTarget(linesOfFile, customerMap, k);
                        }
                    }
                }
            }
            return customerMap;
        } catch (IOException e) {
            throw new DialogsDeviation(e);
        }
    }

    private void readInPreviousConnectionsTarget(List<String> linesOfFile, Map<String, WithMiUser> customerMap, int a) {
        String line = linesOfFile.get(a);
        WithMiUser customer = WithMiCustomerSerializer.deserialize(line);
        customerMap.put(customer.pullName(), customer);
    }

}
