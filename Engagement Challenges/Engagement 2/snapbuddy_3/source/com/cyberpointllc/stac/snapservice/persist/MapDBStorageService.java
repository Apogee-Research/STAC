package com.cyberpointllc.stac.snapservice.persist;

import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.StorageService;
import com.cyberpointllc.stac.snapservice.model.Invitation;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import org.apache.commons.lang3.StringUtils;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class MapDBStorageService implements StorageService {

    private static final String USER_MAP = "users";

    private static final String PERSON_MAP = "people";

    private static final String PHOTO_MAP = "photos";

    private static final String INVITE_SET = "invites";

    private static final int PERSON_IDENTITY_SIZE = 5;

    private static final Random RANDOM = new  Random();

    private final DB db;

    private final UserSerializer userSerializer;

    private final PersonSerializer personSerializer;

    private final PhotoSerializer photoSerializer;

    private final InvitationSerializer invitationSerializer;

    public MapDBStorageService(File file, LocationService locationService) {
        if (file == null) {
            throw new  IllegalArgumentException("File may not be null");
        }
        if (locationService == null) {
            throw new  IllegalArgumentException("LocationService may not be null");
        }
        db = DBMaker.fileDB(file).fileMmapEnableIfSupported().transactionDisable().asyncWriteEnable().make();
        userSerializer = new  UserSerializer();
        personSerializer = new  PersonSerializer(locationService);
        photoSerializer = new  PhotoSerializer(locationService);
        invitationSerializer = new  InvitationSerializer();
    }

    public void close() {
        db.commit();
        db.close();
    }

    protected Map<String, User> getUserMap() {
        ClassgetUserMap replacementClass = new  ClassgetUserMap();
        ;
        return replacementClass.doIt0();
    }

    protected Map<String, Person> getPersonMap() {
        return db.hashMap(PERSON_MAP, Serializer.STRING, personSerializer);
    }

    protected Map<String, Photo> getPhotoMap() {
        ClassgetPhotoMap replacementClass = new  ClassgetPhotoMap();
        ;
        return replacementClass.doIt0();
    }

    protected Set<Invitation> getInvitationSet() {
        return db.hashSet(INVITE_SET, invitationSerializer);
    }

    private static String createIdentity(int size) {
        StringBuilder sb = new  StringBuilder();
        while (sb.length() < size) {
            String value = Integer.toHexString(RANDOM.nextInt()).toUpperCase();
            if (value.length() > size) {
                sb.append(value.substring(RANDOM.nextInt(value.length() - size)));
            } else {
                sb.append(value);
            }
        }
        return sb.substring(0, size);
    }

    protected String createIdentity(Set<String> existingIdentities, int identitySize) {
        String identity = createIdentity(identitySize);
        while (existingIdentities.contains(identity)) {
            identity = createIdentity(identitySize);
        }
        return identity;
    }

    @Override
    public Set<String> getUsers() {
        return getUserMap().keySet();
    }

    @Override
    public User getUser(String identity) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("User identity may not be empty or null");
        }
        return getUserMap().get(identity.toUpperCase());
    }

    @Override
    public boolean addUser(User user) {
        if (user == null) {
            throw new  IllegalArgumentException("User may not be null");
        }
        getUserMap().put(user.getIdentity().toUpperCase(), user);
        return true;
    }

    @Override
    public boolean updateUser(User user) {
        ClassupdateUser replacementClass = new  ClassupdateUser(user);
        ;
        return replacementClass.doIt0();
    }

    @Override
    public boolean deleteUser(String identity) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("User identity may not be empty or null");
        }
        User previous = getUserMap().remove(identity.toUpperCase());
        return (previous != null);
    }

    @Override
    public String createPersonIdentity() {
        return createIdentity(getPersonMap().keySet(), PERSON_IDENTITY_SIZE);
    }

    @Override
    public Set<String> getPeople() {
        return getPersonMap().keySet();
    }

    @Override
    public Person getPerson(String identity) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Person identity may not be empty or null");
        }
        return getPersonMap().get(identity.toUpperCase());
    }

    @Override
    public boolean addPerson(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        getPersonMap().put(person.getIdentity().toUpperCase(), person);
        return true;
    }

    @Override
    public boolean updatePerson(Person person) {
        if (person == null) {
            throw new  IllegalArgumentException("Person may not be null");
        }
        getPersonMap().put(person.getIdentity().toUpperCase(), person);
        return true;
    }

    @Override
    public boolean deletePerson(String identity) {
        ClassdeletePerson replacementClass = new  ClassdeletePerson(identity);
        ;
        replacementClass.doIt0();
        return replacementClass.doIt1();
    }

    @Override
    public Photo getPhoto(String identity) {
        ClassgetPhoto replacementClass = new  ClassgetPhoto(identity);
        ;
        return replacementClass.doIt0();
    }

    @Override
    public boolean addPhoto(Photo photo) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        getPhotoMap().put(photo.getIdentity().toUpperCase(), photo);
        return true;
    }

    @Override
    public boolean updatePhoto(Photo photo) {
        if (photo == null) {
            throw new  IllegalArgumentException("Photo may not be null");
        }
        getPhotoMap().put(photo.getIdentity().toUpperCase(), photo);
        return true;
    }

    @Override
    public boolean deletePhoto(String identity) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Photo identity may not be empty or null");
        }
        Photo previous = getPhotoMap().remove(identity.toUpperCase());
        return (previous != null);
    }

    @Override
    public Set<Invitation> getInvitations() {
        ClassgetInvitations replacementClass = new  ClassgetInvitations();
        ;
        return replacementClass.doIt0();
    }

    @Override
    public boolean addInvitation(Invitation invitation) {
        ClassaddInvitation replacementClass = new  ClassaddInvitation(invitation);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        return replacementClass.doIt2();
    }

    @Override
    public boolean deleteInvitation(Invitation invitation) {
        if (invitation == null) {
            throw new  IllegalArgumentException("Invitation may not be null");
        }
        boolean result = getInvitationSet().remove(invitation);
        return result;
    }

    protected class ClassgetUserMap {

        public ClassgetUserMap() {
        }

        public Map<String, User> doIt0() {
            return db.hashMap(USER_MAP, Serializer.STRING, userSerializer);
        }
    }

    protected class ClassgetPhotoMap {

        public ClassgetPhotoMap() {
        }

        public Map<String, Photo> doIt0() {
            return db.hashMap(PHOTO_MAP, Serializer.STRING, photoSerializer);
        }
    }

    public class ClassupdateUser {

        public ClassupdateUser(User user) {
            this.user = user;
        }

        private User user;

        public boolean doIt0() {
            if (user == null) {
                throw new  IllegalArgumentException("User may not be null");
            }
            getUserMap().put(user.getIdentity().toUpperCase(), user);
            return true;
        }
    }

    public class ClassdeletePerson {

        public ClassdeletePerson(String identity) {
            this.identity = identity;
        }

        private String identity;

        private Person previous;

        public void doIt0() {
            if (StringUtils.isBlank(identity)) {
                throw new  IllegalArgumentException("Person identity may not be empty or null");
            }
            previous = getPersonMap().remove(identity.toUpperCase());
        }

        public boolean doIt1() {
            return (previous != null);
        }
    }

    public class ClassgetPhoto {

        public ClassgetPhoto(String identity) {
            this.identity = identity;
        }

        private String identity;

        public Photo doIt0() {
            if (StringUtils.isBlank(identity)) {
                throw new  IllegalArgumentException("Photo identity may not be empty or null");
            }
            return getPhotoMap().get(identity.toUpperCase());
        }
    }

    public class ClassgetInvitations {

        public ClassgetInvitations() {
        }

        public Set<Invitation> doIt0() {
            return Collections.unmodifiableSet(getInvitationSet());
        }
    }

    public class ClassaddInvitation {

        public ClassaddInvitation(Invitation invitation) {
            this.invitation = invitation;
        }

        private Invitation invitation;

        public void doIt0() {
            if (invitation == null) {
                throw new  IllegalArgumentException("Invitation may not be null");
            }
        }

        private boolean result;

        public void doIt1() {
            result = getInvitationSet().add(invitation);
        }

        public boolean doIt2() {
            return result;
        }
    }
}
