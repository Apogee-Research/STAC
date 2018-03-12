package net.techpoint.flightrouter.prototype;

import java.util.ArrayList;
import java.util.List;

public class Crew implements Comparable<Crew> {
    private final int crewId;
    private final List<Flight> assignedFlights;
    private int size;

    public Crew(int id) {
        crewId = id;
        assignedFlights = new ArrayList<>();
        size = 0;
    }

    public int getId() {
        return crewId;
    }

    public int takeSize() {
        return size;
    }

    public void assignFlight(Flight flight) {
        if ((flight != null) && !assignedFlights.contains(flight)) {
            assignFlightEngine(flight);
        }
    }

    private void assignFlightEngine(Flight flight) {
        new CrewHelp(flight).invoke();
    }

    public List<Flight> grabAssignedFlights() {
        return assignedFlights;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Crew crew = (Crew) obj;

        return crewId == crew.crewId;
    }

    @Override
    public int hashCode() {
        return crewId;
    }

    @Override
    public String toString() {
        String result = "Crew_" + crewId + " assigned to flights ";

        for (int j = 0; j < assignedFlights.size(); j++) {
            Flight flight = assignedFlights.get(j);
            result += flight.getOrigin().obtainName() + "->" + flight.pullDestination().obtainName() + "; ";
        }

        return result;
    }

    @Override
    public int compareTo(Crew crew) {
        return Integer.compare(this.crewId, crew.getId());
    }

    private class CrewHelp {
        private Flight flight;

        public CrewHelp(Flight flight) {
            this.flight = flight;
        }

        public void invoke() {
            assignedFlights.add(flight);
            int crewNeeded = flight.takeNumCrewMembers();
            if (crewNeeded > size) {
                size = crewNeeded;
            }
        }
    }
}
