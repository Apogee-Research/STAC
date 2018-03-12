package edu.networkcusp.chatbox.keep;

import edu.networkcusp.protocols.CommunicationsFailure;
import edu.networkcusp.chatbox.WithMiUser;

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
        File fileOfMembers = takeFilePath().toFile();
        if (!fileOfMembers.exists()) {
            new WithMiConnectionsServiceSupervisor(fileOfMembers).invoke();
        }
    }

    /**
     * Returns the Path of our storage file. The file contains the names and public identities
     * of users that we've previous connected to.
     * @return
     */
    private Path takeFilePath() {
        return Paths.get(dataDir, FILE_NAME);
    }

    /**
     * Adds the public identity of the given user to our previous users file.
     * @param member to add to our file
     * @return List of all known users
     */
    public List<WithMiUser> addMemberToFile(WithMiUser member) throws CommunicationsFailure {
        String serializedIdentity = WithMiMemberSerializer.serialize(member);
        try {
            Files.write(takeFilePath(), (serializedIdentity + "\n").getBytes(), StandardOpenOption.APPEND);
            Map<String, WithMiUser> namesToMembers = readInPreviousConnections();
            return new ArrayList<>(namesToMembers.values());
        } catch (IOException e) {
            throw new CommunicationsFailure(e);
        }
    }

    /**
     * Reads the previous users file and stores the information in a map.
     * @return map mapping user names to users
     * @throws IOException
     */
    public Map<String, WithMiUser> readInPreviousConnections() throws CommunicationsFailure {
        try {
            List<String> linesOfFile = Files.readAllLines(takeFilePath(), StandardCharsets.UTF_8);
            Map<String, WithMiUser> memberMap = new HashMap<>();
            for (int b = 0; b < linesOfFile.size(); b++) {
                readInPreviousConnectionsAdviser(linesOfFile, memberMap, b);
            }
            return memberMap;
        } catch (IOException e) {
            throw new CommunicationsFailure(e);
        }
    }

    private void readInPreviousConnectionsAdviser(List<String> linesOfFile, Map<String, WithMiUser> memberMap, int j) {
        String line = linesOfFile.get(j);
        WithMiUser member = WithMiMemberSerializer.deserialize(line);
        memberMap.put(member.obtainName(), member);
    }

    private class WithMiConnectionsServiceSupervisor {
        private File fileOfMembers;

        public WithMiConnectionsServiceSupervisor(File fileOfMembers) {
            this.fileOfMembers = fileOfMembers;
        }

        public void invoke() throws IOException {
            fileOfMembers.getParentFile().mkdir();
            fileOfMembers.createNewFile();
        }
    }
}
