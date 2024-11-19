package flowershop.services;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

/**
 * Represents a one-time order for event-based flower arrangements. This type of order is designed
 * for clients who require flower services for specific events, such as weddings, corporate events,
 * or celebrations, where arrangements are to be delivered to a specific location on a designated date.
 * <p>Example scenarios include weddings that require complex flower setups, or corporate events
 * where flowers are delivered and arranged for the venue.</p>
 */
@Entity
public class EventOrder extends AbstractOrder {

	private LocalDate eventDate;
	private String deliveryAddress;

	@ManyToOne
	private Client client;

	public EventOrder(UserAccount user, LocalDate eventDate, String deliveryAddress, Client client, String notes) {
		super(user, notes);
		this.client = client;
		this.eventDate = eventDate;
		this.deliveryAddress = deliveryAddress;
	}

	protected EventOrder() {
	}

	public LocalDate getEventDate() {
		return eventDate;
	}

	public void setEventDate(LocalDate eventDate) {
		this.eventDate = eventDate;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}