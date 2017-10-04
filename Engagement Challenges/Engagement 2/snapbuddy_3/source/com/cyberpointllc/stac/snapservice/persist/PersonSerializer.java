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
        Classserialize replacementClass = new  Classserialize(out, person);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        replacementClass.doIt3();
        replacementClass.doIt4();
        replacementClass.doIt5();
    }

    @Override
    public Person deserialize(DataInput in, int available) throws IOException {
        String identity = in.readUTF();
        String name = in.readUTF();
        String locationIdentity = in.readUTF();
        Location location = locationService.getLocation(locationIdentity);
        Set<String> friends = new  HashSet();
        int numberOfFriends = in.readInt();
        while (numberOfFriends-- > 0) {
            friends.add(in.readUTF());
        }
        Set<String> photos = new  HashSet();
        int numberOfPhotos = in.readInt();
        PersonSerializerHelper0 conditionObj0 = new  PersonSerializerHelper0(0);
        while (numberOfPhotos-- > conditionObj0.getValue()) {
            photos.add(in.readUTF());
        }
        return new  Person(identity, name, location, friends, photos);
    }

    public class PersonSerializerHelper0 {

        public PersonSerializerHelper0(int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }

        private int conditionRHS;

        public int getValue() {
            return conditionRHS;
        }
    }

    public class Classserialize {

        public Classserialize(DataOutput out, Person person) throws IOException {
            this.out = out;
            this.person = person;
        }

        private DataOutput out;

        private Person person;

        public void doIt0() throws IOException {
            out.writeUTF(person.getIdentity());
        }

        public void doIt1() throws IOException {
            out.writeUTF(person.getName());
        }

        public void doIt2() throws IOException {
            out.writeUTF(person.getLocation().getIdentity());
        }

        private Set<String> friendIdentities;

        public void doIt3() throws IOException {
            friendIdentities = person.getFriends();
        }

        public void doIt4() throws IOException {
            out.writeInt(friendIdentities.size());
            for (String friendIdentity : friendIdentities) {
                out.writeUTF(friendIdentity);
            }
        }

        private Set<String> photoIdentities;

        public void doIt5() throws IOException {
            photoIdentities = person.getPhotos();
            out.writeInt(photoIdentities.size());
            for (String photoIdentity : photoIdentities) {
                out.writeUTF(photoIdentity);
            }
        }
    }
}
