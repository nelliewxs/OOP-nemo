package oopnemo.model;

public final class Doctor extends Person {
    private String specialization;
    private int patientsCount;

    public Doctor(String id, String name, int age, String email, String city,
                  String specialization, int patientsCount) {
        super(id, name, age, email, city);
        setSpecialization(specialization);
        setPatientsCount(patientsCount);
    }

    @Override public Role getRole() { return Role.DOCTOR; }
    public String getSpecialization() { return specialization; }
    public int getPatientsCount() { return patientsCount; }
    public void setSpecialization(String value) {
        specialization = Validation.text(value, "Specialization");
    }
    public void setPatientsCount(int count) {
        patientsCount = Validation.range(count, 0, Integer.MAX_VALUE, "Patient count");
    }
    public String examinePatient(String patientName) {
        String checkedName = Validation.text(patientName, "Patient name");
        patientsCount = Math.incrementExact(patientsCount);
        return "Dr. " + getName() + " examined " + checkedName + ". Total patients: " + patientsCount;
    }
    public long calculateMonthlyIncome() { return 500L + patientsCount * 20L; }
    @Override public long calculateMonthlyPayment() { return calculateMonthlyIncome(); }
}
