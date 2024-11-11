package flowershop.models.order;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import flowershop.models.Client;
import jakarta.persistence.ManyToOne;
import org.salespointframework.order.OrderStatus;
import org.salespointframework.useraccount.UserAccount;

import java.time.LocalDateTime;

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
