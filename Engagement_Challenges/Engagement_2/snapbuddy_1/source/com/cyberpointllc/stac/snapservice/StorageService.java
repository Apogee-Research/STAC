package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.snapservice.model.Invitation;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import java.util.Set;

public interface StorageService {

    /**
     * Returns the set of identities of all users in the system.
     *
     * @return Set of identities;
     * may be empty but guaranteed to not be <code>null</code>
     */
    Set<String> getUsers();

    /**
     * Returns the User associated with the specified identity.
     *
     * @param identity used to look up the User
     * @return User matching the identity;
     * will be <code>null</code> if no match exists
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    User getUser(String identity);

    /**
     * Adds the specified User to the collection of users.
     *
     * @param user to be added to the collection
     * @return boolean true if the user was added
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean addUser(User user);

    /**
     * Updates the specified User.
     *
     * @param user to be updated
     * @return boolean true if the user was updated
     * @throws IllegalArgumentException if argument is <code>null</code>
     * @see #addUser(User)
     */
    boolean updateUser(User user);

    /**
     * Removes the User associated with the specified identity
     * from the collection of users.
     *
     * @param identity of the User to be removed
     * @return boolean true if the User was removed;
     * false if the User does not exist in the system
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    boolean deleteUser(String identity);

    /**
     * Returns a unique identity to be used for a new Person.
     *
     * @return String representing a unique identity;
     * guaranteed to not be <code>null</code> or empty
     */
    String createPersonIdentity();

    /**
     * Returns the set of identities of all people in the system.
     *
     * @return Set of identities;
     * may be empty but guaranteed to not be <code>null</code>
     */
    Set<String> getPeople();

    /**
     * Returns the Person associated with the specified identity.
     *
     * @param identity used to look up the Person
     * @return Person matching the identity;
     * will be <code>null</code> if no match exists
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    Person getPerson(String identity);

    /**
     * Adds the specified Person to the collection of people.
     *
     * @param person to be added to the collection
     * @return boolean true if the person was added
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean addPerson(Person person);

    /**
     * Updates the specified Person.
     *
     * @param person to be updated
     * @return boolean true if the person was updated
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean updatePerson(Person person);

    /**
     * Removes the Person associated with the specified identity
     * from the collection of people.
     *
     * @param identity of the Person to be removed
     * @return boolean true if the Person was removed;
     * false if the Person does not exist in the system
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    boolean deletePerson(String identity);

    /**
     * Returns the Photo associated with the specified identity.
     *
     * @param identity used to look up the Photo
     * @return Photo matching the identity;
     * will be <code>null</code> if no match exists
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    Photo getPhoto(String identity);

    /**
     * Adds the specified Photo to the collection of photos.
     *
     * @param photo to be added to the collection
     * @return boolean true if the photo was added
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean addPhoto(Photo photo);

    /**
     * Updates the specified Photo.
     *
     * @param photo to be updated
     * @return boolean true if the photo was updated
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean updatePhoto(Photo photo);

    /**
     * Removes the Photo associated with the specified identity
     * from the collection of photos.
     *
     * @param identity of the Photo to be removed
     * @return boolean true if the Photo was removed;
     * false if the Photo does not exist in the system
     * @throws IllegalArgumentException if argument is <code>null</code> or empty
     */
    boolean deletePhoto(String identity);

    /**
     * Returns the set of all invitations in the system.
     *
     * @return Set of invitations;
     * may be empty but guaranteed to not be <code>null</code>
     */
    Set<Invitation> getInvitations();

    /**
     * Adds the specified Invitation to the collection of invitations.
     *
     * @param invitation to be added to the collection
     * @return boolean true if the invitation was added
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean addInvitation(Invitation invitation);

    /**
     * Removes the specified Invitation.
     *
     * @param invitation to be removed from the collection
     * @return boolean true if the invitation was removed
     * @throws IllegalArgumentException if argument is <code>null</code>
     */
    boolean deleteInvitation(Invitation invitation);
}
