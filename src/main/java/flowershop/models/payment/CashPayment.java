package flowershop.models.payment;

import org.salespointframework.payment.PaymentMethod;

public class CashPayment extends PaymentMethod {
	public CashPayment() {
		super("Cash");
	}
}