package net.cybertip.routing.keep;

import net.cybertip.routing.framework.Airline;
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
        out.writeUTF(value.grabID());
        out.writeUTF(value.grabAirlineName());
        out.writeUTF(value.fetchPassword());

        Set<Integer> routeMapIds = value.takeRouteMapIds();
        out.writeInt(routeMapIds.size());
        for (Integer id : routeMapIds) {
            out.writeInt(id);
        }

        out.writeLong(value.grabCreationDate().getTime());
    }

    @Override
    public Airline deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String airlineName = in.readUTF();
        String password = in.readUTF();

        Set<Integer> routeMapIds = new LinkedHashSet<>();
        int numOfRouteMapIds = in.readInt();
        for (int c = 0; c < numOfRouteMapIds; c++ ) {
            int routeMapId = in.readInt();
            routeMapIds.add(routeMapId);
        }

        long dateLong = in.readLong();
        Date date = new Date(dateLong);

        return new Airline(db, id, airlineName, password, routeMapIds, date);
    }
}
