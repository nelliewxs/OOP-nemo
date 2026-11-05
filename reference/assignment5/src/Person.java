public abstract class Person {
    private String name = "Unknown";
    private int age = 18;
    private String email = "no-email@mail.com";
    private String city = "Unknown";

    protected Person(String name, int age, String email, String city) {
        setName(name);
        setAge(age);
        setEmail(email);
        setCity(city);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {

        if (name == null || name.trim().isEmpty()) {
            System.out.println("Error: name can not be empty. Old value is kept.");
        } else {
            this.name = name;
        }
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 0 || age > 120) {
            System.out.println("Error: age " + age
                    + " is not valid. Old value is kept.");
        } else {
            this.age = age;
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            System.out.println("Error: email must contain @. Old value is kept.");
        } else {
            this.email = email;
        }
    }

    public String getCity() {
        return city;
    }


    public void setCity(String city) {
        if (city == null || city.trim().isEmpty()) {
            System.out.println("Error: city can not be empty. Old value is kept.");
        } else {
            this.city = city;
        }
    }

    public void displayInfo() {
        System.out.println("Name: " + name);
        System.out.println("Age: " + age);
        System.out.println("Email: " + email);
        System.out.println("City: " + city);
    }
}
