package oopnemo.model;

/** A person keeps the same immutable business ID across all subclasses. */
public abstract class Person implements Payable, Comparable<Person> {
    private final String id;
    private String name;
    private int age;
    private String email;
    private String city;

    protected Person(String id, String name, int age, String email, String city) {
        this.id = Validation.text(id, "Person ID");
        updateContactDetails(name, age, email, city);
    }

    public final String getId() { return id; }
    @Override public final String getName() { return name; }
    public final int getAge() { return age; }
    public final String getEmail() { return email; }
    public final String getCity() { return city; }
    public abstract Role getRole();

    public final void setName(String name) { this.name = Validation.text(name, "Name"); }
    public final void setAge(int age) { this.age = Validation.range(age, 0, 120, "Age"); }
    public final void setEmail(String email) { this.email = Validation.email(email); }
    public final void setCity(String city) { this.city = Validation.text(city, "City"); }

    /** Validate all fields first so an invalid multi-field update is atomic. */
    public final void updateContactDetails(String name, int age, String email, String city) {
        String checkedName = Validation.text(name, "Name");
        int checkedAge = Validation.range(age, 0, 120, "Age");
        String checkedEmail = Validation.email(email);
        String checkedCity = Validation.text(city, "City");
        this.name = checkedName;
        this.age = checkedAge;
        this.email = checkedEmail;
        this.city = checkedCity;
    }

    public String displayInfo() {
        return String.format("%s | %-7s | %-17s | age %d | %s | %s | payment %d USD",
                id, getRole(), name, age, email, city, calculateMonthlyPayment());
    }

    @Override public final int compareTo(Person other) { return id.compareTo(other.id); }

    @Override public final boolean equals(Object other) {
        return this == other || other instanceof Person person && id.equals(person.id);
    }

    @Override public final int hashCode() { return id.hashCode(); }
    @Override public final String toString() { return displayInfo(); }
}
