package flowershop.models.orders;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import flowershop.models.Client;
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

	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	public EventOrder(LocalDate eventDate, String deliveryAddress, UserAccount orderProcessingEmployee, Client client, String notes) {
		super(orderProcessingEmployee, notes);
		this.eventDate = eventDate;
		this.deliveryAddress = deliveryAddress;
		this.client = client;
	}

	public EventOrder(LocalDate eventDate, String deliveryAddress, UserAccount orderProcessingEmployee, Client client) {
		super(orderProcessingEmployee);
		this.eventDate = eventDate;
		this.deliveryAddress = deliveryAddress;
		this.client = client;
	}

	public EventOrder() {
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
