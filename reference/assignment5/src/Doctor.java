public class Doctor extends Person implements Payable {
    private String specialization = "General";
    private int patientsCount = 0;

    public Doctor(String name, int age, String email, String city,
                  String specialization, int patientsCount) {
        super(name, age, email, city);
        setSpecialization(specialization);
        setPatientsCount(patientsCount);
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        if (specialization == null || specialization.trim().isEmpty()) {
            System.out.println("Error: specialization can not be empty.");
        } else {
            this.specialization = specialization;
        }
    }

    public int getPatientsCount() {

        return patientsCount;
    }

    public void setPatientsCount(int patientsCount) {
        if (patientsCount < 0) {
            System.out.println("Error: patients count can not be negative.");
        } else {
            this.patientsCount = patientsCount;
        }
    }

    @Override
    public void displayInfo() {
        super.displayInfo();
        System.out.println("Role: Doctor");
        System.out.println("Specialization: " + specialization);
        System.out.println("Patients: " + patientsCount);
    }

    public void examinePatient(String patientName) {
        patientsCount++;
        System.out.println("Dr. " + getName() + " examined " + patientName
                + ". Total patients: " + patientsCount);
    }

    // 500 USD base salary plus 20 USD for every patient.
    public int calculateMonthlyIncome() {
        return 500 + patientsCount * 20;
    }

    @Override
    public int calculateMonthlyPayment() {
        return calculateMonthlyIncome();
    }
}
