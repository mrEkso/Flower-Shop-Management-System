package flowershop.models.orders;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import flowershop.models.Client;
import jakarta.persistence.ManyToOne;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDateTime;

/**
 * Represents an order for a reservation to pick up flower arrangements from the shop at a
 * specified date and time. This type of order is designed for clients who prefer to collect
 * their flowers in person, often used for custom or pre-arranged bouquets that are ready upon arrival.
 *
 * <p>Typical use cases include walk-in clients who schedule pickup orders or clients who prefer
 * to inspect arrangements before taking them home.</p>
 */
@Entity
public class ReservationOrder extends AbstractOrder {

	private LocalDateTime dateTime; // Date and time for the reservation

	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	public ReservationOrder(UserAccount userAccount, OrderStatus orderStatus, LocalDateTime dateTime, Client client) {
		super(userAccount);
		this.dateTime = dateTime;
	}

	public ReservationOrder(UserAccount userAccount) {
		super(userAccount);
	}

	protected ReservationOrder() {

	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}