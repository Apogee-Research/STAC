package com.roboticcusp.organizer.save;

import com.roboticcusp.organizer.framework.Airline;
import org.mapdb.Serializer;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class AirlineSerializer extends Serializer<Airline> {

    private final AirDatabase db;

    public AirlineSerializer(AirDatabase db) {
        this.db = db;
    }

    @Override
    public void serialize(DataOutput out, Airline value) throws IOException {
        out.writeUTF(value.getID());
        out.writeUTF(value.obtainAirlineName());
        out.writeUTF(value.grabPassword());

        Set<Integer> routeMapIds = value.grabRouteMapIds();
        out.writeInt(routeMapIds.size());
        for (Integer id : routeMapIds) {
            out.writeInt(id);
        }

        out.writeLong(value.pullCreationDate().getTime());
    }

    @Override
    public Airline deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String airlineName = in.readUTF();
        String password = in.readUTF();

        Set<Integer> routeMapIds = new LinkedHashSet<>();
        int numOfRouteMapIds = in.readInt();
        for (int q = 0; q < numOfRouteMapIds; q++ ) {
            deserializeHome(in, routeMapIds);
        }

        long dateLong = in.readLong();
        Date date = new Date(dateLong);

        return new Airline(db, id, airlineName, password, routeMapIds, date);
    }

    private void deserializeHome(DataInput in, Set<Integer> routeMapIds) throws IOException {
        int routeMapId = in.readInt();
        routeMapIds.add(routeMapId);
    }
}
