package com.cyberpointllc.stac.host;

import com.cyberpointllc.stac.common.DESHelper;
import com.cyberpointllc.stac.hashmap.HashMap;
import com.cyberpointllc.stac.snapservice.persist.MapDBStorageService;
import com.cyberpointllc.stac.webserver.User;
import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.model.Filter;
import com.cyberpointllc.stac.snapservice.model.FilterFactory;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import com.cyberpointllc.stac.snapservice.model.Photo;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Loads SnapBuddy maps from provided input streams.
 */
public class SnapBuddyLoader {

    private static final String PEOPLE_RESOURCE = "initialpersons.csv";

    private static final String PHOTOS_RESOURCE = "initialphotos.csv";

    private static final String USERS_RESOURCE = "initialusers.csv";

    public static void populate(MapDBStorageService storageService, LocationService locationService, String passwordKey) throws IOException {
        Map<String, Photo> photos;
        Map<String, Person> people;
        Map<String, User> users;
        try (InputStream inputStream = SnapBuddyLoader.class.getResourceAsStream(PHOTOS_RESOURCE)) {
            photos = SnapBuddyLoader.getPhotos(inputStream, locationService);
            System.out.format("Loaded %d photos%n", photos.size());
        }
        try (InputStream inputStream = SnapBuddyLoader.class.getResourceAsStream(PEOPLE_RESOURCE)) {
            people = SnapBuddyLoader.getPeople(inputStream, locationService);
            System.out.format("Loaded %d people%n", people.size());
        }
        try (InputStream inputStream = SnapBuddyLoader.class.getResourceAsStream(USERS_RESOURCE)) {
            users = SnapBuddyLoader.getUsers(inputStream, passwordKey);
            System.out.format("Loaded %d users%n", users.size());
        }
        for (Photo photo : photos.values()) {
            storageService.addPhoto(photo);
        }
        for (Person person : people.values()) {
            storageService.addPerson(person);
        }
        for (User user : users.values()) {
            storageService.addUser(user);
        }
    }

    private static Map<String, Photo> getPhotos(InputStream inputStream, LocationService locationService) {
        Map<String, Photo> photos = new  HashMap();
        try (BufferedReader reader = new  BufferedReader(new  InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                String path = parts[0];
                boolean isPublic = parts[1].isEmpty() ? true : Boolean.valueOf(parts[1]);
                Location location = parts[2].isEmpty() ? Location.UNKNOWN : locationService.getLocation(parts[2]);
                String caption = parts[3];
                List<Filter> filters = new  ArrayList();
                for (String filterId : parts[4].split(";")) {
                    Filter filter = FilterFactory.getFilter(filterId);
                    if (filter != null) {
                        filters.add(filter);
                    }
                }
                if (!path.isEmpty()) {
                    photos.put(path, new  Photo(path, isPublic, caption, location, filters));
                }
            }
        } catch (IOException e) {
            throw new  IllegalArgumentException("Trouble parsing Photo resources", e);
        }
        return photos;
    }

    private static Map<String, Person> getPeople(InputStream inputStream, LocationService locationService) {
        Map<String, Person> people = new  HashMap();
        try (BufferedReader reader = new  BufferedReader(new  InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                String identity = parts[0];
                String name = parts[1].isEmpty() ? "Unknown Name" : parts[1];
                Location location = parts[2].isEmpty() ? Location.UNKNOWN : locationService.getLocation(parts[2]);
                Set<String> photoIds = new  HashSet();
                for (String photoIdentity : parts[3].split(";")) {
                    if (!photoIdentity.isEmpty()) {
                        photoIds.add(photoIdentity);
                    }
                }
                Set<String> friendIds = new  HashSet();
                for (String friendIdentity : parts[4].split(";")) {
                    if (!friendIdentity.isEmpty()) {
                        friendIds.add(friendIdentity);
                    }
                }
                if (!identity.isEmpty()) {
                    people.put(identity, new  Person(identity, name, location, friendIds, photoIds));
                }
            }
        } catch (IOException e) {
            throw new  IllegalArgumentException("Trouble parsing Person resources", e);
        }
        return people;
    }

    private static Map<String, User> getUsers(InputStream inputStream, String passwordKey) {
        Map<String, User> users = new  HashMap();
        try (BufferedReader reader = new  BufferedReader(new  InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                String identity = parts[0];
                String username = parts[1];
                String password = parts[2];
                if (password.length() < User.MIN_PASSWORD_LENGTH) {
                    throw new  IllegalArgumentException("Password may not be less than " + User.MIN_PASSWORD_LENGTH + " characters");
                }
                if (password.length() > User.MAX_PASSWORD_LENGTH) {
                    throw new  IllegalArgumentException("Password may not be more than " + User.MAX_PASSWORD_LENGTH + " characters");
                }
                String encryptedPw = DESHelper.getEncryptedString(password, passwordKey);
                if (!identity.isEmpty() && !username.isEmpty() && !encryptedPw.isEmpty()) {
                    users.put(identity, new  User(identity, username, encryptedPw));
                }
            }
        } catch (IOException e) {
            throw new  IllegalArgumentException("Trouble parsing User resources", e);
        }
        return users;
    }
}
