package edu.cyberapex.flightplanner.framework;

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

    public int takeId() {
        return crewId;
    }

    public int getSize() {
        return size;
    }

    public void assignFlight(Flight flight) {
        if ((flight != null) && !assignedFlights.contains(flight)) {
            assignedFlights.add(flight);
            int crewNeeded = flight.pullNumCrewMembers();
            if (crewNeeded > size) {
                size = crewNeeded;
            }
        }
    }

    public List<Flight> takeAssignedFlights() {
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

        for (int c = 0; c < assignedFlights.size(); c++) {
            Flight flight = assignedFlights.get(c);
            result += flight.obtainOrigin().getName() + "->" + flight.grabDestination().getName() + "; ";
        }

        return result;
    }

    @Override
    public int compareTo(Crew crew) {
        return Integer.compare(this.crewId, crew.takeId());
    }
}
