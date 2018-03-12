package org.digitaltip.chatroom.store;

import org.digitaltip.dialogs.TalkersDeviation;
import org.digitaltip.chatroom.User;

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
        File fileOfCustomers = pullFilePath().toFile();
        if (!fileOfCustomers.exists()) {
            fileOfCustomers.getParentFile().mkdir();
            fileOfCustomers.createNewFile();
        }
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path pullFilePath() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param customer to add to our file
     * @return List of all known users
     */
    public List<User> addCustomerToFile(User customer) throws TalkersDeviation {
        String serializedIdentity = WithMiCustomerSerializer.serialize(customer);
        try {
            Files.write(pullFilePath(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, User> namesToCustomers = readInPreviousConnections();
            return new ArrayList<>(namesToCustomers.values());
        } catch (IOException e) {
            throw new TalkersDeviation(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, User> readInPreviousConnections() throws TalkersDeviation {
        try {
            List<String> linesOfFile = Files.readAllLines(pullFilePath(), StandardCharsets.UTF_8);
            Map<String, User> customerMap = new HashMap<>();
            for (int c = 0; c < linesOfFile.size(); c++) {
                String line = linesOfFile.get(c);
                User customer = WithMiCustomerSerializer.deserialize(line);
                customerMap.put(customer.takeName(), customer);
            }
            return customerMap;
        } catch (IOException e) {
            throw new TalkersDeviation(e);
        }
    }

}
