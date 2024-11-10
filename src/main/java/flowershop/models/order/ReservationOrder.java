package flowershop.models.order;

import jakarta.persistence.Entity;
import flowershop.models.Client;
import org.salespointframework.order.OrderStatus;

import java.time.LocalDateTime;

@Entity
public class ReservationOrder extends AbstractOrder {

	private LocalDateTime dateTime; // Date and time for the reservation

	public ReservationOrder(OrderStatus orderStatus, LocalDateTime dateTime, Client client) {
		super(client);
		this.dateTime = dateTime;
	}

	public ReservationOrder() {
	}

	public LocalDateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(LocalDateTime dateTime) {
		this.dateTime = dateTime;
	}
}
