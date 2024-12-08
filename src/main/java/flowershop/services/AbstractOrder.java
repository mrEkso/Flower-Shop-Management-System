package flowershop.services;

import flowershop.sales.CardPayment;
import jakarta.persistence.MappedSuperclass;
import org.salespointframework.order.Order;
import org.salespointframework.payment.Cash;
import org.salespointframework.useraccount.UserAccount;

import java.util.Objects;

/**
 * The AbstractOrder class is an abstract representation of an order in the flower shop system.
 * It extends the Order class from the Salespoint framework and is annotated with @MappedSuperclass,
 * indicating that it is a base class for other entities but does not require a separate table in the database.
 */
@MappedSuperclass
public abstract class AbstractOrder extends Order {
	private String notes;

	/**
	 * Constructs an AbstractOrder with the specified user and notes.
	 *
	 * @param user  the user account associated with the order
	 * @param notes additional notes for the order
	 * @throws NullPointerException if the user ID is null
	 */
	public AbstractOrder(UserAccount user, String notes) {
		super(Objects.requireNonNull(user.getId()));
		this.notes = notes;
	}

	/**
	 * Constructs an AbstractOrder with the specified user.
	 *
	 * @param user the user account associated with the order
	 * @throws NullPointerException if the user ID is null
	 */
	public AbstractOrder(UserAccount user) {
		super(Objects.requireNonNull(user.getId()));
	}

	/**
	 * Default constructor for AbstractOrder.
	 * This constructor is primarily used by JPA and other frameworks.
	 */
	@SuppressWarnings({"unused", "deprecation"})
	public AbstractOrder() {
		super();
	}

	/**
	 * Returns the notes associated with the order.
	 *
	 * @return the notes
	 */
	public String getNotes() {
		return notes;
	}

	/**
	 * Sets the notes for the order.
	 *
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {
		this.notes = notes;
	}

	/**
	 * Sets the payment method for the order based on the provided payment method string.
	 *
	 * @param paymentMethod the payment method as a string ("Cash" or "Card")
	 */
	public void setPaymentMethod(String paymentMethod) {
		if (paymentMethod.equals("Cash")) {
			this.setPaymentMethod(Cash.CASH);
		} else if (paymentMethod.equals("Card")) {
			this.setPaymentMethod(new CardPayment());
		}
	}
}
