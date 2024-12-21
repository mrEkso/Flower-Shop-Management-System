package flowershop.sales;

import org.jetbrains.annotations.NotNull;
import org.salespointframework.payment.PaymentMethod;

/**
 * A workaround class to avoid using overcomplicated PaymentCard class from salespoint.
 */
public class CardPayment extends PaymentMethod {
	public CardPayment() {
		super("Credit Card");
	}

	@NotNull
	public String toString() {
		return "Card()";
	}
}