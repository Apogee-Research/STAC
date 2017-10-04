package com.cyberpointllc.stac.snapservice;

import com.cyberpointllc.stac.snapservice.model.Filter;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import java.util.Set;

public interface SnapService {

    /**
     * Returns a unique identity to be used for a new Person.
     *
     * @return String representing a unique identity;
     * guaranteed to not be <code>null</code> or empty
     */
    String createPersonIdentity();

    /**
     * Returns the set of all known Person identities.
     *
     * @return Set of all known Person identities;
     * guaranteed to not be <code>null</code>
     */
    Set<String> getPeople();

    /**
     * Returns the Person associated with the specified user identity.
     * An empty or <code>null</code> identity does not match anything.
     *
     * @param identity of user used to look up the Person
     * @return Person matching the user identity;
     * may be <code>null</code> if no match exists
     */
    Person getPerson(String identity);

    /**
     * Returns the Set of Person entries that are currently
     * in the same location as the specified Person.
     * If the location of the specified Person is unknown,
     * an empty set will be returned.
     * The set will not include the specified Person
     * nor is it confined to just the Person's friends but includes
     * any Person in the same location.
     *
     * @param person whose neighbors are being sought
     * @return Set of Person instances that are located at
     * the same location as the specified Person;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the Person is <code>null</code>
     */
    Set<Person> getNeighbors(Person person);

    /**
     * Updates the Person's displayed name to the specified value.
     * If the argument is empty or <code>null</code>,
     * the request fails and <code>false</code> is returned.
     *
     * @param person to have name updated
     * @param name   to be assigned to the Person
     * @return boolean true if the name was updated
     * @throws IllegalArgumentException if the Person is <code>null</code>
     */
    boolean setName(Person person, String name);

    /**
     * Indicates if the specified Person is permitted to request to
     * update their current location.
     * There may be constraints that limit the number of times a
     * person may update their position in a given time frame.
     * If an update request would be rejected in this case,
     * this method will return <code>false</code>.
     * If the specified person is permitted, <code>true</code>
     * will be returned.
     * Permission to request an update does not imply the request
     * will succeed; only that it is permitted to be made.
     *
     * @param person requesting to change their current location
     * @return boolean true if the location may be updated
     * @throws IllegalArgumentException if the Person is <code>null</code>
     * @see #setLocation(Person, Location)
     */
    boolean canUpdateLocation(Person person);

    /**
     * Updates the Person's current location to the specified setting.
     * If the argument is <code>null</code>,
     * an unknown location is assigned.
     * There can be constraints that limit if the location can be
     * updated so the return value should be checked to determine
     * if the location was updated.
     * If the specified location is the same as the current setting,
     * <code>false</code> is returned.
     *
     * @param person   to update
     * @param location to be assigned to the Person
     * @return boolean true if the location was updated
     * @throws IllegalArgumentException if the Person is <code>null</code>
     * @see #canUpdateLocation(Person)
     */
    boolean setLocation(Person person, Location location);

    /**
     * Adds the friendship between the specified Person
     * and a set of other Persons.
     * Adding a friend is bi-directional: adding a friend to
     * person's set of friends also adds person to the friend's
     * set of friends.
     *
     * @param person  to add friends
     * @param friends to be added as friends
     * @return boolean true if any of the friendships were created
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean addFriends(Person person, Set<Person> friends);

    /**
     * Removes the friendship between the specified Person
     * and the set of friends.
     * Removing a friend is bi-directional: removing a friend from
     * person's set of friends also removes person from the friend's
     * set of friends.
     *
     * @param person  to remove friends
     * @param friends to be removed as friends
     * @return boolean true if any of the friendships were dissolved
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean removeFriends(Person person, Set<Person> friends);

    /**
     * Adds the specified Photo to the Person's collection
     * of photos.
     *
     * @param person who owns photos
     * @param photo  to be added to the collection
     * @return boolean true if the photo was added
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean addPhoto(Person person, Photo photo);

    /**
     * Removes the specified photo from the Person's collection
     * of photos.
     *
     * @param person who owns photos
     * @param photo  to be removed from the collection
     * @return boolean true if the photo was removed
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean removePhoto(Person person, Photo photo);

    /**
     * Returns the Photo associated with the specified identity.
     *
     * @param identity used to look up the Photo
     * @return Photo matching the identity;
     * may be <code>null</code> if no match exists
     */
    Photo getPhoto(String identity);

    /**
     * Updates the visibility of the specified Photo.
     * If the photo is marked publicly visible (<code>true</code>),
     * then any Person may see the Photo.
     * Otherwise, only the owner and the owner's friends
     * may see the Photo.
     *
     * @param photo    to be updated
     * @param isPublic true if the Photo should be publicly visible;
     *                 false if only friends and the owner may see it
     * @return boolean true if the Photo was updated
     * @throws IllegalArgumentException if the Photo is <code>null</code>
     */
    boolean setVisibility(Photo photo, boolean isPublic);

    /**
     * Updates the caption of the specified Photo.
     * If the caption is empty (or <code>null</code>),
     * the caption is removed from the Photo.
     *
     * @param photo   to be updated
     * @param caption to be assigned to the Photo
     * @return boolean true if the Photo was updated
     * @throws IllegalArgumentException if the Photo is <code>null</code>
     */
    boolean setCaption(Photo photo, String caption);

    /**
     * Updates the Photo's current location to the specified setting.
     * If the argument is <code>null</code>,
     * an unknown location is assigned.
     * There can be constraints that limit if the
     * location can be updated so the return
     * value should be checked to determine if
     * the location was updated.
     *
     * @param photo    to be updated
     * @param location to be assigned to the Photo
     * @return boolean true if the location was updated
     * @throws IllegalArgumentException if the Photo is <code>null</code>
     */
    boolean setLocation(Photo photo, Location location);

    /**
     * Adds the specified Filter to the specified Photo.
     * If the filter is <code>null</code> or is already
     * assigned to the Photo, this request is denied
     * and <code>false</code> is returned.
     * Otherwise, the filter is added to the end of the
     * filters applied to the photo.
     *
     * @param photo  to be filtered
     * @param filter to be assigned to the Photo
     * @return boolean true if the filter was applied to the Photo
     * @throws IllegalArgumentException if the Photo is <code>null</code>
     */
    boolean addFilter(Photo photo, Filter filter);

    /**
     * Removes the specified Filter to the specified Photo.
     * If the filter is <code>null</code> or is not already
     * assigned to the Photo, this request is denied
     * and <code>false</code> is returned.
     * Otherwise, the filter is removed from the
     * filters applied to the photo.
     *
     * @param photo  to be updated
     * @param filter to be removed
     * @return boolean true if the filter was removed from the Photo
     * @throws IllegalArgumentException if the Photo is <code>null</code>
     */
    boolean removeFilter(Photo photo, Filter filter);

    /**
     * Determine if the specified Person is permitted to
     * see the specified Photo.
     * The Person is permitted to see the Photo only if:
     * <ol>
     * <li>The Photo is publicly visible</li>
     * <li>The Person owns the Photo</li>
     * <li>The Photo belongs to one of the Person's friends</li>
     * </ol>
     *
     * @param person to who the photo is or isn't visible
     * @param photo  in question
     * @return boolean true if the photo is permitted to be seen
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean isPhotoVisible(Person person, Photo photo);

    /**
     * Returns the set of all Persons associated
     * with an invitation involving the specified Person.
     * The specified Person may be either the sender or
     * the receiver of the invitation.
     *
     * @param person associated with an invitation
     * @return Set of Persons sending or receiving an invitation;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the Person is <code>null</code>
     */
    Set<Person> getInvitations(Person person);

    /**
     * Returns the set of all Persons who have sent
     * an invitation to the specified Person.
     *
     * @param person who is receiving an invitation
     * @return Set of Persons who have sent an invitation;
     * may be empty but guaranteed to not be <code>null</code>
     * @throws IllegalArgumentException if the Person is <code>null</code>
     */
    Set<Person> getInvitationsTo(Person person);

    /**
     * Sends an invitation from the specified sending Person
     * to the specified receiving (invited) Person.
     *
     * @param sender   Person who is sending the invitation
     * @param receiver Person who will receive the invitation
     * @return boolean true if the invitation was sent
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean sendInvitation(Person sender, Person receiver);

    /**
     * Accepts the invitation from the specified sending Person
     * to the specified receiving (invited) Person.
     *
     * @param sender   Person who has sent the invitation
     * @param receiver Person who is accepting the invitation
     * @return boolean true if the invitation was accepted
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean acceptInvitation(Person sender, Person receiver);

    /**
     * Rejects the invitation from the specified sending Person
     * to the specified receiving (invited) Person.
     *
     * @param sender   Person who has sent the invitation
     * @param receiver Person who is rejecting the invitation
     * @return boolean true if the invitation was rejected
     * @throws IllegalArgumentException if either argument is <code>null</code>
     */
    boolean rejectInvitation(Person sender, Person receiver);
}
