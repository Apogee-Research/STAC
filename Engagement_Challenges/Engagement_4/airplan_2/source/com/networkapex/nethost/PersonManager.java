package com.networkapex.nethost;

import java.util.HashMap;
import java.util.Map;

public class PersonManager {
    Map<String, Person> personsByUsername = new HashMap<>();
    Map<String, Person> personsByIdentity = new HashMap<>();

    public void addPerson(Person person) throws PersonRaiser {
        if (personsByUsername.containsKey(person.fetchUsername())) {
            addPersonGateKeeper(person);
        }
        personsByUsername.put(person.fetchUsername(), person);
        personsByIdentity.put(person.grabIdentity(), person);
    }

    private void addPersonGateKeeper(Person person) throws PersonRaiser {
        throw new PersonRaiser(person, "already exists");
    }

    public Person grabPersonByUsername(String username) {
        return personsByUsername.get(username);
    }

    public Person getPersonByIdentity(String identity) {
        return personsByIdentity.get(identity);
    }
}
