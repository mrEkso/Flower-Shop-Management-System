package flowershop.services;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDateTime;

/**
 * Represents a one-time order for event-based flower arrangements. This type of order is designed
 * for clients who require flower services for specific events, such as weddings, corporate events,
 * or celebrations, where arrangements are to be delivered to a specific location on a designated date.
 * <p>Example scenarios include weddings that require complex flower setups, or corporate events
 * where flowers are delivered and arranged for the venue.</p>
 */
@Entity
public class EventOrder extends AbstractOrder {

	private LocalDateTime eventDate;
	private String deliveryAddress;

	@ManyToOne
	private Client client;

	/**
	 * Constructs an `EventOrder` with the specified user, event date, delivery address, client, and notes.
	 *
	 * @param user            the user account associated with the order
	 * @param eventDate       the date of the event
	 * @param deliveryAddress the address where the flowers will be delivered
	 * @param client          the client associated with the order
	 * @param notes           additional notes for the order
	 */
	public EventOrder(UserAccount user, LocalDateTime eventDate, String deliveryAddress, Client client, String notes) {
		super(user, notes);
		this.client = client;
		this.eventDate = eventDate;
		this.deliveryAddress = deliveryAddress;
	}

	/**
	 * Default constructor for `EventOrder`.
	 * This constructor is primarily used by JPA and other frameworks.
	 */
	protected EventOrder() {
	}

	/**
	 * Returns the date of the event.
	 *
	 * @return the event date
	 */
	public LocalDateTime getEventDate() {
		return eventDate;
	}

	/**
	 * Sets the date of the event.
	 *
	 * @param eventDate the event date to set
	 */
	public void setEventDate(LocalDateTime eventDate) {
		this.eventDate = eventDate;
	}

	/**
	 * Returns the delivery address.
	 *
	 * @return the delivery address
	 */
	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	/**
	 * Sets the delivery address.
	 *
	 * @param deliveryAddress the delivery address to set
	 */
	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	/**
	 * Returns the client associated with the order.
	 *
	 * @return the client
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * Sets the client associated with the order.
	 *
	 * @param client the client to set
	 */
	public void setClient(Client client) {
		this.client = client;
	}
}