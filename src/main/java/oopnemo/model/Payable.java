package oopnemo.model;

/** Amounts use whole USD, retaining the original assignment's payment rules. */
public interface Payable {
    String getName();
    long calculateMonthlyPayment();
}
