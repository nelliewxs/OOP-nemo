package oopnemo.app;

import oopnemo.model.*;
import oopnemo.service.CampusService;

public final class DemoData {
    private DemoData() { }
    public static CampusService create() {
        CampusService service = new CampusService();
        service.addPerson(new Student("S001", "Aizada Omurova", 19, "aizada@example.com", "Osh", "CS-21", 3.8, 2));
        service.addPerson(new Student("S002", "Timur Asanov", 21, "timur@example.com", "Bishkek", "CS-22", 3.2, 3));
        service.addPerson(new Teacher("T001", "Nurlan Sadykov", 45, "nurlan@example.com", "Bishkek", "Java", 15, 900));
        service.addPerson(new Doctor("D001", "Elena Petrova", 38, "elena@example.com", "Karakol", "Cardiology", 120));
        service.addPerson(new Doctor("D002", "Azamat Bekov", 32, "azamat@example.com", "Osh", "Neurology", 80));
        service.addHospital(new Hospital("H001", "City Hospital No. 1"));
        service.addHospital(new Hospital("H002", "Private Clinic"));
        service.assignDoctor("H001", "D001");
        service.assignDoctor("H002", "D001");
        service.assignDoctor("H002", "D002");
        service.recordExam("S001", "Mathematics", 87);
        service.recordExam("S001", "Physics", 41);
        return service;
    }
}
