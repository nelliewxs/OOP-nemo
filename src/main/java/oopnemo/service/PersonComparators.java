package oopnemo.service;

import java.util.Comparator;
import oopnemo.model.Person;

public final class PersonComparators {
    private PersonComparators() { }
    public static final Comparator<Person> BY_NAME = Comparator
            .comparing(Person::getName, String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Person::getId);
    public static final Comparator<Person> BY_AGE = Comparator
            .comparingInt(Person::getAge).thenComparing(Person::getId);
    public static final Comparator<Person> BY_PAYMENT_DESC = Comparator
            .comparingLong(Person::calculateMonthlyPayment).reversed()
            .thenComparing(Person::getId);
}
