package flowershop.services;

import flowershop.sales.CardPayment;
import jakarta.persistence.MappedSuperclass;
import org.salespointframework.order.Order;
import org.salespointframework.payment.Cash;
import org.salespointframework.useraccount.UserAccount;

import java.util.Objects;

/*
	The @MappedSuperclass annotation tells Hibernate that
	it does not need to create a separate table from this class.
	But at the same time, child classes that extend this class
	will adopt the properties and fields of this class.
 */
@MappedSuperclass
public abstract class AbstractOrder extends Order {
	private String notes;

	/*
	The order is always built on the worker who took the order and
	the client to whom the order is given. Depending on the type of order,
	various other fields will be added.
	 */
	public AbstractOrder(UserAccount user, String notes) {
		super(Objects.requireNonNull(user.getId()));
		this.notes = notes; // #FIXME use it in OrderFactory
	}

	public AbstractOrder(UserAccount user) {
		super(Objects.requireNonNull(user.getId()));
	}

	@SuppressWarnings({"unused", "deprecation"})
	public AbstractOrder() {
		super();
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public void setPaymentMethod(String paymentMethod) {
		if (paymentMethod.equals("Cash")) {
			this.setPaymentMethod(Cash.CASH);
		} else if (paymentMethod.equals("Card")) {
			this.setPaymentMethod(new CardPayment());
		}
	}
}
