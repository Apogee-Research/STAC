package com.cyberpointllc.stac.snapservice.persist;

import com.cyberpointllc.stac.snapservice.LocationService;
import com.cyberpointllc.stac.snapservice.model.Filter;
import com.cyberpointllc.stac.snapservice.model.FilterFactory;
import com.cyberpointllc.stac.snapservice.model.Location;
import com.cyberpointllc.stac.snapservice.model.Photo;
import org.mapdb.Serializer;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PhotoSerializer extends Serializer<Photo> {

    private final LocationService locationService;

    public PhotoSerializer(LocationService locationService) {
        if (locationService == null) {
            throw new  IllegalArgumentException("LocationService may not be null");
        }
        this.locationService = locationService;
    }

    @Override
    public void serialize(DataOutput out, Photo photo) throws IOException {
        out.writeUTF(photo.getPath());
        out.writeBoolean(photo.isPublicPhoto());
        out.writeUTF(photo.getCaption());
        out.writeUTF(photo.getLocation().getIdentity());
        out.writeInt(photo.getFilters().size());
        for (Filter filter : photo.getFilters()) {
            out.writeUTF(filter.getIdentity());
        }
    }

    @Override
    public Photo deserialize(DataInput in, int available) throws IOException {
        Classdeserialize replacementClass = new  Classdeserialize(in, available);
        ;
        replacementClass.doIt0();
        replacementClass.doIt1();
        replacementClass.doIt2();
        return replacementClass.doIt3();
    }

    public class Classdeserialize {

        public Classdeserialize(DataInput in, int available) throws IOException {
            this.in = in;
            this.available = available;
        }

        private DataInput in;

        private int available;

        private String path;

        private boolean isPublicPhoto;

        private String caption;

        public void doIt0() throws IOException {
            path = in.readUTF();
            isPublicPhoto = in.readBoolean();
            caption = in.readUTF();
        }

        private String locationIdentity;

        private Location location;

        public void doIt1() throws IOException {
            locationIdentity = in.readUTF();
            location = locationService.getLocation(locationIdentity);
        }

        private List<Filter> filters;

        private int numberOfFilters;

        public void doIt2() throws IOException {
            filters = new  ArrayList();
            numberOfFilters = in.readInt();
        }

        private Photo photo;

        public Photo doIt3() throws IOException {
            while (numberOfFilters-- > 0) {
                Filter filter = FilterFactory.getFilter(in.readUTF());
                if (filter != null) {
                    filters.add(filter);
                }
            }
            try {
                photo = new  Photo(path, isPublicPhoto, caption, location, filters);
            } catch (IllegalArgumentException e) {
                throw new  IOException("Trouble deserializing Photo", e);
            }
            return photo;
        }
    }
}
