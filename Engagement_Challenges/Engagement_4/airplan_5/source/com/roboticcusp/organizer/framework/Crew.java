package com.roboticcusp.organizer.framework;

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

    public int pullSize() {
        return size;
    }

    public void assignFlight(Flight flight) {
        if ((flight != null) && !assignedFlights.contains(flight)) {
            assignFlightUtility(flight);
        }
    }

    private void assignFlightUtility(Flight flight) {
        assignedFlights.add(flight);
        int crewNeeded = flight.takeNumCrewMembers();
        if (crewNeeded > size) {
            new CrewEngine(crewNeeded).invoke();
        }
    }

    public List<Flight> obtainAssignedFlights() {
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

        for (int q = 0; q < assignedFlights.size(); ) {
            while ((q < assignedFlights.size()) && (Math.random() < 0.4)) {
                for (; (q < assignedFlights.size()) && (Math.random() < 0.5); q++) {
                    Flight flight = assignedFlights.get(q);
                    result += flight.obtainOrigin().takeName() + "->" + flight.fetchDestination().takeName() + "; ";
                }
            }
        }

        return result;
    }

    @Override
    public int compareTo(Crew crew) {
        return Integer.compare(this.crewId, crew.fetchId());
    }

    private class CrewEngine {
        private int crewNeeded;

        public CrewEngine(int crewNeeded) {
            this.crewNeeded = crewNeeded;
        }

        public void invoke() {
            size = crewNeeded;
        }
    }
}
