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
        serializeHelper(photo, out);
    }

    @Override
    public Photo deserialize(DataInput in, int available) throws IOException {
        String path = in.readUTF();
        boolean isPublicPhoto = in.readBoolean();
        String caption = in.readUTF();
        String locationIdentity = in.readUTF();
        Location location = locationService.getLocation(locationIdentity);
        List<Filter> filters = new  ArrayList();
        int numberOfFilters = in.readInt();
        while (numberOfFilters-- > 0) {
            deserializeHelper(in, filters);
        }
        Photo photo;
        try {
            photo = new  Photo(path, isPublicPhoto, caption, location, filters);
        } catch (IllegalArgumentException e) {
            throw new  IOException("Trouble deserializing Photo", e);
        }
        return photo;
    }

    private void serializeHelper(Photo photo, DataOutput out) throws IOException {
        out.writeUTF(photo.getPath());
        out.writeBoolean(photo.isPublicPhoto());
        out.writeUTF(photo.getCaption());
        out.writeUTF(photo.getLocation().getIdentity());
        out.writeInt(photo.getFilters().size());
        for (Filter filter : photo.getFilters()) {
            out.writeUTF(filter.getIdentity());
        }
    }

    private void deserializeHelper(DataInput in, List<Filter> filters) throws IOException {
        Filter filter = FilterFactory.getFilter(in.readUTF());
        if (filter != null) {
            filters.add(filter);
        }
    }
}
