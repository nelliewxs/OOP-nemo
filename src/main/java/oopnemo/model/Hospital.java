package oopnemo.model;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

/** Aggregation: removing a hospital does not remove its independently owned doctors. */
public final class Hospital {
    private final String id;
    private String name;
    private final Set<Doctor> doctors = new LinkedHashSet<>();

    public Hospital(String id, String name) {
        this.id = Validation.text(id, "Hospital ID");
        setName(name);
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = Validation.text(name, "Hospital name"); }
    public boolean addDoctor(Doctor doctor) { return doctors.add(Objects.requireNonNull(doctor, "Doctor")); }
    public boolean removeDoctor(Doctor doctor) { return doctors.remove(doctor); }
    public Set<Doctor> getDoctors() { return Set.copyOf(doctors); }
    @Override public boolean equals(Object other) {
        return this == other || other instanceof Hospital hospital && id.equals(hospital.id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return id + " | " + name + " | doctors " + doctors.size(); }
}
