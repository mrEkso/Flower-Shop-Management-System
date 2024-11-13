package kickstart.Davyd_Lera.models.orders;

import jakarta.persistence.Entity;
import kickstart.Davyd_Lera.models.Client;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDate;

@Entity
public class EventOrder extends AbstractOrder {

	private LocalDate eventDate;
	private String deliveryAddress;

	public EventOrder(LocalDate eventDate, String deliveryAddress, UserAccount orderProcessingEmployee, Client client) {
		super(orderProcessingEmployee, client);
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
