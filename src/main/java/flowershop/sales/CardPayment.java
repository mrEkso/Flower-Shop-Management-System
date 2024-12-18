package flowershop.sales;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.payment.PaymentMethod;

// TODO: Remove this whole class maybe
public class CardPayment extends PaymentMethod {
	public CardPayment() {
		super("Credit Card");
	}

	@NotNull
	public String toString() {
		return "Card()";
	}
}