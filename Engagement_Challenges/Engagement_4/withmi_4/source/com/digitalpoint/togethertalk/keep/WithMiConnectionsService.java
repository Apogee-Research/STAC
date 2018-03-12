package com.digitalpoint.togethertalk.keep;

import com.digitalpoint.dialogs.SenderReceiversException;
import com.digitalpoint.togethertalk.Participant;

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
        File fileOfMembers = grabFilePath().toFile();
        if (!fileOfMembers.exists()) {
            fileOfMembers.getParentFile().mkdir();
            fileOfMembers.createNewFile();
        }
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path grabFilePath() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param member to add to our file
     * @return List of all known users
     */
    public List<Participant> addMemberToFile(Participant member) throws SenderReceiversException {
        String serializedIdentity = WithMiMemberSerializer.serialize(member);
        try {
            Files.write(grabFilePath(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, Participant> namesToMembers = readInPreviousConnections();
            return new ArrayList<>(namesToMembers.values());
        } catch (IOException e) {
            throw new SenderReceiversException(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, Participant> readInPreviousConnections() throws SenderReceiversException {
        try {
            List<String> linesOfFile = Files.readAllLines(grabFilePath(), StandardCharsets.UTF_8);
            Map<String, Participant> memberMap = new HashMap<>();
            for (int j = 0; j < linesOfFile.size(); j++) {
                String line = linesOfFile.get(j);
                Participant member = WithMiMemberSerializer.deserialize(line);
                memberMap.put(member.grabName(), member);
            }
            return memberMap;
        } catch (IOException e) {
            throw new SenderReceiversException(e);
        }
    }

}
