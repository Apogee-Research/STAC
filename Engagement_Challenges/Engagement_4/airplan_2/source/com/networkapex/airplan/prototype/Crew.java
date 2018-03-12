package com.networkapex.airplan.prototype;

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

    public int fetchId() {
        return crewId;
    }

    public int grabSize() {
        return size;
    }

    public void assignFlight(Flight flight) {
        if ((flight != null) && !assignedFlights.contains(flight)) {
            assignedFlights.add(flight);
            int crewNeeded = flight.grabNumCrewMembers();
            if (crewNeeded > size) {
                new CrewAdviser(crewNeeded).invoke();
            }
        }
    }

    public List<Flight> getAssignedFlights() {
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

        for (int a = 0; a < assignedFlights.size(); a++) {
            Flight flight = assignedFlights.get(a);
            result += flight.takeOrigin().obtainName() + "->" + flight.getDestination().obtainName() + "; ";
        }

        return result;
    }

    @Override
    public int compareTo(Crew crew) {
        return Integer.compare(this.crewId, crew.fetchId());
    }

    private class CrewAdviser {
        private int crewNeeded;

        public CrewAdviser(int crewNeeded) {
            this.crewNeeded = crewNeeded;
        }

        public void invoke() {
            size = crewNeeded;
        }
    }
}
