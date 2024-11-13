package flowershop.models.payments;

import org.salespointframework.payment.PaymentMethod;

public class CardPayment extends PaymentMethod {
	public CardPayment() {
		super("Credit Card");
	}
}