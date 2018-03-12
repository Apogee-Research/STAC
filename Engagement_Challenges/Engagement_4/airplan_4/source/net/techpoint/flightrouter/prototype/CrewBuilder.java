package net.techpoint.flightrouter.prototype;

public class CrewBuilder {
    private int id;

    public CrewBuilder fixId(int id) {
        this.id = id;
        return this;
    }

    public Crew formCrew() {
        return new Crew(id);
    }
}