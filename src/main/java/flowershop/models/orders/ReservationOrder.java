package flowershop.models.orders;

import jakarta.persistence.Entity;
import flowershop.models.Client;

import org.salespointframework.useraccount.UserAccount;
import org.springframework.util.Assert;

import flowershop.models.enums.ReservationStatus;

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

	private LocalDateTime reservationDateTime; // Date and time for the reservation
	/* In addition to the orderStatus field built into the Order parent class,
	 which can be “Open”, “Paid”, “Completed”, “Canceled”, we will have a
	 reservationStatus field that shows the progress of the reservation process itself. */
	private ReservationStatus reservationStatus;

	public ReservationOrder(LocalDateTime reservationDateTime, UserAccount orderProcessingEmployee, Client client, String notes) {
		super(orderProcessingEmployee, client, notes);
		this.reservationDateTime = reservationDateTime;
		this.reservationStatus = ReservationStatus.IN_PROCESS;
	}

	public ReservationOrder(LocalDateTime reservationDateTime, UserAccount orderProcessingEmployee, Client client) {
		super(orderProcessingEmployee, client);
		this.reservationDateTime = reservationDateTime;
		this.reservationStatus = ReservationStatus.IN_PROCESS;
	}

	public ReservationOrder() {
	}

	public LocalDateTime getReservationDateTime() {
		return reservationDateTime;
	}

	public void setReservationDateTime(LocalDateTime reservationDateTime) {
		this.reservationDateTime = reservationDateTime;
	}

	public ReservationStatus getReservationStatus() {
		return reservationStatus;
	}

	ReservationOrder markInProcess() {
		this.reservationStatus = ReservationStatus.IN_PROCESS;
		return this;
	}

	ReservationOrder markReadyForPickup() {
		this.reservationStatus = ReservationStatus.READY_FOR_PICKUP;
		return this;
	}

	ReservationOrder markPickedUp() {
		Assert.isTrue(this.isPaid(), "A reservation must be paid to be picked up!");
		this.reservationStatus = ReservationStatus.PICKED_UP;
		return this;
	}
}
