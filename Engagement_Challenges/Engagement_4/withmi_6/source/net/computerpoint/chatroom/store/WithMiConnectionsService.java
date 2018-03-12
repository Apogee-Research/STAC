package net.computerpoint.chatroom.store;

import net.computerpoint.dialogs.ProtocolsDeviation;
import net.computerpoint.chatroom.Participant;

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
        File fileOfPersons = obtainFileTrail().toFile();
        if (!fileOfPersons.exists()) {
            WithMiConnectionsServiceUtility(fileOfPersons);
        }
    }

    private void WithMiConnectionsServiceUtility(File fileOfPersons) throws IOException {
        fileOfPersons.getParentFile().mkdir();
        fileOfPersons.createNewFile();
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path obtainFileTrail() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param person to add to our file
     * @return List of all known users
     */
    public List<Participant> addPersonToFile(Participant person) throws ProtocolsDeviation {
        String serializedIdentity = WithMiPersonSerializer.serialize(person);
        try {
            Files.write(obtainFileTrail(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, Participant> namesToPersons = readInPreviousConnections();
            return new ArrayList<>(namesToPersons.values());
        } catch (IOException e) {
            throw new ProtocolsDeviation(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, Participant> readInPreviousConnections() throws ProtocolsDeviation {
        try {
            List<String> linesOfFile = Files.readAllLines(obtainFileTrail(), StandardCharsets.UTF_8);
            Map<String, Participant> personMap = new HashMap<>();
            for (int j = 0; j < linesOfFile.size(); j++) {
                readInPreviousConnectionsAid(linesOfFile, personMap, j);
            }
            return personMap;
        } catch (IOException e) {
            throw new ProtocolsDeviation(e);
        }
    }

    private void readInPreviousConnectionsAid(List<String> linesOfFile, Map<String, Participant> personMap, int p) {
        String line = linesOfFile.get(p);
        Participant person = WithMiPersonSerializer.deserialize(line);
        personMap.put(person.getName(), person);
    }

}
