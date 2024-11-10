package kickstart.Davyd_Lera.models.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import kickstart.Davyd_Lera.models.Client;

import java.time.LocalDate;

@Entity
public class EventOrder extends AbstractOrder {

	private LocalDate eventDate;
	private String deliveryAddress;

	@ManyToOne(cascade = CascadeType.ALL)
	private Client client;

	public EventOrder(LocalDate eventDate, String deliveryAddress, Client client) {
		super(client);
		this.eventDate = eventDate;
		this.deliveryAddress = deliveryAddress;
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
}
