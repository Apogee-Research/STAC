package com.cyberpointllc.stac.snapservice.model;

import org.apache.commons.lang3.StringUtils;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class Person {

    public static final PersonAscendingComparator ASCENDING_COMPARATOR = new  PersonAscendingComparator();

    private final String identity;

    private final String name;

    private final Location location;

    private final Set<String> friends;

    private final Set<String> photos;

    public Person(String identity, String name, Location location, Set<String> friends, Set<String> photos) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Person identity may not be null or empty");
        }
        if (StringUtils.isBlank(name)) {
            throw new  IllegalArgumentException("Person name may not be null or empty");
        }
        this.identity = identity;
        this.name = name;
        this.location = (location != null) ? location : Location.UNKNOWN;
        this.friends = new  LinkedHashSet();
        if (friends != null) {
            this.friends.addAll(friends);
        }
        this.photos = new  LinkedHashSet();
        if (photos != null) {
            this.photos.addAll(photos);
        }
    }

    /**
     * Returns the identity for this Person.
     *
     * @return String representing the identity;
     * guaranteed to not be <code>null</code>
     */
    public String getIdentity() {
        return identity;
    }

    /**
     * Returns the full name to be displayed for this Person.
     *
     * @return String representing the full name of this Person;
     * guaranteed to not be <code>null</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the current location of this Person.
     *
     * @return Location currently associated with this Person
     * guaranteed to not be <code>null</code>
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Returns an unmodifiable Set of friends of this Person.
     * The set contains the identity of the friends.
     * There is no guarantee as to the order of the friends in the Set.
     *
     * @return Set of identity instances who are friends of this person;
     * may be empty but guaranteed to not be <code>null</code>
     */
    public Set<String> getFriends() {
        return Collections.unmodifiableSet(friends);
    }

    /**
     * Returns an unmodifiable Set of photos owned by the Person.
     * The set contains the identity of the photos.
     * There is no guarantee as to the order of the photos in the Set.
     *
     * @return Set of identity instances of photos owned by this person;
     * may be empty but guaranteed to not be <code>null</code>
     */
    public Set<String> getPhotos() {
        return Collections.unmodifiableSet(photos);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Person person = (Person) obj;
        return identity.equals(person.identity);
    }

    @Override
    public int hashCode() {
        return identity.hashCode();
    }

    /**
     * @return boolean true if this photo belongs to this Person
     */
    public boolean ownsPhoto(Photo photo) {
        return photos.contains(photo.getIdentity());
    }

    public static class PersonAscendingComparator implements Comparator<Person> {

        @Override
        public int compare(Person person1, Person person2) {
            String person1Name = person1.getName();
            String person2Name = person2.getName();
            return person1Name.compareTo(person2Name);
        }
    }
}
