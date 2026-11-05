public class PayrollService {
    // Dependency: Payable is used only as a method parameter, never stored.
    public int pay(Payable payee) {
        int amount = payee.calculateMonthlyPayment();
        System.out.printf("Payment of %d USD sent to %s%n", amount, payee.getName());
        return amount;
    }
}
