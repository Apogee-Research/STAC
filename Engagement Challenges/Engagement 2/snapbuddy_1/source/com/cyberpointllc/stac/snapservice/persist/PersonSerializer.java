package com.cyberpointllc.stac.snapservice.persist;

import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Person;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PersonSerializer extends Serializer<Person> {

    private final LocationService locationService;

    public PersonSerializer(LocationService locationService) {
        if (locationService == null) {
            throw new  IllegalArgumentException("LocationService may not be null");
        }
        this.locationService = locationService;
    }

    @Override
    public void serialize(DataOutput out, Person person) throws IOException {
        out.writeUTF(person.getIdentity());
        out.writeUTF(person.getName());
        out.writeUTF(person.getLocation().getIdentity());
        Set<String> friendIdentities = person.getFriends();
        out.writeInt(friendIdentities.size());
        for (String friendIdentity : friendIdentities) {
            out.writeUTF(friendIdentity);
        }
        Set<String> photoIdentities = person.getPhotos();
        out.writeInt(photoIdentities.size());
        for (String photoIdentity : photoIdentities) {
            out.writeUTF(photoIdentity);
        }
    }

    @Override
    public Person deserialize(DataInput in, int available) throws IOException {
        String identity = in.readUTF();
        String name = in.readUTF();
        String locationIdentity = in.readUTF();
        Location location = locationService.getLocation(locationIdentity);
        Set<String> friends = new  HashSet();
        int numberOfFriends = in.readInt();
        int conditionObj0 = 0;
        while (numberOfFriends-- > conditionObj0) {
            friends.add(in.readUTF());
        }
        Set<String> photos = new  HashSet();
        int numberOfPhotos = in.readInt();
        int conditionObj1 = 0;
        while (numberOfPhotos-- > conditionObj1) {
            photos.add(in.readUTF());
        }
        return new  Person(identity, name, location, friends, photos);
    }
}
