package net.robotictip.dropbys.persist;

import net.robotictip.protocols.SenderReceiversTrouble;
import net.robotictip.dropbys.Chatee;

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
        File fileOfUsers = fetchFilePath().toFile();
        if (!fileOfUsers.exists()) {
            WithMiConnectionsServiceManager(fileOfUsers);
        }
    }

    private void WithMiConnectionsServiceManager(File fileOfUsers) throws IOException {
        fileOfUsers.getParentFile().mkdir();
        fileOfUsers.createNewFile();
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path fetchFilePath() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param user to add to our file
     * @return List of all known users
     */
    public List<Chatee> addUserToFile(Chatee user) throws SenderReceiversTrouble {
        String serializedIdentity = WithMiUserSerializer.serialize(user);
        try {
            Files.write(fetchFilePath(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, Chatee> namesToUsers = readInPreviousConnections();
            return new ArrayList<>(namesToUsers.values());
        } catch (IOException e) {
            throw new SenderReceiversTrouble(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, Chatee> readInPreviousConnections() throws SenderReceiversTrouble {
        try {
            List<String> linesOfFile = Files.readAllLines(fetchFilePath(), StandardCharsets.UTF_8);
            Map<String, Chatee> userMap = new HashMap<>();
            for (int j = 0; j < linesOfFile.size(); j++) {
                readInPreviousConnectionsHelper(linesOfFile, userMap, j);
            }
            return userMap;
        } catch (IOException e) {
            throw new SenderReceiversTrouble(e);
        }
    }

    private void readInPreviousConnectionsHelper(List<String> linesOfFile, Map<String, Chatee> userMap, int a) {
        String line = linesOfFile.get(a);
        Chatee user = WithMiUserSerializer.deserialize(line);
        userMap.put(user.obtainName(), user);
    }

}
