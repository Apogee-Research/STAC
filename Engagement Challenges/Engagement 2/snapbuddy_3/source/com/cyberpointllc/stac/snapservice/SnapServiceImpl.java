package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.model.Filter;
import com.cyberpointllc.stac.snapservice.model.Invitation;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SnapServiceImpl implements SnapService {

    private static final int LOCATION_CHANGE_LIMIT = 10;

    private final StorageService storageService;

    private final Map<String, Integer> locationChangeCounts;

    private long lastDay;

    public SnapServiceImpl(StorageService storageService) {
        if (storageService == null) {
            throw new  IllegalArgumentException("StorageService may not be null");
        }
        this.storageService = storageService;
        locationChangeCounts = new  HashMap();
        lastDay = TimeUnit.DAYS.convert(new  Date().getTime(), TimeUnit.MILLISECONDS);
    }

    @Override
    public String createPersonIdentity() {
        return storageService.createPersonIdentity();
    }

    @Override
    public Set<String> getPeople() {
        return storageService.getPeople();
    }

    @Override
    public Person getPerson(String identity) {
        ClassgetPerson replacementClass = new  ClassgetPerson(identity);
        ;
        return replacementClass.doIt0();
    }

    @Override
    public Set<Person> getNeighbors(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        Set<Person> neighbors = new  HashSet();
        if (!Location.UNKNOWN.equals(person.getLocation())) {
            for (String identity : getPeople()) {
                Person neighbor = getPerson(identity);
                if (!person.equals(neighbor) && person.getLocation().equals(neighbor.getLocation())) {
                    neighbors.add(neighbor);
                }
            }
        }
        return neighbors;
    }

    @Override
    public boolean setName(Person person, String name) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (StringUtils.isBlank(name)) {
            return false;
        }
        Person newPerson = new  Person(person.getIdentity(), name, person.getLocation(), person.getFriends(), person.getPhotos());
        return storageService.updatePerson(newPerson);
    }

    @Override
    public boolean canUpdateLocation(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        String key = person.getIdentity();
        // First, determine if day crossing causes a reset
        long today = TimeUnit.DAYS.convert(new  Date().getTime(), TimeUnit.MILLISECONDS);
        if (today > lastDay) {
            lastDay = today;
            locationChangeCounts.clear();
        }
        // Ensure Person has an entry
        if (!locationChangeCounts.containsKey(key)) {
            locationChangeCounts.put(key, 0);
        }
        // Permit location changes if count limit is not reached
        return (locationChangeCounts.get(key) < LOCATION_CHANGE_LIMIT);
    }

    @Override
    public boolean setLocation(Person person, Location location) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (location == null) {
            location = Location.UNKNOWN;
        }
        if (person.getLocation().equals(location)) {
            return false;
        }
        if (!canUpdateLocation(person)) {
            throw new  IllegalArgumentException("Number of location requests per day has been exceeded");
        }
        // Increase the location change count; safe due to canUpdateLocation call
        locationChangeCounts.put(person.getIdentity(), (1 + locationChangeCounts.get(person.getIdentity())));
        Person newPerson = new  Person(person.getIdentity(), person.getName(), location, person.getFriends(), person.getPhotos());
        return storageService.updatePerson(newPerson);
    }

    @Override
    public boolean addFriends(Person person, Set<Person> friends) {
        ClassaddFriends replacementClass = new  ClassaddFriends(person, friends);
        ;
        return replacementClass.doIt0();
    }

    @Override
    public boolean removeFriends(Person person, Set<Person> friends) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (friends == null) {
            throw new  IllegalArgumentException("Set of friends may not be null");
        }
        if (friends.isEmpty()) {
            return false;
        }
        // Removing friends is bi-directional so first gather
        // up all of the friend identities to be removed.
        Set<String> identities = new  HashSet();
        for (Person friend : friends) {
            identities.add(friend.getIdentity());
        }
        // Modify this person by removing these identities as friends
        boolean modified = modifyFriends(person, identities, false);
        // For the other direction, reset the set of identities
        // to now only hold this Person's identity
        identities.clear();
        identities.add(person.getIdentity());
        // this Person from their collection of friends.
        for (Person friend : friends) {
            modified |= modifyFriends(friend, identities, false);
        }
        return modified;
    }

    @Override
    public boolean addPhoto(Person person, Photo photo) {
        return modifyPhoto(person, photo, true);
    }

    @Override
    public boolean removePhoto(Person person, Photo photo) {
        return modifyPhoto(person, photo, false);
    }

    @Override
    public Photo getPhoto(String identity) {
        return storageService.getPhoto(identity);
    }

    @Override
    public boolean setVisibility(Photo photo, boolean isPublic) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        boolean modified = isPublic != photo.isPublicPhoto();
        if (modified) {
            Photo newPhoto = new  Photo(photo.getPath(), isPublic, photo.getCaption(), photo.getLocation(), photo.getFilters());
            modified = storageService.updatePhoto(newPhoto);
        }
        return modified;
    }

    @Override
    public boolean setCaption(Photo photo, String caption) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        boolean modified = !(StringUtils.isBlank(caption) ? StringUtils.isBlank(photo.getCaption()) : caption.equals(photo.getCaption()));
        if (modified) {
            Photo newPhoto = new  Photo(photo.getPath(), photo.isPublicPhoto(), caption, photo.getLocation(), photo.getFilters());
            modified = storageService.updatePhoto(newPhoto);
        }
        return modified;
    }

    @Override
    public boolean setLocation(Photo photo, Location location) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        if (location == null) {
            location = Location.UNKNOWN;
        }
        boolean modified = !location.equals(photo.getLocation());
        if (modified) {
            Photo newPhoto = new  Photo(photo.getPath(), photo.isPublicPhoto(), photo.getCaption(), location, photo.getFilters());
            modified = storageService.updatePhoto(newPhoto);
        }
        return modified;
    }

    @Override
    public boolean addFilter(Photo photo, Filter filter) {
        return modifyFilter(photo, filter, true);
    }

    @Override
    public boolean removeFilter(Photo photo, Filter filter) {
        return modifyFilter(photo, filter, false);
    }

    @Override
    public boolean isPhotoVisible(Person person, Photo photo) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        if (photo.isPublicPhoto()) {
            return true;
        } else if (person.ownsPhoto(photo)) {
            return true;
        }
        // Look at all of this Person's friends
        for (String friendId : person.getFriends()) {
            Person friend = getPerson(friendId);
            if ((friend != null) && friend.ownsPhoto(photo)) {
                return true;
            }
        }
        return false;
    }

    private boolean modifyFriends(Person person, Set<String> identities, boolean isAdding) {
        boolean modified;
        Set<String> friends = new  HashSet(person.getFriends());
        if (isAdding) {
            modified = friends.addAll(identities);
        } else {
            modified = friends.removeAll(identities);
        }
        if (modified) {
            Person newPerson = new  Person(person.getIdentity(), person.getName(), person.getLocation(), friends, person.getPhotos());
            modified = storageService.updatePerson(newPerson);
        }
        return modified;
    }

    private boolean modifyPhoto(Person person, Photo photo, boolean isAdding) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        boolean modified;
        if (isAdding) {
            modified = storageService.addPhoto(photo);
        } else {
            modified = storageService.deletePhoto(photo.getIdentity());
        }
        if (modified) {
            Set<String> photos = new  HashSet(person.getPhotos());
            if (isAdding) {
                modified = photos.add(photo.getIdentity());
            } else {
                modified = photos.remove(photo.getIdentity());
            }
            if (modified) {
                Person newPerson = new  Person(person.getIdentity(), person.getName(), person.getLocation(), person.getFriends(), photos);
                modified = storageService.updatePerson(newPerson);
            }
        }
        return modified;
    }

    private boolean modifyFilter(Photo photo, Filter filter, boolean isAdding) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        boolean modified = false;
        if (filter != null) {
            List<Filter> filters = new  ArrayList(photo.getFilters());
            if (isAdding) {
                modified = filters.add(filter);
            } else {
                modified = filters.remove(filter);
            }
            if (modified) {
                Photo newPhoto = new  Photo(photo.getPath(), photo.isPublicPhoto(), photo.getCaption(), photo.getLocation(), filters);
                modified = storageService.updatePhoto(newPhoto);
            }
        }
        return modified;
    }

    @Override
    public Set<Person> getInvitations(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        Set<Person> people = new  HashSet();
        for (Invitation invitation : storageService.getInvitations()) {
            if (person.getIdentity().equals(invitation.getInviteFromIdentity())) {
                people.add(getPerson(invitation.getInviteToIdentity()));
            } else if (person.getIdentity().equals(invitation.getInviteToIdentity())) {
                people.add(getPerson(invitation.getInviteFromIdentity()));
            }
        }
        return people;
    }

    @Override
    public Set<Person> getInvitationsTo(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        Set<Person> people = new  HashSet();
        for (Invitation invitation : storageService.getInvitations()) {
            if (person.getIdentity().equals(invitation.getInviteToIdentity())) {
                people.add(getPerson(invitation.getInviteFromIdentity()));
            }
        }
        return people;
    }

    @Override
    public boolean sendInvitation(Person sender, Person receiver) {
        if (sender == null) {
            throw new  IllegalArgumentException("Sending Person may not be null");
        }
        if (receiver == null) {
            throw new  IllegalArgumentException("Receiving Person may not be null");
        }
        boolean added = false;
        // First, check that a reverse invitation doesn't already exist
        Invitation reverseInvitation = new  Invitation(receiver.getIdentity(), sender.getIdentity());
        if (!storageService.getInvitations().contains(reverseInvitation)) {
            // Reverse invitation does not already exist; add a new one
            added = storageService.addInvitation(new  Invitation(sender.getIdentity(), receiver.getIdentity()));
        }
        return added;
    }

    @Override
    public boolean acceptInvitation(Person sender, Person receiver) {
        if (sender == null) {
            throw new  IllegalArgumentException("Sending Person may not be null");
        }
        if (receiver == null) {
            throw new  IllegalArgumentException("Receiving Person may not be null");
        }
        Set<Invitation> invitations = storageService.getInvitations();
        Invitation matchingInvitation = new  Invitation(sender.getIdentity(), receiver.getIdentity());
        if (!invitations.contains(matchingInvitation)) {
            // No match; try the reverse invitation
            matchingInvitation = new  Invitation(receiver.getIdentity(), sender.getIdentity());
            if (!invitations.contains(matchingInvitation)) {
                // No match either; no invitation to accept
                matchingInvitation = null;
            }
        }
        boolean response = false;
        if (matchingInvitation != null) {
            storageService.deleteInvitation(matchingInvitation);
            response = addFriends(sender, Collections.singleton(receiver));
        }
        return response;
    }

    @Override
    public boolean rejectInvitation(Person sender, Person receiver) {
        if (sender == null) {
            throw new  IllegalArgumentException("Sending Person may not be null");
        }
        if (receiver == null) {
            throw new  IllegalArgumentException("Receiving Person may not be null");
        }
        Set<Invitation> invitations = storageService.getInvitations();
        Invitation matchingInvitation = new  Invitation(sender.getIdentity(), receiver.getIdentity());
        if (!invitations.contains(matchingInvitation)) {
            // No match; try the reverse invitation
            matchingInvitation = new  Invitation(receiver.getIdentity(), sender.getIdentity());
            if (!invitations.contains(matchingInvitation)) {
                // No match either; no invitation to accept
                matchingInvitation = null;
            }
        }
        boolean response = false;
        if (matchingInvitation != null) {
            response = storageService.deleteInvitation(matchingInvitation);
        }
        return response;
    }

    public class ClassgetPerson {

        public ClassgetPerson(String identity) {
            this.identity = identity;
        }

        private String identity;

        public Person doIt0() {
            return storageService.getPerson(identity);
        }
    }

    public class ClassaddFriends {

        public ClassaddFriends(Person person, Set<Person> friends) {
            this.person = person;
            this.friends = friends;
        }

        private Person person;

        private Set<Person> friends;

        private Set<String> identities;

        private boolean modified;

        public boolean doIt0() {
            if (person == null) {
                throw new  IllegalArgumentException("Person may not be null");
            }
            if (friends == null) {
                throw new  IllegalArgumentException("Set of friends may not be null");
            }
            if (friends.isEmpty()) {
                return false;
            }
            identities = new  HashSet();
            for (Person friend : friends) {
                identities.add(friend.getIdentity());
            }
            // Make sure this Person doesn't add their own identity as a friend
            identities.remove(person.getIdentity());
            modified = modifyFriends(person, identities, true);
            // to now only hold this Person's identity
            identities.clear();
            identities.add(person.getIdentity());
            // this Person to their collection of friends.
            for (Person friend : friends) {
                modified |= modifyFriends(friend, identities, true);
            }
            return modified;
        }
    }
}
