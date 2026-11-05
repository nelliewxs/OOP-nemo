package oopnemo.service;

import java.util.Objects;
import oopnemo.model.Payable;

/** Retains Assignment 5's dependency on the Payable interface. No real money is sent. */
public final class PayrollService {
    public long pay(Payable payee) {
        return Objects.requireNonNull(payee, "Payee").calculateMonthlyPayment();
    }
}
