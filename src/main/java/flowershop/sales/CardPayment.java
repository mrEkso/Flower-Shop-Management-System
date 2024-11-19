package flowershop.sales;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.payment.PaymentMethod;

public class CardPayment extends PaymentMethod {
	public CardPayment() {
		super("Credit Card");
	}

	@NotNull
	public String toString() {
		return "Card()";
	}
}