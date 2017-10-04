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
        return db.hashMap(USER_MAP, Serializer.STRING, userSerializer);
    }

    protected Map<String, Person> getPersonMap() {
        return db.hashMap(PERSON_MAP, Serializer.STRING, personSerializer);
    }

    protected Map<String, Photo> getPhotoMap() {
        return db.hashMap(PHOTO_MAP, Serializer.STRING, photoSerializer);
    }

    protected Set<Invitation> getInvitationSet() {
        return db.hashSet(INVITE_SET, invitationSerializer);
    }

    private static String createIdentity(int size) {
        StringBuilder sb = new  StringBuilder();
        while (sb.length() < size) {
            String value = Integer.toHexString(RANDOM.nextInt()).toUpperCase();
            if (value.length() > size) {
                createIdentityHelper(sb, value, size);
            } else {
                createIdentityHelper1(sb, value);
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
        if (user == null) {
            throw new  IllegalArgumentException("User may not be null");
        }
        getUserMap().put(user.getIdentity().toUpperCase(), user);
        return true;
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
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Person identity may not be empty or null");
        }
        Person previous = getPersonMap().remove(identity.toUpperCase());
        return (previous != null);
    }

    @Override
    public Photo getPhoto(String identity) {
        if (StringUtils.isBlank(identity)) {
            throw new  IllegalArgumentException("Photo identity may not be empty or null");
        }
        return getPhotoMap().get(identity.toUpperCase());
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
        return Collections.unmodifiableSet(getInvitationSet());
    }

    @Override
    public boolean addInvitation(Invitation invitation) {
        if (invitation == null) {
            throw new  IllegalArgumentException("Invitation may not be null");
        }
        boolean result = getInvitationSet().add(invitation);
        return result;
    }

    @Override
    public boolean deleteInvitation(Invitation invitation) {
        if (invitation == null) {
            throw new  IllegalArgumentException("Invitation may not be null");
        }
        boolean result = getInvitationSet().remove(invitation);
        return result;
    }

    private static void createIdentityHelper(StringBuilder sb, String value, int size) {
        sb.append(value.substring(RANDOM.nextInt(value.length() - size)));
    }

    private static void createIdentityHelper1(StringBuilder sb, String value) {
        sb.append(value);
    }
}
