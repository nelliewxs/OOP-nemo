import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Hospital {
    private final String name;
    private final List<Doctor> doctors = new ArrayList<>();

    public Hospital(String name) {
        this.name = name;
    }

    // Aggregation: doctors are created outside and only referenced here.

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public String getName() {
        return name;
    }

    public List<Doctor> getDoctors() {
        return Collections.unmodifiableList(doctors);
    }
}
