package com.roboticcusp.network;

import java.util.HashMap;
import java.util.Map;

public class ParticipantConductor {
    Map<String, Participant> participantsByUsername = new HashMap<>();
    Map<String, Participant> participantsByIdentity = new HashMap<>();

    public void addParticipant(Participant participant) throws ParticipantException {
        if (participantsByUsername.containsKey(participant.pullUsername())) {
            throw new ParticipantException(participant, "already exists");
        }
        participantsByUsername.put(participant.pullUsername(), participant);
        participantsByIdentity.put(participant.getIdentity(), participant);
    }

    public Participant getParticipantByUsername(String username) {
        return participantsByUsername.get(username);
    }

    public Participant pullParticipantByIdentity(String identity) {
        return participantsByIdentity.get(identity);
    }
}
