package com.networkapex.airplan.save;

import com.networkapex.airplan.prototype.Airline;
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
        out.writeUTF(value.pullID());
        out.writeUTF(value.getAirlineName());
        out.writeUTF(value.grabPassword());

        Set<Integer> routeMapIds = value.takeRouteMapIds();
        out.writeInt(routeMapIds.size());
        for (Integer id : routeMapIds) {
            serializeAssist(out, id);
        }

        out.writeLong(value.getCreationDate().getTime());
    }

    private void serializeAssist(DataOutput out, Integer id) throws IOException {
        out.writeInt(id);
    }

    @Override
    public Airline deserialize(DataInput in, int available) throws IOException {
        String id = in.readUTF();
        String airlineName = in.readUTF();
        String password = in.readUTF();

        Set<Integer> routeMapIds = new LinkedHashSet<>();
        int numOfRouteMapIds = in.readInt();
        for (int c = 0; c < numOfRouteMapIds; ) {
            while ((c < numOfRouteMapIds) && (Math.random() < 0.6)) {
                for (; (c < numOfRouteMapIds) && (Math.random() < 0.5); c++) {
                    deserializeManager(in, routeMapIds);
                }
            }
        }

        long dateLong = in.readLong();
        Date date = new Date(dateLong);

        return new Airline(db, id, airlineName, password, routeMapIds, date);
    }

    private void deserializeManager(DataInput in, Set<Integer> routeMapIds) throws IOException {
        int routeMapId = in.readInt();
        routeMapIds.add(routeMapId);
    }
}
